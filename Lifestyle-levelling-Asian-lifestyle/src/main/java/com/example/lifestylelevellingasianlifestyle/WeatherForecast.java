package com.example.lifestylelevellingasianlifestyle;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * An immutable local-time forecast together with derived sunny-day intervals.
 */
public final class WeatherForecast {
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.ENGLISH);

    private final String locationName;
    private final double latitude;
    private final double longitude;
    private final String timezone;
    private final List<WeatherDay> days;
    private final List<WeatherDay> sunnyDays;
    private final List<SunnyDayInterval> sunnyDayIntervals;

    public WeatherForecast(
            String locationName,
            double latitude,
            double longitude,
            String timezone,
            List<WeatherDay> days
    ) {
        if (locationName == null || locationName.isBlank()) {
            throw new IllegalArgumentException("locationName must not be blank");
        }
        if (!Double.isFinite(latitude) || latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
        if (!Double.isFinite(longitude) || longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }
        ZoneId.of(Objects.requireNonNull(timezone, "timezone"));
        Objects.requireNonNull(days, "days");
        if (days.isEmpty()) {
            throw new IllegalArgumentException("days must not be empty");
        }

        List<WeatherDay> orderedDays = new ArrayList<>(days.size());
        Set<LocalDate> dates = new HashSet<>();
        for (WeatherDay day : days) {
            WeatherDay nonNullDay = Objects.requireNonNull(day, "days must not contain null");
            if (!dates.add(nonNullDay.getDate())) {
                throw new IllegalArgumentException("days must not contain duplicate dates");
            }
            orderedDays.add(nonNullDay);
        }
        orderedDays.sort(Comparator.comparing(WeatherDay::getDate));

        this.locationName = locationName.trim();
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezone = timezone;
        this.days = List.copyOf(orderedDays);
        this.sunnyDays = this.days.stream().filter(WeatherDay::isSunny).toList();
        this.sunnyDayIntervals = calculateSunnyIntervals(this.sunnyDays);
    }

    public String getLocationName() {
        return locationName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public List<WeatherDay> getDays() {
        return days;
    }

    public List<WeatherDay> getSunnyDays() {
        return sunnyDays;
    }

    /**
     * Returns the intervals between each pair of consecutive qualifying sunny days.
     */
    public List<SunnyDayInterval> getSunnyDayIntervals() {
        return sunnyDayIntervals;
    }

    /** A concise explanation intended for the Weather screen. */
    public String getSunnyIntervalSummary() {
        if (sunnyDays.isEmpty()) {
            return "No qualifying sunny day is forecast in the next 7 days.";
        }
        WeatherDay first = sunnyDays.get(0);
        if (sunnyDays.size() == 1) {
            return "One qualifying sunny day: " + DISPLAY_DATE.format(first.getDate())
                    + ". Another sunny day is needed to calculate an interval.";
        }

        StringBuilder summary = new StringBuilder()
                .append(sunnyDays.size())
                .append(" qualifying sunny days. Next: ")
                .append(DISPLAY_DATE.format(first.getDate()))
                .append(". Intervals: ");
        for (int index = 0; index < sunnyDayIntervals.size(); index++) {
            if (index > 0) {
                summary.append("; ");
            }
            SunnyDayInterval interval = sunnyDayIntervals.get(index);
            summary.append(DISPLAY_DATE.format(interval.from()))
                    .append(" → ")
                    .append(DISPLAY_DATE.format(interval.to()))
                    .append(": ")
                    .append(interval.daysApart())
                    .append(interval.daysApart() == 1 ? " day apart" : " days apart");
        }
        return summary.append('.').toString();
    }

    /** A one-line overview suitable for logs or a dashboard. */
    public String getSummary() {
        long dryingDays = days.stream().filter(WeatherDay::isLaundrySuitable).count();
        long walkingDays = days.stream().filter(WeatherDay::isWalkSuitable).count();
        return locationName + ": " + sunnyDays.size() + " sunny, " + dryingDays
                + " laundry-friendly, and " + walkingDays + " walking-friendly days.";
    }

    private static List<SunnyDayInterval> calculateSunnyIntervals(List<WeatherDay> sunnyDays) {
        List<SunnyDayInterval> intervals = new ArrayList<>();
        for (int index = 1; index < sunnyDays.size(); index++) {
            LocalDate from = sunnyDays.get(index - 1).getDate();
            LocalDate to = sunnyDays.get(index).getDate();
            intervals.add(new SunnyDayInterval(from, to, ChronoUnit.DAYS.between(from, to)));
        }
        return List.copyOf(intervals);
    }

    /**
     * Interval between consecutive sunny dates. {@code daysApart} is 1 for
     * consecutive calendar days; {@link #nonSunnyDaysBetween()} is 0 in that case.
     */
    public record SunnyDayInterval(LocalDate from, LocalDate to, long daysApart) {
        public SunnyDayInterval {
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(to, "to");
            if (!to.isAfter(from)) {
                throw new IllegalArgumentException("to must be after from");
            }
            long actualDays = ChronoUnit.DAYS.between(from, to);
            if (daysApart != actualDays) {
                throw new IllegalArgumentException("daysApart must match the supplied dates");
            }
        }

        public long nonSunnyDaysBetween() {
            return Math.max(0, daysApart - 1);
        }
    }
}
