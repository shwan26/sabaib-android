package com.smnc.sabaib.domain.bill

data class Participant(
    val id: String,
    val name: String,
    val isHost: Boolean = false
)