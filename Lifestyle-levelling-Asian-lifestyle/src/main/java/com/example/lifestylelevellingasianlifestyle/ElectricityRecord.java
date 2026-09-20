package com.example.lifestylelevellingasianlifestyle;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Represents one monthly electricity bill for a user.
 */
public class ElectricityRecord {
    private static final DateTimeFormatter DISPLAY_MONTH =
            DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    private final int recordId;
    private final int userId;
    private final YearMonth billingMonth;
    private final double usageKwh;
    private final double billAmount;

    public ElectricityRecord(
            int recordId,
            int userId,
            YearMonth billingMonth,
            double usageKwh,
            double billAmount
    ) {
        this.recordId = recordId;
        this.userId = userId;
        this.billingMonth = billingMonth;
        this.usageKwh = usageKwh;
        this.billAmount = billAmount;
    }

    public int getRecordId() {
        return recordId;
    }

    public int getUserId() {
        return userId;
    }

    public YearMonth getBillingMonth() {
        return billingMonth;
    }

    public String getDisplayMonth() {
        return billingMonth.format(DISPLAY_MONTH);
    }

    public double getUsageKwh() {
        return usageKwh;
    }

    public double getBillAmount() {
        return billAmount;
    }

    public double getCostPerKwh() {
        if (usageKwh == 0) {
            return 0;
        }
        return billAmount / usageKwh;
    }
}
