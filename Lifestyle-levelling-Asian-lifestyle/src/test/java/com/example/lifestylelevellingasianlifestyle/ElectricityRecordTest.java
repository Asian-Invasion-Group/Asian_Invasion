package com.example.lifestylelevellingasianlifestyle;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ElectricityRecordTest {
    @Test
    void normalizesDecimalsAndSupportsAnUnsavedId() {
        ElectricityRecord record = new ElectricityRecord(
                7L,
                YearMonth.of(2026, 8),
                new BigDecimal("120.00"),
                new BigDecimal("45.500")
        );

        assertEquals(ElectricityRecord.UNSAVED_ID, record.getId());
        assertEquals(7L, record.getUserId());
        assertEquals(new BigDecimal("1.2E+2"), record.getKwh());
        assertEquals(new BigDecimal("45.5"), record.getBillAmount());
    }

    @Test
    void rejectsInvalidIdentityAndNegativeNumbers() {
        YearMonth month = YearMonth.of(2026, 8);

        assertThrows(IllegalArgumentException.class,
                () -> new ElectricityRecord(-1, 1, month, BigDecimal.ONE, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> new ElectricityRecord(0, month, BigDecimal.ONE, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> new ElectricityRecord(1, month, new BigDecimal("-0.01"), BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> new ElectricityRecord(1, month, BigDecimal.ONE, new BigDecimal("-0.01")));
    }
}
