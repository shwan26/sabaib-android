package com.smnc.sabaib.domain.charges

data class ChargeResult(
    val subtotal: Double,
    val serviceCharge: Double,
    val vat: Double,
    val discount: Double,
    val total: Double
)

object ChargeCalculator {

    fun calculate(
        subtotal: Double,
        serviceChargeRate: Double,
        vatRate: Double,
        discount: Double,
        isVatIncluded: Boolean
    ): ChargeResult {

        val serviceCharge =
            subtotal * serviceChargeRate

        val beforeVat =
            subtotal + serviceCharge

        val vat =
            if (isVatIncluded) {
                0.0
            } else {
                beforeVat * vatRate
            }

        val total =
            beforeVat +
                    vat -
                    discount

        return ChargeResult(
            subtotal = subtotal,
            serviceCharge = serviceCharge,
            vat = vat,
            discount = discount,
            total = total.coerceAtLeast(0.0)
        )
    }
}