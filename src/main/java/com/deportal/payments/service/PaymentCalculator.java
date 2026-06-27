package com.deportal.payments.service;

import com.deportal.payments.model.PaymentCalculation;
import com.deportal.users.enums.CustomerType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import org.springframework.stereotype.Component;

@Component
public class PaymentCalculator {

    private static final BigDecimal MEMBER_DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal OFF_PEAK_DISCOUNT_RATE = new BigDecimal("0.20");
    private static final BigDecimal MAX_DISCOUNT_RATE = new BigDecimal("0.30");
    private static final LocalTime OFF_PEAK_MORNING_LIMIT = LocalTime.of(10, 0);
    private static final LocalTime OFF_PEAK_NIGHT_LIMIT = LocalTime.of(19, 0);

    public PaymentCalculation calculate(
            BigDecimal hourlyRate,
            int durationHours,
            CustomerType customerType,
            LocalTime startTime) {
        BigDecimal baseAmount = money(hourlyRate.multiply(BigDecimal.valueOf(durationHours)));
        BigDecimal currentAmount = baseAmount;
        BigDecimal memberDiscount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        if (customerType == CustomerType.MIEMBRO) {
            memberDiscount = money(currentAmount.multiply(MEMBER_DISCOUNT_RATE));
            currentAmount = currentAmount.subtract(memberDiscount);
        }

        BigDecimal offPeakDiscount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        if (isOffPeak(startTime)) {
            offPeakDiscount = money(currentAmount.multiply(OFF_PEAK_DISCOUNT_RATE));
            currentAmount = currentAmount.subtract(offPeakDiscount);
        }

        BigDecimal totalDiscount = memberDiscount.add(offPeakDiscount);
        BigDecimal maxDiscount = money(baseAmount.multiply(MAX_DISCOUNT_RATE));

        if (totalDiscount.compareTo(maxDiscount) > 0) {
            totalDiscount = maxDiscount;
            currentAmount = baseAmount.subtract(maxDiscount);
        }

        return new PaymentCalculation(
                baseAmount,
                memberDiscount,
                offPeakDiscount,
                money(totalDiscount),
                money(currentAmount));
    }

    private boolean isOffPeak(LocalTime startTime) {
        return startTime.isBefore(OFF_PEAK_MORNING_LIMIT) || startTime.isAfter(OFF_PEAK_NIGHT_LIMIT);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
