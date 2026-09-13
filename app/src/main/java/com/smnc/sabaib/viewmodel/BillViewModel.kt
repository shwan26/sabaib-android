package com.smnc.sabaib.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smnc.sabaib.data.BillRepository
import com.smnc.sabaib.data.BillRow
import com.smnc.sabaib.data.ItemClaimRepository
import com.smnc.sabaib.data.ParticipantRepository
import com.smnc.sabaib.data.ParticipantRow
import com.smnc.sabaib.data.ProfileRepository
import com.smnc.sabaib.data.PromptPayQrRepository
import com.smnc.sabaib.data.toUserMessage
import com.smnc.sabaib.domain.charges.ChargeCalculator
import com.smnc.sabaib.model.Bill
import com.smnc.sabaib.model.BillStage
import com.smnc.sabaib.model.ItemSelection
import com.smnc.sabaib.model.JoinMethod
import com.smnc.sabaib.model.Participant
import com.smnc.sabaib.model.ParticipantTotal
import com.smnc.sabaib.model.ReceiptItem
import com.smnc.sabaib.util.generateGroupCode
import java.util.UUID
import kotlinx.coroutines.launch

sealed class BillSaveState {
    object Idle : BillSaveState()
    object Saving : BillSaveState()
    object Success : BillSaveState()
    data class Error(val message: String) : BillSaveState()
}

class BillViewModel(
    private val billRepository: BillRepository = BillRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val participantRepository: ParticipantRepository = ParticipantRepository(),
    private val itemClaimRepository: ItemClaimRepository = ItemClaimRepository(),
    private val promptPayQrRepository: PromptPayQrRepository = PromptPayQrRepository()
) : ViewModel() {

    private val _bill = mutableStateOf(
        Bill(
            id = System.currentTimeMillis()
                .toString(),

            code = generateGroupCode()
        )
    )

    val bill: State<Bill> = _bill

    private val _saveState = mutableStateOf<BillSaveState>(BillSaveState.Idle)

    val saveState: State<BillSaveState> = _saveState

    private val _participants = mutableStateOf<List<Participant>>(
        emptyList()
    )

    private val _itemSelections =
        mutableStateOf<List<ItemSelection>>(
            emptyList()
        )

    val itemSelections: State<List<ItemSelection>> =
        _itemSelections

    val participants: State<List<Participant>> = _participants

    // The participant this device/session is acting as: the host who
    // created the room, or the guest who just joined it themselves.
    private val _currentParticipantId =
        mutableStateOf<String?>(null)

    val currentParticipantId: State<String?> =
        _currentParticipantId

    private val _isUploadingPromptPayQr =
        mutableStateOf(false)

    val isUploadingPromptPayQr: State<Boolean> =
        _isUploadingPromptPayQr

    private val _paidStatus =
        mutableStateOf<Map<String, Boolean>>(emptyMap())

    val paidStatus: State<Map<String, Boolean>> =
        _paidStatus

    // Participants removed locally whose delete may not have reached
    // Supabase yet - kept out of the roster so a poll landing mid-delete
    // doesn't resurrect them. See [loadParticipants] / [removeParticipantAndPersist].
    private val _pendingRemovalIds =
        mutableStateOf<Set<String>>(emptySet())

    /**
     * Resets every piece of per-bill-flow local state to fresh defaults,
     * including a newly generated id/code. [BillViewModel] is scoped to the
     * whole app session (constructed once in [com.smnc.sabaib.navigation.AppNavHost]),
     * so without this, starting a second "create a bill"/"join a bill" flow
     * in the same session would silently reuse the first bill's id/code and
     * leftover roster/selections. Call this at the start of both flows,
     * before any navigation happens.
     */
    fun startNewBill() {
        _bill.value = Bill(
            id = System.currentTimeMillis().toString(),
            code = generateGroupCode()
        )
        _participants.value = emptyList()
        _itemSelections.value = emptyList()
        _currentParticipantId.value = null
        _isUploadingPromptPayQr.value = false
        _paidStatus.value = emptyMap()
        _pendingRemovalIds.value = emptySet()
        _saveState.value = BillSaveState.Idle
    }

    /**
     * Uploads [bytes] as the host's PromptPay QR image to Supabase storage,
     * then persists its public URL on the bill row so every device polling
     * the bill (guests included) picks it up - see [pollBillState].
     */
    fun uploadPromptPayQr(billId: String, bytes: ByteArray, contentType: String?) {
        _isUploadingPromptPayQr.value = true

        viewModelScope.launch {
            runCatching {
                val path = "$billId/qr"
                promptPayQrRepository.upload(path, bytes, contentType)
                promptPayQrRepository.publicUrl(path)
            }.onSuccess { url ->
                // Cache-bust so a re-uploaded QR at the same path doesn't
                // keep showing a stale cached image on other devices.
                setPromptPayQrUrl(billId, "$url?t=${System.currentTimeMillis()}")
            }.onFailure {
                Log.e("BillViewModel", "Failed to upload promptpay QR", it)
            }

            _isUploadingPromptPayQr.value = false
        }
    }

    fun removePromptPayQr(billId: String) {
        setPromptPayQrUrl(billId, null)
    }

    private fun setPromptPayQrUrl(billId: String, url: String?) {
        _bill.value = _bill.value.copy(promptPayQrUrl = url)

        viewModelScope.launch {
            runCatching {
                billRepository.updatePromptPayQrUrl(billId, url)
            }.onFailure {
                Log.e("BillViewModel", "Failed to persist promptpay QR url", it)
            }
        }
    }

    fun markParticipantPaid(participantId: String) {
        _paidStatus.value = _paidStatus.value + (participantId to true)
    }

    fun markParticipantUnpaid(participantId: String) {
        _paidStatus.value = _paidStatus.value - participantId
    }

    fun updateItems(items: List<ReceiptItem>) {

        val subtotal = items.sumOf {
            it.price * it.quantity
        }

        _bill.value = _bill.value.copy(
            items = items,
            subtotal = subtotal,
            total = subtotal
        )
    }

    fun updateRestaurantName(name: String) {
        _bill.value = _bill.value.copy(
            restaurantName = name
        )
    }

    /**
     * Points this session at a bill it didn't create itself - i.e. a guest
     * joining via code/QR. Without this, [bill]'s id stays the local
     * placeholder generated at ViewModel construction, and every downstream
     * screen that fetches by [Bill.id] (e.g. [loadParticipants]) would query
     * the wrong bill. Also pulls in the charge totals already computed by
     * the host and fetches the receipt items in the background, since a
     * guest's own [Bill] state otherwise never had them.
     */
    fun adoptJoinedBill(billRow: BillRow) {
        _bill.value = _bill.value.copy(
            id = billRow.id!!,
            code = billRow.code,
            restaurantName = billRow.restaurantName,
            subtotal = billRow.subtotal,
            serviceChargeRate = billRow.serviceChargePercent / 100,
            serviceChargeAmount = billRow.serviceChargeAmount,
            vatRate = billRow.vatPercent / 100,
            vatAmount = billRow.vatAmount,
            discount = billRow.discountAmount,
            total = billRow.totalAmount,
            stage = BillStage.fromDb(billRow.status),
            isSplitEvenly = billRow.isSplitEvenly,
            splitDecided = billRow.splitDecided,
            promptPayQrUrl = billRow.promptPayQrUrl
        )

        loadBillItemsIfMissing(billRow.id)
    }

    /**
     * Fetches this bill's receipt items from Supabase if [Bill.items] is
     * still empty - the case for a guest whose [Bill] was adopted via
     * [adoptJoinedBill] rather than scanned locally. Safe to call
     * repeatedly (e.g. from a poll loop): a host who already has items
     * loaded locally, or a guest whose earlier fetch already succeeded,
     * is a no-op. This also means a guest whose one-shot fetch in
     * [adoptJoinedBill] raced the host's insert, or hit a transient
     * network error, keeps retrying until the items show up instead of
     * being stuck with a permanently empty Split screen.
     */
    fun loadBillItemsIfMissing(billId: String) {
        if (_bill.value.id != billId) return
        if (_bill.value.items.isNotEmpty()) return

        viewModelScope.launch {
            runCatching {
                billRepository.findItemsByBillId(billId)
            }.onSuccess { items ->
                if (items.isNotEmpty()) {
                    _bill.value = _bill.value.copy(items = items)
                }
            }.onFailure {
                Log.e("BillViewModel", "Failed to load bill items", it)
            }
        }
    }

    /**
     * Persists the current bill (and its items) to Supabase, creates the
     * host's own participant row (named from their profile, falling back to
     * [hostNameFallback]), then records a free scan against the owner's
     * rolling 30-day quota. Losing the scan-count update doesn't fail the
     * whole operation - the bill itself is what matters to the user.
     */
    fun saveBillAndProceed(ownerId: String, hostNameFallback: String) {
        _saveState.value = BillSaveState.Saving

        viewModelScope.launch {
            try {
                val saved = billRepository.saveBill(ownerId, _bill.value)
                val row = saved.row
                _bill.value = _bill.value.copy(id = row.id!!, code = row.code, items = saved.items)

                val profile = runCatching { profileRepository.getProfile(ownerId) }.getOrNull()
                val hostName = profile?.displayName?.takeIf { it.isNotBlank() } ?: hostNameFallback
                createHostAndPersist(billId = row.id, userId = ownerId, displayName = hostName)

                runCatching {
                    profileRepository.incrementFreeScanUsageIfNeeded(ownerId)
                }

                _saveState.value = BillSaveState.Success
            } catch (e: Exception) {
                Log.e("BillViewModel", "Failed to save bill", e)
                _saveState.value = BillSaveState.Error(e.toUserMessage())
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = BillSaveState.Idle
    }

    /**
     * Points this session at an existing bill the user tapped from
     * Groups/Recent Groups - as host reopening it or a guest who already
     * joined it. Unlike [adoptJoinedBill] (guest mid-join, no roster/items
     * yet), this fully replaces every piece of per-bill state with a fresh
     * snapshot from Supabase, including the roster and [currentParticipantId]
     * (matched by [currentUserId] against each participant's [ParticipantRow.userId]),
     * so nothing from a previously abandoned scan/join flow leaks in. Returns
     * false (bill missing, or a network/decode failure) without touching any
     * state, so the caller can simply skip navigating.
     */
    suspend fun loadExistingBill(billId: String, currentUserId: String?): Boolean {
        return try {
            val row = billRepository.findById(billId) ?: return false
            val items = billRepository.findItemsByBillId(billId)
            val participantRows = participantRepository.listByBillId(billId)

            _bill.value = Bill(
                id = row.id!!,
                code = row.code,
                restaurantName = row.restaurantName,
                items = items,
                subtotal = row.subtotal,
                serviceChargeRate = row.serviceChargePercent / 100,
                serviceChargeAmount = row.serviceChargeAmount,
                vatRate = row.vatPercent / 100,
                vatAmount = row.vatAmount,
                discount = row.discountAmount,
                total = row.totalAmount,
                stage = BillStage.fromDb(row.status),
                isSplitEvenly = row.isSplitEvenly,
                splitDecided = row.splitDecided,
                promptPayQrUrl = row.promptPayQrUrl
            )
            _participants.value = participantRows.map { it.toParticipant() }
            _currentParticipantId.value = participantRows.find { it.userId == currentUserId }?.id
            _itemSelections.value = emptyList()
            _pendingRemovalIds.value = emptySet()
            _paidStatus.value = emptyMap()
            _isUploadingPromptPayQr.value = false
            _saveState.value = BillSaveState.Idle

            loadItemClaims(billId)
            true
        } catch (e: Exception) {
            Log.e("BillViewModel", "Failed to load existing bill $billId", e)
            false
        }
    }

    /**
     * Fetches the current participant roster for [billId] from Supabase and
     * merges it into local state - server rows first, then any purely-local
     * participant not yet reflected there (covers the brief window before a
     * background insert completes). This is what makes a join visible on
     * other devices/sessions, since each device otherwise only knows about
     * participants it added itself.
     */
    fun loadParticipants(billId: String) {
        viewModelScope.launch {
            runCatching {
                participantRepository.listByBillId(billId)
            }.onSuccess { rows ->
                val fetched = rows.map { it.toParticipant() }
                    .filterNot { it.id in _pendingRemovalIds.value }
                val localOnly = _participants.value.filterNot { local ->
                    fetched.any { it.id == local.id }
                }
                _participants.value = fetched + localOnly
            }.onFailure {
                Log.e("BillViewModel", "Failed to load participants", it)
            }
        }
    }

    private fun ParticipantRow.toParticipant() = Participant(
        id = id ?: UUID.randomUUID().toString(),
        name = name,
        isHost = role == "host",
        isReady = isReady,
        joinMethod = if (userId != null) JoinMethod.SELF_JOINED else JoinMethod.HOST_ADDED
    )

    /**
     * Adds [name] to the local participant list immediately (optimistic),
     * then writes the row through to Supabase in the background. A write
     * failure is logged but doesn't roll back the local state or surface an
     * error to the user - this is best-effort, unlike the bill save itself.
     */
    fun addParticipantAndPersist(
        billId: String,
        name: String,
        isHost: Boolean = false,
        joinMethod: JoinMethod = JoinMethod.HOST_ADDED,
        userId: String? = null
    ): Participant? {
        val trimmedName = name.trim()

        if (trimmedName.isBlank()) return null

        // Only guard against duplicate names when the host is manually typing
        // friends in - a self-join must never be silently dropped just
        // because its name happens to match an existing participant's.
        val alreadyExists =
            joinMethod == JoinMethod.HOST_ADDED &&
                _participants.value.any { participant ->
                    participant.name.equals(
                        trimmedName,
                        ignoreCase = true
                    )
                }

        if (alreadyExists) return null

        val participant = Participant(
            id = UUID.randomUUID().toString(),
            name = trimmedName,
            isHost = isHost,
            joinMethod = joinMethod
        )

        _participants.value += participant

        // A participant who joins themselves (via QR/code) is the one
        // driving this session, so this session now acts as them.
        if (joinMethod == JoinMethod.SELF_JOINED) {
            _currentParticipantId.value = participant.id
        }

        viewModelScope.launch {
            runCatching {
                participantRepository.insertParticipant(
                    ParticipantRow(
                        id = participant.id,
                        billId = billId,
                        userId = userId,
                        name = participant.name,
                        role = "member"
                    )
                )
            }.onFailure {
                Log.e("BillViewModel", "Failed to persist participant", it)
            }
        }

        return participant
    }

    fun removeParticipantAndPersist(participantId: String) {
        _participants.value =
            _participants.value.filterNot { participant ->
                participant.id == participantId
            }

        _pendingRemovalIds.value = _pendingRemovalIds.value + participantId

        viewModelScope.launch {
            runCatching {
                participantRepository.deleteParticipant(participantId)
            }.onFailure {
                Log.e("BillViewModel", "Failed to delete participant", it)
            }
            _pendingRemovalIds.value = _pendingRemovalIds.value - participantId
        }
    }

    /**
     * Creates the host's own participant row (optimistic local update, then
     * best-effort write-through to Supabase - see [addParticipantAndPersist]
     * for the same failure-handling rationale).
     */
    fun createHostAndPersist(billId: String, userId: String, displayName: String) {

        if (_participants.value.any {
                it.isHost
            }) {
            return
        }

        val host = Participant(
            id = UUID.randomUUID().toString(),
            name = displayName,
            isHost = true,
            joinMethod = JoinMethod.HOST_ADDED
        )

        _participants.value += host
        _currentParticipantId.value = host.id

        viewModelScope.launch {
            runCatching {
                participantRepository.insertParticipant(
                    ParticipantRow(
                        id = host.id,
                        billId = billId,
                        userId = userId,
                        name = host.name,
                        role = "host"
                    )
                )
            }.onFailure {
                Log.e("BillViewModel", "Failed to persist host participant", it)
            }
        }
    }

    /**
     * Whether [actingParticipantId] is allowed to change item selections
     * on behalf of [targetParticipantId]:
     * - Nobody can before the host has made the evenly-vs-by-item call, or
     *   once they've chosen to split evenly - see [chooseSplitEvenly]/
     *   [chooseSplitByItems].
     * - A participant who has already confirmed their split is locked,
     *   even from their own further edits or the host's.
     * - Anyone can manage their own selections.
     * - The host can additionally manage participants they added manually
     *   (those can't select for themselves since they never opened the app).
     */
    fun canControlParticipant(
        actingParticipantId: String?,
        targetParticipantId: String
    ): Boolean {

        if (!_bill.value.splitDecided) return false
        if (_bill.value.isSplitEvenly) return false
        if (actingParticipantId == null) return false

        val targetParticipant =
            _participants.value.find { it.id == targetParticipantId }
                ?: return false

        if (targetParticipant.isReady) return false
        if (actingParticipantId == targetParticipantId) return true

        val actingParticipant =
            _participants.value.find { it.id == actingParticipantId }
                ?: return false

        return actingParticipant.isHost &&
                targetParticipant.joinMethod == JoinMethod.HOST_ADDED
    }

    /**
     * Host-only: commits the one-time evenly-vs-by-item call for the bill,
     * unlocking [canControlParticipant]/item taps for everyone when the
     * answer is "by item". There's no path back to the undecided state -
     * see [Bill.splitDecided].
     */
    private fun setSplitDecision(billId: String, isSplitEvenly: Boolean) {
        _bill.value = _bill.value.copy(
            isSplitEvenly = isSplitEvenly,
            splitDecided = true
        )

        viewModelScope.launch {
            runCatching {
                billRepository.setSplitDecision(billId, isSplitEvenly)
            }.onFailure {
                Log.e("BillViewModel", "Failed to persist split decision", it)
            }
        }
    }

    /**
     * Host chose to split the bill evenly - there's nothing left to divide
     * up per-item, so this jumps straight to the payment stage for everyone
     * polling the bill.
     */
    fun chooseSplitEvenly(billId: String) {
        setSplitDecision(billId, isSplitEvenly = true)
        advanceStage(billId, BillStage.PAYMENT)
    }

    /** Host chose to split by item - unlocks item tapping for everyone. */
    fun chooseSplitByItems(billId: String) {
        setSplitDecision(billId, isSplitEvenly = false)
    }

    /**
     * Flips [participantId]'s own ready/confirmed flag - status-only, never
     * triggers navigation. Used both for "I'm ready to start splitting" in
     * the waiting room and "I've confirmed my split" on the Split screen.
     */
    fun toggleReady(participantId: String, billId: String) {
        val target = _participants.value.find { it.id == participantId } ?: return
        val next = !target.isReady

        _participants.value = _participants.value.map {
            if (it.id == participantId) it.copy(isReady = next) else it
        }

        viewModelScope.launch {
            runCatching {
                participantRepository.setReady(participantId, next)
            }.onFailure {
                Log.e("BillViewModel", "Failed to persist ready state", it)
            }
        }
    }

    /**
     * Clears this device's own leftover "I'm ready to start splitting" flag
     * right as the bill moves past the waiting room. Necessary because
     * [advanceStage] resets ready flags via cross-user writes to every
     * participant's row from whoever tapped Continue (normally the host) -
     * those can silently fail under a "users can only update their own row"
     * RLS policy, permanently tripping [canControlParticipant]'s
     * already-confirmed lock for that participant on the Split screen. This
     * is a self-row update instead, so it's never subject to that.
     */
    fun clearOwnReadyForNewStage(participantId: String?, billId: String) {
        val target = _participants.value.find { it.id == participantId } ?: return
        if (target.isReady) {
            toggleReady(participantId!!, billId)
        }
    }

    /**
     * Host-only action that moves the whole bill (and everyone polling it)
     * to [stage]. Resets every participant's ready/confirmed flag for the
     * new stage, both locally and best-effort in Supabase.
     */
    fun advanceStage(billId: String, stage: BillStage) {
        _bill.value = _bill.value.copy(stage = stage)
        _participants.value = _participants.value.map { it.copy(isReady = false) }

        viewModelScope.launch {
            runCatching {
                billRepository.updateStage(billId, stage)
            }.onFailure {
                Log.e("BillViewModel", "Failed to persist stage", it)
            }

            _participants.value.forEach { participant ->
                launch {
                    runCatching {
                        participantRepository.setReady(participant.id, false)
                    }.onFailure {
                        Log.e("BillViewModel", "Failed to reset ready for ${participant.id}", it)
                    }
                }
            }
        }
    }

    /**
     * Polls the bill's shared stage/split-evenly/PromptPay-QR fields from
     * Supabase - the fields the host can change that every other device
     * needs to react to live. Deliberately excludes items/charges/
     * restaurantName, which are locally authoritative once loaded.
     */
    fun pollBillState(billId: String) {
        viewModelScope.launch {
            runCatching {
                billRepository.findById(billId)
            }.onSuccess { row ->
                if (row != null) {
                    _bill.value = _bill.value.copy(
                        stage = BillStage.fromDb(row.status),
                        isSplitEvenly = row.isSplitEvenly,
                        splitDecided = row.splitDecided,
                        promptPayQrUrl = row.promptPayQrUrl
                    )
                }
            }.onFailure {
                Log.e("BillViewModel", "Failed to poll bill state", it)
            }
        }
    }

    /** Item price with this item's share of VAT/service charge folded in. */
    fun itemEffectivePrice(item: ReceiptItem): Double {

        val base = item.price * item.quantity
        val withService = base * (1 + _bill.value.serviceChargeRate)

        return if (_bill.value.isVatIncluded) {
            withService
        } else {
            withService * (1 + _bill.value.vatRate)
        }
    }

    private fun rawItemsSubtotalForParticipant(
        participantId: String
    ): Double {

        var total = 0.0

        _bill.value.items.forEach { item ->

            val selection =
                _itemSelections.value.find {
                    it.itemId == item.id
                }

            val selectedParticipants =
                selection?.participantIds
                    ?: emptySet()

            if (
                participantId in selectedParticipants &&
                selectedParticipants.isNotEmpty()
            ) {
                total += (item.price * item.quantity) /
                        selectedParticipants.size
            }
        }

        return total
    }

    private fun evenSplitTotal(): Double {
        return _bill.value.items.sumOf { itemEffectivePrice(it) }
    }

    private fun evenSplitPerPerson(): Double {
        val count = _participants.value.size
        return if (count > 0) evenSplitTotal() / count else 0.0
    }

    fun toggleItemSelection(
        itemId: String,
        participantId: String
    ) {

        val existingSelection =
            _itemSelections.value.find {
                it.itemId == itemId
            }

        val added: Boolean

        if (existingSelection == null) {

            _itemSelections.value += ItemSelection(
                itemId = itemId,
                participantIds = setOf(participantId)
            )

            added = true

        } else {

            val wasSelected = participantId in existingSelection.participantIds

            val updatedParticipants =
                if (wasSelected) {
                    existingSelection.participantIds - participantId
                } else {
                    existingSelection.participantIds + participantId
                }

            _itemSelections.value =
                _itemSelections.value.map {

                    if (it.itemId == itemId) {
                        it.copy(
                            participantIds = updatedParticipants
                        )
                    } else {
                        it
                    }
                }

            added = !wasSelected
        }

        viewModelScope.launch {
            runCatching {
                if (added) {
                    itemClaimRepository.claimItem(itemId, participantId)
                } else {
                    itemClaimRepository.unclaimItem(itemId, participantId)
                }
            }.onFailure {
                Log.e("BillViewModel", "Failed to persist item claim", it)
            }
        }
    }

    /**
     * Fetches item claims for the current bill's items and replaces
     * [itemSelections] wholesale - this is what makes item choices visible
     * across devices. A poll landing just before this device's own optimistic
     * [toggleItemSelection] update can briefly revert it until the next tick;
     * acceptable at the ~3s polling cadence used everywhere else in this flow.
     */
    fun loadItemClaims(billId: String) {
        if (_bill.value.id != billId) return

        val itemIds = _bill.value.items.map { it.id }
        if (itemIds.isEmpty()) return

        viewModelScope.launch {
            runCatching {
                itemClaimRepository.listClaimsForItems(itemIds)
            }.onSuccess { rows ->
                val grouped = rows.groupBy { it.itemId }
                    .mapValues { (_, claims) -> claims.map { it.participantId }.toSet() }

                val fetched = itemIds.map { id ->
                    ItemSelection(itemId = id, participantIds = grouped[id] ?: emptySet())
                }

                if (fetched != _itemSelections.value) {
                    _itemSelections.value = fetched
                }
            }.onFailure {
                Log.e("BillViewModel", "Failed to load item claims", it)
            }
        }
    }

    fun getSelectionForItem(
        itemId: String
    ): ItemSelection? {

        return _itemSelections.value.find {
            it.itemId == itemId
        }
    }

    fun calculateParticipantSubtotal(
        participantId: String
    ): Double {

        if (_bill.value.isSplitEvenly) {
            return evenSplitPerPerson()
        }

        var total = 0.0

        _bill.value.items.forEach {
                item ->

            val selection =
                _itemSelections.value.find {
                    it.itemId == item.id
                }

            val selectedParticipants =
                selection?.participantIds
                    ?: emptySet()

            if (
                participantId in
                selectedParticipants &&
                selectedParticipants.isNotEmpty()
            ) {

                total +=
                    itemEffectivePrice(item) /
                            selectedParticipants.size
            }
        }

        return total
    }

    fun hasUnclaimedItems(): Boolean {

        if (!_bill.value.splitDecided) return false
        if (_bill.value.isSplitEvenly) return false

        return _bill.value.items.any {
                item ->

            val selection = getSelectionForItem(item.id)

            selection == null ||
                    selection.participantIds
                        .isEmpty()
        }
    }

    fun updateCharges(
        serviceChargeRate: Double,
        vatRate: Double,
        discount: Double,
        isVatIncluded: Boolean
    ) {

        val result =
            ChargeCalculator.calculate(
                subtotal = _bill.value.subtotal,
                serviceChargeRate = serviceChargeRate,
                vatRate = vatRate,
                discount = discount,
                isVatIncluded = isVatIncluded
            )

        _bill.value =
            _bill.value.copy(
                serviceChargeRate =
                    serviceChargeRate,

                serviceChargeAmount =
                    result.serviceCharge,

                vatRate =
                    vatRate,

                vatAmount =
                    result.vat,

                discount =
                    result.discount,

                total =
                    result.total,

                isVatIncluded =
                    isVatIncluded
            )
    }

    fun calculateParticipantTotals():
            List<ParticipantTotal> {

        val billValue =
            _bill.value

        val subtotal =
            billValue.subtotal

        val count =
            _participants.value.size

        return _participants.value.map {
                participant ->

            // Raw (pre VAT/service) food cost, used only to work out this
            // participant's fair share of the discount below.
            val rawFoodSubtotal =
                rawItemsSubtotalForParticipant(
                    participant.id
                )

            val ratio =
                if (subtotal > 0) {
                    rawFoodSubtotal / subtotal
                } else {
                    0.0
                }

            val serviceShare =
                if (billValue.isSplitEvenly) {
                    if (count > 0) billValue.serviceChargeAmount / count else 0.0
                } else {
                    billValue.serviceChargeAmount * ratio
                }

            val vatShare =
                if (billValue.isSplitEvenly) {
                    if (count > 0) billValue.vatAmount / count else 0.0
                } else {
                    billValue.vatAmount * ratio
                }

            val discountShare =
                if (billValue.isSplitEvenly) {
                    if (count > 0) billValue.discount / count else 0.0
                } else {
                    billValue.discount * ratio
                }

            // Already includes this participant's share of VAT/service
            // charge (folded in per item), so the discount is the only
            // thing left to subtract here.
            val foodSubtotal =
                calculateParticipantSubtotal(
                    participant.id
                )

            val total =
                (foodSubtotal - discountShare)
                    .coerceAtLeast(0.0)

            ParticipantTotal(
                participantId =
                    participant.id,

                participantName =
                    participant.name,

                foodSubtotal =
                    rawFoodSubtotal,

                serviceCharge =
                    serviceShare,

                vat =
                    vatShare,

                discount =
                    discountShare,

                total =
                    total
            )
        }
    }
}