package com.smnc.sabaib.model

enum class BillStage(val dbValue: String) {
    WAITING("waiting"),
    SPLITTING("splitting"),
    PAYMENT("settling");

    companion object {
        fun fromDb(value: String): BillStage =
            entries.find { it.dbValue == value } ?: WAITING
    }
}
