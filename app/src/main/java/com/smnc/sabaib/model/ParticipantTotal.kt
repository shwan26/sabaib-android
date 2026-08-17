package com.smnc.sabaib.model

data class ParticipantTotal(
    val participantId: String,
    val participantName: String,

    val foodSubtotal: Double,
    val serviceCharge: Double,
    val vat: Double,
    val discount: Double,

    val total: Double
)