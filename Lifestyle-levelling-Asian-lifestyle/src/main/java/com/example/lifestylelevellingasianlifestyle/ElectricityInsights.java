package com.example.lifestylelevellingasianlifestyle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Immutable summary calculated from a user's monthly electricity records.
 */
public final class ElectricityInsights {
    public enum Trend {
        NO_DATA,
        INSUFFICIENT_DATA,
        INCREASED,
        DECREASED,
        STABLE,
        UNDEFINED_ZERO_BASELINE
    }

    private static final BigDecimal TEN_PERCENT_REDUCTION = new BigDecimal("0.90");

    private final Optional<YearMonth> latestMonth;
    private final Optional<BigDecimal> latestKwh;
    private final Optional<BigDecimal> latestMonthChangePercent;
    private final BigDecimal averageKwh;
    private final BigDecimal nextMonthTargetKwh;
    private final Trend trend;
    private final List<String> recommendations;

    private ElectricityInsights(
            Optional<YearMonth> latestMonth,
            Optional<BigDecimal> latestKwh,
            Optional<BigDecimal> latestMonthChangePercent,
            BigDecimal averageKwh,
            BigDecimal nextMonthTargetKwh,
            Trend trend,
            List<String> recommendations
    ) {
        this.latestMonth = latestMonth;
        this.latestKwh = latestKwh;
        this.latestMonthChangePercent = latestMonthChangePercent;
        this.averageKwh = averageKwh;
        this.nextMonthTargetKwh = nextMonthTargetKwh;
        this.trend = trend;
        this.recommendations = List.copyOf(recommendations);
    }

    /**
     * Calculates insights without mutating the supplied list.
     *
     * <p>Records are ordered by billing month, so callers may provide them in any order.
     * If duplicate months are supplied, the last value in the input list wins, matching the
     * service's database upsert semantics.</p>
     */
    public static ElectricityInsights calculate(List<ElectricityRecord> records) {
        Objects.requireNonNull(records, "records");

        TreeMap<YearMonth, ElectricityRecord> byMonth = new TreeMap<>();
        for (ElectricityRecord record : records) {
            ElectricityRecord nonNullRecord = Objects.requireNonNull(record, "records must not contain null");
            byMonth.put(nonNullRecord.getBillingMonth(), nonNullRecord);
        }

        if (byMonth.isEmpty()) {
            return new ElectricityInsights(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    Trend.NO_DATA,
                    List.of("Add a monthly electricity bill to start tracking usage.")
            );
        }

        List<ElectricityRecord> ordered = new ArrayList<>(byMonth.values());
        ordered.sort(Comparator.comparing(ElectricityRecord::getBillingMonth));

        BigDecimal total = ordered.stream()
                .map(ElectricityRecord::getKwh)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = rounded(total.divide(
                BigDecimal.valueOf(ordered.size()),
                8,
                RoundingMode.HALF_UP
        ));

        ElectricityRecord latest = ordered.get(ordered.size() - 1);
        BigDecimal target = rounded(latest.getKwh().multiply(TEN_PERCENT_REDUCTION));
        Optional<BigDecimal> change = Optional.empty();
        Trend trend;
        List<String> recommendations = new ArrayList<>();

        if (ordered.size() == 1) {
            trend = Trend.INSUFFICIENT_DATA;
            recommendations.add("Add another month to unlock a month-over-month comparison.");
        } else {
            ElectricityRecord previous = ordered.get(ordered.size() - 2);
            if (previous.getKwh().signum() == 0) {
                if (latest.getKwh().signum() == 0) {
                    change = Optional.of(BigDecimal.ZERO);
                    trend = Trend.STABLE;
                    recommendations.add("Usage remains at 0 kWh; check that both bills were entered correctly.");
                } else {
                    trend = Trend.UNDEFINED_ZERO_BASELINE;
                    recommendations.add("A percentage change cannot be calculated because the previous month was 0 kWh.");
                }
            } else {
                BigDecimal percentage = rounded(
                        latest.getKwh()
                                .subtract(previous.getKwh())
                                .divide(previous.getKwh(), 8, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                );
                change = Optional.of(percentage);
                int direction = percentage.compareTo(BigDecimal.ZERO);
                if (direction > 0) {
                    trend = Trend.INCREASED;
                    recommendations.add("Usage rose by " + display(percentage)
                            + "%. Check heating or cooling settings and switch off standby devices.");
                } else if (direction < 0) {
                    trend = Trend.DECREASED;
                    recommendations.add("Usage fell by " + display(percentage.abs())
                            + "%. Keep the habits that worked this month.");
                } else {
                    trend = Trend.STABLE;
                    recommendations.add("Usage is unchanged. Try line-drying on sunny days and turn devices off at the wall.");
                }
            }
        }

        if (latest.getKwh().signum() == 0) {
            recommendations.add("The latest usage is 0 kWh, so the reduction target stays at 0 kWh; verify the bill value.");
        } else {
            recommendations.add("Aim for " + display(target) + " kWh next month, 10% below the latest month.");
        }

        return new ElectricityInsights(
                Optional.of(latest.getBillingMonth()),
                Optional.of(latest.getKwh()),
                change,
                average,
                target,
                trend,
                recommendations
        );
    }

    private static BigDecimal rounded(BigDecimal value) {
        BigDecimal rounded = value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        return rounded.signum() == 0 ? BigDecimal.ZERO : rounded;
    }

    private static String display(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    public Optional<YearMonth> getLatestMonth() {
        return latestMonth;
    }

    public Optional<BigDecimal> getLatestKwh() {
        return latestKwh;
    }

    /**
     * Empty when there is no preceding month or the preceding month is zero and the latest is not.
     */
    public Optional<BigDecimal> getLatestMonthChangePercent() {
        return latestMonthChangePercent;
    }

    public BigDecimal getAverageKwh() {
        return averageKwh;
    }

    public BigDecimal getNextMonthTargetKwh() {
        return nextMonthTargetKwh;
    }

    public Trend getTrend() {
        return trend;
    }

    public List<String> getRecommendations() {
        return Collections.unmodifiableList(recommendations);
    }
}
