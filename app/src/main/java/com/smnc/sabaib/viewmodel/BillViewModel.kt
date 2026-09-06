package com.smnc.sabaib.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smnc.sabaib.data.BillRepository
import com.smnc.sabaib.data.ProfileRepository
import com.smnc.sabaib.data.toUserMessage
import com.smnc.sabaib.domain.charges.ChargeCalculator
import com.smnc.sabaib.model.Bill
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
    private val profileRepository: ProfileRepository = ProfileRepository()
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

    private val _promptPayNumber =
        mutableStateOf<String?>(null)

    val promptPayNumber: State<String?> =
        _promptPayNumber

    private val _paidStatus =
        mutableStateOf<Map<String, Boolean>>(emptyMap())

    val paidStatus: State<Map<String, Boolean>> =
        _paidStatus

    fun updatePromptPayNumber(number: String) {
        _promptPayNumber.value = number
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
     * Persists the current bill (and its items) to Supabase, then records
     * a free scan against the owner's rolling 30-day quota. Losing the
     * scan-count update doesn't fail the whole operation - the bill itself
     * is what matters to the user.
     */
    fun saveBillAndProceed(ownerId: String) {
        _saveState.value = BillSaveState.Saving

        viewModelScope.launch {
            try {
                val row = billRepository.saveBill(ownerId, _bill.value)
                _bill.value = _bill.value.copy(id = row.id!!)

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

    fun addParticipant(
        name: String,
        isHost: Boolean = false,
        joinMethod: JoinMethod = JoinMethod.HOST_ADDED
    ): Participant? {
        val trimmedName = name.trim()

        if (trimmedName.isBlank()) return null

        val alreadyExists =
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

        return participant
    }

    fun removeParticipant(participantId: String) {
        _participants.value =
            _participants.value.filterNot { participant ->
                participant.id == participantId
            }
    }

    fun createHost(name: String) {

        if (_participants.value.any {
                it.isHost
            }) {
            return
        }

        val host = Participant(
            id = System.currentTimeMillis().toString(),
            name = name,
            isHost = true,
            joinMethod = JoinMethod.HOST_ADDED
        )

        _participants.value += host
        _currentParticipantId.value = host.id
    }

    /**
     * Whether [actingParticipantId] is allowed to change item selections
     * on behalf of [targetParticipantId]:
     * - Nobody can when the bill is split evenly.
     * - Anyone can manage their own selections.
     * - The host can additionally manage participants they added manually
     *   (those can't select for themselves since they never opened the app).
     */
    fun canControlParticipant(
        actingParticipantId: String?,
        targetParticipantId: String
    ): Boolean {

        if (_bill.value.isSplitEvenly) return false
        if (actingParticipantId == null) return false
        if (actingParticipantId == targetParticipantId) return true

        val actingParticipant =
            _participants.value.find { it.id == actingParticipantId }
                ?: return false

        val targetParticipant =
            _participants.value.find { it.id == targetParticipantId }
                ?: return false

        return actingParticipant.isHost &&
                targetParticipant.joinMethod == JoinMethod.HOST_ADDED
    }

    fun setSplitEvenly(isSplitEvenly: Boolean) {
        _bill.value = _bill.value.copy(
            isSplitEvenly = isSplitEvenly
        )
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

        if (existingSelection == null) {

            _itemSelections.value += ItemSelection(
                itemId = itemId,
                participantIds = setOf(participantId)
            )

        } else {

            val updatedParticipants =
                if (
                    participantId in existingSelection.participantIds
                ) {
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


    fun isValidGroupCode(code: String): Boolean {

        return _bill.value.code.equals(
            code.trim(),
            ignoreCase = true
        )
    }
}