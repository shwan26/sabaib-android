package com.smnc.sabaib.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.smnc.sabaib.model.Bill
import com.smnc.sabaib.model.Participant
import com.smnc.sabaib.model.ReceiptItem
import com.smnc.sabaib.util.generateGroupCode

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

    fun addParticipant(participant: Participant) {
        _participants.value =
            _participants.value + participant
    }

    fun removeParticipant(participantId: String) {
        _participants.value =
            _participants.value.filter {
                it.id != participantId
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

        _participants.value =
            _participants.value + host
    }
}