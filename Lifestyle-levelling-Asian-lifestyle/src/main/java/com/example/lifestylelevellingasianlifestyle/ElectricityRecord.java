package com.example.lifestylelevellingasianlifestyle;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

/**
 * One monthly electricity bill belonging to a single user.
 *
 * <p>An id of {@value #UNSAVED_ID} represents a value that has not been persisted yet.
 * kWh and bill amount are represented as {@link BigDecimal} so calculations do not gain
 * binary floating-point rounding noise. This value object accepts zero kWh so analytics can
 * safely describe imported or incomplete data; {@link ElectricityService} requires persisted
 * kWh to be greater than zero to match the database constraint.</p>
 */
public final class ElectricityRecord {
    public static final long UNSAVED_ID = 0L;

    private final long id;
    private final long userId;
    private final YearMonth billingMonth;
    private final BigDecimal kwh;
    private final BigDecimal billAmount;

    public ElectricityRecord(
            long id,
            long userId,
            YearMonth billingMonth,
            BigDecimal kwh,
            BigDecimal billAmount
    ) {
        if (id < UNSAVED_ID) {
            throw new IllegalArgumentException("id must be zero (unsaved) or positive");
        }
        if (userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }

        this.id = id;
        this.userId = userId;
        this.billingMonth = Objects.requireNonNull(billingMonth, "billingMonth");
        this.kwh = nonNegative(kwh, "kwh");
        this.billAmount = nonNegative(billAmount, "billAmount");
    }

    public ElectricityRecord(
            long userId,
            YearMonth billingMonth,
            BigDecimal kwh,
            BigDecimal billAmount
    ) {
        this(UNSAVED_ID, userId, billingMonth, kwh, billAmount);
    }

    private static BigDecimal nonNegative(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value.signum() == 0 ? BigDecimal.ZERO : value.stripTrailingZeros();
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public YearMonth getBillingMonth() {
        return billingMonth;
    }

    public BigDecimal getKwh() {
        return kwh;
    }

    public BigDecimal getBillAmount() {
        return billAmount;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ElectricityRecord that)) {
            return false;
        }
        return id == that.id
                && userId == that.userId
                && billingMonth.equals(that.billingMonth)
                && kwh.equals(that.kwh)
                && billAmount.equals(that.billAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, billingMonth, kwh, billAmount);
    }

    @Override
    public String toString() {
        return "ElectricityRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", billingMonth=" + billingMonth +
                ", kwh=" + kwh +
                ", billAmount=" + billAmount +
                '}';
    }
}
