package com.deportal.payments.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.deportal.payments.model.PaymentCalculation;
import com.deportal.users.enums.CustomerType;
import java.math.BigDecimal;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class PaymentCalculatorTest {

    private final PaymentCalculator calculator = new PaymentCalculator();

    @Test
    void shouldCalculateBaseAmountWithoutDiscounts() {
        PaymentCalculation calculation = calculator.calculate(
                new BigDecimal("20.00"),
                2,
                CustomerType.NO_MIEMBRO,
                LocalTime.of(12, 0));

        assertThat(calculation.baseAmount()).isEqualByComparingTo("40.00");
        assertThat(calculation.memberDiscount()).isEqualByComparingTo("0.00");
        assertThat(calculation.offPeakDiscount()).isEqualByComparingTo("0.00");
        assertThat(calculation.totalDiscount()).isEqualByComparingTo("0.00");
        assertThat(calculation.totalAmount()).isEqualByComparingTo("40.00");
    }

    @Test
    void shouldApplyMemberDiscount() {
        PaymentCalculation calculation = calculator.calculate(
                new BigDecimal("20.00"),
                2,
                CustomerType.MIEMBRO,
                LocalTime.of(12, 0));

        assertThat(calculation.baseAmount()).isEqualByComparingTo("40.00");
        assertThat(calculation.memberDiscount()).isEqualByComparingTo("4.00");
        assertThat(calculation.totalAmount()).isEqualByComparingTo("36.00");
    }

    @Test
    void shouldApplyMorningOffPeakDiscount() {
        PaymentCalculation calculation = calculator.calculate(
                new BigDecimal("20.00"),
                2,
                CustomerType.NO_MIEMBRO,
                LocalTime.of(8, 0));

        assertThat(calculation.offPeakDiscount()).isEqualByComparingTo("8.00");
        assertThat(calculation.totalAmount()).isEqualByComparingTo("32.00");
    }

    @Test
    void shouldApplyNightOffPeakDiscountAfterSevenPm() {
        PaymentCalculation calculation = calculator.calculate(
                new BigDecimal("20.00"),
                2,
                CustomerType.NO_MIEMBRO,
                LocalTime.of(20, 0));

        assertThat(calculation.offPeakDiscount()).isEqualByComparingTo("8.00");
        assertThat(calculation.totalAmount()).isEqualByComparingTo("32.00");
    }

    @Test
    void shouldApplyAccumulatedDiscountsSequentiallyLikeStatementExample() {
        PaymentCalculation calculation = calculator.calculate(
                new BigDecimal("20.00"),
                2,
                CustomerType.MIEMBRO,
                LocalTime.of(20, 0));

        assertThat(calculation.baseAmount()).isEqualByComparingTo("40.00");
        assertThat(calculation.memberDiscount()).isEqualByComparingTo("4.00");
        assertThat(calculation.offPeakDiscount()).isEqualByComparingTo("7.20");
        assertThat(calculation.totalDiscount()).isEqualByComparingTo("11.20");
        assertThat(calculation.totalAmount()).isEqualByComparingTo("28.80");
    }
}
