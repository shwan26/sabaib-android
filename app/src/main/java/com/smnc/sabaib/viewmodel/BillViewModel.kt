package com.smnc.sabaib.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.smnc.sabaib.domain.charges.ChargeCalculator
import com.smnc.sabaib.model.Bill
import com.smnc.sabaib.model.ItemSelection
import com.smnc.sabaib.model.Participant
import com.smnc.sabaib.model.ParticipantSplit
import com.smnc.sabaib.model.ParticipantTotal
import com.smnc.sabaib.model.ReceiptItem
import com.smnc.sabaib.util.generateGroupCode
import java.util.UUID

class BillViewModel : ViewModel() {

    private val _bill = mutableStateOf(
        Bill(
            id = System.currentTimeMillis()
                .toString(),

            code = generateGroupCode()
        )
    )

    val bill: State<Bill> = _bill

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

    fun addParticipant(
        name: String,
        isHost: Boolean = false
    ) {
        if (name.isBlank()) return

        val participant = Participant(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            isHost = isHost
        )

        _participants.value += participant
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
            isHost = true
        )

        _participants.value += host
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

                val itemTotal =
                    item.price * item.quantity

                total +=
                    itemTotal /
                            selectedParticipants.size
            }
        }

        return total
    }

    fun hasUnclaimedItems(): Boolean {

        return _bill.value.items.any {
                item ->

            val selection = getSelectionForItem(item.id)

            selection == null ||
                    selection.participantIds
                        .isEmpty()
        }
    }

    fun selectEveryoneForItem(
        itemId: String
    ) {

        val allParticipantIds =
            _participants.value
                .map { it.id }
                .toSet()

        val existing =
            _itemSelections.value.find {
                it.itemId == itemId
            }

        if (existing == null) {

            _itemSelections.value += ItemSelection(
                                        itemId = itemId,
                                        participantIds =
                                            allParticipantIds
                                    )

        } else {

            _itemSelections.value =
                _itemSelections.value.map {

                    if (it.itemId == itemId) {
                        it.copy(
                            participantIds =
                                allParticipantIds
                        )
                    } else {
                        it
                    }
                }
        }
    }

    fun clearItemSelection(
        itemId: String
    ) {
        _itemSelections.value =
            _itemSelections.value.map { selection ->

                if (selection.itemId == itemId) {
                    selection.copy(
                        participantIds = emptySet()
                    )
                } else {
                    selection
                }
            }
    }

    fun calculateParticipantSplits():
            List<ParticipantSplit> {

        return _participants.value.map {
                participant ->

            ParticipantSplit(
                participantId =
                    participant.id,

                participantName =
                    participant.name,

                subtotal =
                    calculateParticipantSubtotal(
                        participant.id
                    )
            )
        }
    }

    fun selectedItemsTotal(): Double {

        return _bill.value.items
            .filter { item ->

                val selection = getSelectionForItem(item.id)

                selection?.participantIds?.isNotEmpty() == true
            }
            .sumOf {
                it.price * it.quantity
            }
    }

    fun participantSubtotalTotal(): Double {

        return _participants.value
            .sumOf {
                calculateParticipantSubtotal(
                    it.id
                )
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

        return _participants.value.map {
                participant ->

            val foodSubtotal =
                calculateParticipantSubtotal(
                    participant.id
                )

            val ratio =
                if (subtotal > 0) {
                    foodSubtotal / subtotal
                } else {
                    0.0
                }

            val serviceShare =
                billValue.serviceChargeAmount *
                        ratio

            val vatShare =
                billValue.vatAmount *
                        ratio

            val discountShare =
                billValue.discount *
                        ratio

            val total =
                foodSubtotal +
                        serviceShare +
                        vatShare -
                        discountShare

            ParticipantTotal(
                participantId =
                    participant.id,

                participantName =
                    participant.name,

                foodSubtotal =
                    foodSubtotal,

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

    fun participantTotalsSum(): Double {

        return calculateParticipantTotals()
            .sumOf {
                it.total
            }
    }

    fun isFinalTotalBalanced(): Boolean {

        return kotlin.math.abs(
            participantTotalsSum() -
                    _bill.value.total
        ) < 0.01
    }


}