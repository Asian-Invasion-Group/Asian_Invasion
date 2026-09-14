package com.example.lifestylelevellingasianlifestyle;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElectricityInsightsTest {
    @Test
    void emptyHistoryProducesSafeZeroValues() {
        ElectricityInsights insights = ElectricityInsights.calculate(List.of());

        assertEquals(ElectricityInsights.Trend.NO_DATA, insights.getTrend());
        assertFalse(insights.getLatestMonth().isPresent());
        assertFalse(insights.getLatestMonthChangePercent().isPresent());
        assertDecimalEquals("0", insights.getAverageKwh());
        assertDecimalEquals("0", insights.getNextMonthTargetKwh());
        assertFalse(insights.getRecommendations().isEmpty());
    }

    @Test
    void singleRecordHasAverageAndTargetButNoComparison() {
        ElectricityInsights insights = ElectricityInsights.calculate(List.of(
                record(2026, 3, "120")
        ));

        assertEquals(ElectricityInsights.Trend.INSUFFICIENT_DATA, insights.getTrend());
        assertEquals(YearMonth.of(2026, 3), insights.getLatestMonth().orElseThrow());
        assertFalse(insights.getLatestMonthChangePercent().isPresent());
        assertDecimalEquals("120", insights.getAverageKwh());
        assertDecimalEquals("108", insights.getNextMonthTargetKwh());
    }

    @Test
    void unorderedInputUsesTheLatestTwoCalendarMonths() {
        ElectricityInsights insights = ElectricityInsights.calculate(List.of(
                record(2026, 3, "90"),
                record(2026, 1, "100"),
                record(2026, 2, "120")
        ));

        assertEquals(ElectricityInsights.Trend.DECREASED, insights.getTrend());
        assertEquals(YearMonth.of(2026, 3), insights.getLatestMonth().orElseThrow());
        assertDecimalEquals("-25", insights.getLatestMonthChangePercent().orElseThrow());
        assertDecimalEquals("103.33", insights.getAverageKwh());
        assertDecimalEquals("81", insights.getNextMonthTargetKwh());
        assertTrue(insights.getRecommendations().get(0).contains("fell by 25%"));
    }

    @Test
    void positiveUsageAfterZeroDoesNotDivideByZero() {
        ElectricityInsights insights = ElectricityInsights.calculate(List.of(
                record(2026, 1, "0"),
                record(2026, 2, "50")
        ));

        assertEquals(ElectricityInsights.Trend.UNDEFINED_ZERO_BASELINE, insights.getTrend());
        assertFalse(insights.getLatestMonthChangePercent().isPresent());
        assertDecimalEquals("25", insights.getAverageKwh());
        assertDecimalEquals("45", insights.getNextMonthTargetKwh());
        assertTrue(insights.getRecommendations().get(0).contains("previous month was 0 kWh"));
    }

    @Test
    void twoZeroMonthsAreReportedAsStable() {
        ElectricityInsights insights = ElectricityInsights.calculate(List.of(
                record(2026, 2, "0"),
                record(2026, 1, "0")
        ));

        assertEquals(ElectricityInsights.Trend.STABLE, insights.getTrend());
        assertDecimalEquals("0", insights.getLatestMonthChangePercent().orElseThrow());
        assertDecimalEquals("0", insights.getNextMonthTargetKwh());
    }

    private static ElectricityRecord record(int year, int month, String kwh) {
        return new ElectricityRecord(
                1,
                YearMonth.of(year, month),
                new BigDecimal(kwh),
                new BigDecimal("50")
        );
    }

    private static void assertDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
