package com.smnc.sabaib.model

data class ItemSelection(
    val itemId: String,
    val participantIds: Set<String> = emptySet()
)
