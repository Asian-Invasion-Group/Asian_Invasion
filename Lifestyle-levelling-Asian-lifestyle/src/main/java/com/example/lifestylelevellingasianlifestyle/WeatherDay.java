package com.example.lifestylelevellingasianlifestyle;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

/** Immutable weather metrics and sustainability recommendations for one day. */
public final class WeatherDay {
    private static final double MAX_LAUNDRY_RAIN_MM = 0.5;
    private static final int MAX_LAUNDRY_RAIN_CHANCE = 30;
    private static final double MIN_LAUNDRY_SUNSHINE_HOURS = 3.0;
    private static final double MAX_LAUNDRY_WIND_KMH = 35.0;
    private static final double MIN_LAUNDRY_TEMPERATURE_C = 12.0;

    private static final double MAX_WALK_RAIN_MM = 5.0;
    private static final int MAX_WALK_RAIN_CHANCE = 60;
    private static final double MAX_WALK_WIND_KMH = 45.0;
    private static final double MIN_WALK_TEMPERATURE_C = 5.0;
    private static final double MAX_WALK_MIN_TEMPERATURE_C = 32.0;

    private final LocalDate date;
    private final double minTemperatureC;
    private final double maxTemperatureC;
    private final int weatherCode;
    private final double rainSumMm;
    private final int precipitationProbabilityMaxPercent;
    private final double sunshineHours;
    private final double maxWindSpeedKmh;
    private final double maxUvIndex;

    public WeatherDay(
            LocalDate date,
            double minTemperatureC,
            double maxTemperatureC,
            int weatherCode,
            double rainSumMm,
            int precipitationProbabilityMaxPercent,
            double sunshineHours,
            double maxWindSpeedKmh,
            double maxUvIndex
    ) {
        this.date = Objects.requireNonNull(date, "date");
        this.minTemperatureC = finite(minTemperatureC, "minTemperatureC");
        this.maxTemperatureC = finite(maxTemperatureC, "maxTemperatureC");
        if (maxTemperatureC < minTemperatureC) {
            throw new IllegalArgumentException(
                    "maxTemperatureC must be greater than or equal to minTemperatureC");
        }
        if (weatherCode < 0 || weatherCode > 99) {
            throw new IllegalArgumentException("weatherCode must be between 0 and 99");
        }
        this.weatherCode = weatherCode;
        this.rainSumMm = nonNegative(rainSumMm, "rainSumMm");
        if (precipitationProbabilityMaxPercent < 0
                || precipitationProbabilityMaxPercent > 100) {
            throw new IllegalArgumentException(
                    "precipitationProbabilityMaxPercent must be between 0 and 100");
        }
        this.precipitationProbabilityMaxPercent = precipitationProbabilityMaxPercent;
        this.sunshineHours = range(sunshineHours, 0.0, 24.0, "sunshineHours");
        this.maxWindSpeedKmh = nonNegative(maxWindSpeedKmh, "maxWindSpeedKmh");
        this.maxUvIndex = nonNegative(maxUvIndex, "maxUvIndex");
    }

    public LocalDate getDate() {
        return date;
    }

    public double getMinTemperatureC() {
        return minTemperatureC;
    }

    public double getMaxTemperatureC() {
        return maxTemperatureC;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public double getRainSumMm() {
        return rainSumMm;
    }

    public int getPrecipitationProbabilityMaxPercent() {
        return precipitationProbabilityMaxPercent;
    }

    public double getSunshineHours() {
        return sunshineHours;
    }

    public double getMaxWindSpeedKmh() {
        return maxWindSpeedKmh;
    }

    public double getMaxUvIndex() {
        return maxUvIndex;
    }

    public boolean isSunny() {
        return weatherCode <= 1
                && rainSumMm <= 0.2
                && precipitationProbabilityMaxPercent <= 20
                && sunshineHours >= 4.0;
    }

    public boolean sunny() {
        return isSunny();
    }

    public boolean isLaundrySuitable() {
        return rainSumMm <= MAX_LAUNDRY_RAIN_MM
                && precipitationProbabilityMaxPercent <= MAX_LAUNDRY_RAIN_CHANCE
                && !isWetWeatherCode(weatherCode)
                && sunshineHours >= MIN_LAUNDRY_SUNSHINE_HOURS
                && maxWindSpeedKmh <= MAX_LAUNDRY_WIND_KMH
                && maxTemperatureC >= MIN_LAUNDRY_TEMPERATURE_C;
    }

    public boolean laundrySuitable() {
        return isLaundrySuitable();
    }

    public boolean isWalkSuitable() {
        return rainSumMm <= MAX_WALK_RAIN_MM
                && precipitationProbabilityMaxPercent <= MAX_WALK_RAIN_CHANCE
                && !isDangerousWeatherCode(weatherCode)
                && maxWindSpeedKmh <= MAX_WALK_WIND_KMH
                && maxTemperatureC >= MIN_WALK_TEMPERATURE_C
                && minTemperatureC <= MAX_WALK_MIN_TEMPERATURE_C;
    }

    public boolean walkSuitable() {
        return isWalkSuitable();
    }

    public String getSunnyReason() {
        if (isSunny()) {
            return format("Clear weather with %.1f hours of sunshine and only %d%% rain chance.",
                    sunshineHours, precipitationProbabilityMaxPercent);
        }
        if (weatherCode > 1) {
            return "The forecast is " + getWeatherDescription().toLowerCase(Locale.ROOT)
                    + ", rather than clear or mainly clear.";
        }
        if (rainSumMm > 0.2 || precipitationProbabilityMaxPercent > 20) {
            return format("Rain risk is too high (%.1f mm, %d%% chance) for a sunny-day rating.",
                    rainSumMm, precipitationProbabilityMaxPercent);
        }
        return format("Only %.1f hours of sunshine are forecast; at least 4 hours are needed.",
                sunshineHours);
    }

    public String getLaundryReason() {
        if (rainSumMm > MAX_LAUNDRY_RAIN_MM
                || precipitationProbabilityMaxPercent > MAX_LAUNDRY_RAIN_CHANCE
                || isWetWeatherCode(weatherCode)) {
            return format("Outdoor drying is not recommended: %.1f mm rain and a %d%% rain chance are forecast.",
                    rainSumMm, precipitationProbabilityMaxPercent);
        }
        if (sunshineHours < MIN_LAUNDRY_SUNSHINE_HOURS) {
            return format("Outdoor drying is not recommended: only %.1f hours of sunshine are forecast.",
                    sunshineHours);
        }
        if (maxWindSpeedKmh > MAX_LAUNDRY_WIND_KMH) {
            return format("Outdoor drying is not recommended: wind may reach %.1f km/h.",
                    maxWindSpeedKmh);
        }
        if (maxTemperatureC < MIN_LAUNDRY_TEMPERATURE_C) {
            return format("Outdoor drying may be slow because the high is only %.1f°C.",
                    maxTemperatureC);
        }
        return format("Good drying conditions: %.1f hours of sunshine, %d%% rain chance, and %.1f km/h maximum wind.",
                sunshineHours, precipitationProbabilityMaxPercent, maxWindSpeedKmh);
    }

    public String getWalkReason() {
        if (isDangerousWeatherCode(weatherCode)) {
            return "Walking is not recommended because "
                    + getWeatherDescription().toLowerCase(Locale.ROOT) + " is forecast.";
        }
        if (rainSumMm > MAX_WALK_RAIN_MM
                || precipitationProbabilityMaxPercent > MAX_WALK_RAIN_CHANCE) {
            return format("Walking is not recommended: %.1f mm rain and a %d%% rain chance are forecast.",
                    rainSumMm, precipitationProbabilityMaxPercent);
        }
        if (maxWindSpeedKmh > MAX_WALK_WIND_KMH) {
            return format("Walking is not recommended because wind may reach %.1f km/h.",
                    maxWindSpeedKmh);
        }
        if (maxTemperatureC < MIN_WALK_TEMPERATURE_C) {
            return format("Walking may be uncomfortable because the high is only %.1f°C.",
                    maxTemperatureC);
        }
        if (minTemperatureC > MAX_WALK_MIN_TEMPERATURE_C) {
            return format("Walking is not recommended because even the low is %.1f°C.",
                    minTemperatureC);
        }
        if (maxUvIndex >= 6.0) {
            return format("Suitable for an early or late walk; UV may reach %.1f, so use sun protection.",
                    maxUvIndex);
        }
        if (maxTemperatureC > 32.0) {
            return format("Suitable during cooler hours; temperatures may reach %.1f°C.",
                    maxTemperatureC);
        }
        return format("Good walking conditions: %.1f–%.1f°C, %d%% rain chance, and UV %.1f.",
                minTemperatureC,
                maxTemperatureC,
                precipitationProbabilityMaxPercent,
                maxUvIndex);
    }

    public String getWeatherDescription() {
        if (weatherCode == 0) return "Clear sky";
        if (weatherCode == 1) return "Mainly clear";
        if (weatherCode == 2) return "Partly cloudy";
        if (weatherCode == 3) return "Overcast";
        if (weatherCode == 45 || weatherCode == 48) return "Fog";
        if (weatherCode >= 51 && weatherCode <= 57) return "Drizzle";
        if (weatherCode >= 61 && weatherCode <= 67) return "Rain";
        if (weatherCode >= 71 && weatherCode <= 77) return "Snow";
        if (weatherCode >= 80 && weatherCode <= 82) return "Rain showers";
        if (weatherCode == 85 || weatherCode == 86) return "Snow showers";
        if (weatherCode >= 95) return "Thunderstorm";
        return "Mixed conditions";
    }

    private static boolean isWetWeatherCode(int code) {
        return (code >= 51 && code <= 86) || code >= 95;
    }

    private static boolean isDangerousWeatherCode(int code) {
        return (code >= 66 && code <= 77) || (code >= 85 && code <= 86) || code >= 95;
    }

    private static double finite(double value, String field) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(field + " must be finite");
        }
        return value;
    }

    private static double nonNegative(double value, String field) {
        finite(value, field);
        if (value < 0.0) {
            throw new IllegalArgumentException(field + " must not be negative");
        }
        return value;
    }

    private static double range(double value, double minimum, double maximum, String field) {
        finite(value, field);
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(
                    field + " must be between " + minimum + " and " + maximum);
        }
        return value;
    }

    private static String format(String template, Object... arguments) {
        return String.format(Locale.ROOT, template, arguments);
    }
}
