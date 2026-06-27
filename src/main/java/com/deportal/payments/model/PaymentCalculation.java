package com.deportal.payments.model;

import java.math.BigDecimal;

public record PaymentCalculation(
        BigDecimal baseAmount,
        BigDecimal memberDiscount,
        BigDecimal offPeakDiscount,
        BigDecimal totalDiscount,
        BigDecimal totalAmount) {
}
