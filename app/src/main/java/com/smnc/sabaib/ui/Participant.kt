package com.smnc.sabaib.ui

data class Participant(
    val id: String,
    val name: String,
    val isHost: Boolean = false
)