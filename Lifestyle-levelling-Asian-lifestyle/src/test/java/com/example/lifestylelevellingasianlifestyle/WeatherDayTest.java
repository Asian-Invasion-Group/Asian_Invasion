package com.example.lifestylelevellingasianlifestyle;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherDayTest {
    private static final LocalDate DATE = LocalDate.of(2026, 9, 12);

    @Test
    void recommendsSunnyLaundryAndWalkingConditions() {
        WeatherDay day = day(0, 0.0, 10, 8.0, 16, 25, 4);

        assertAll(
                () -> assertTrue(day.isSunny()),
                () -> assertTrue(day.isLaundrySuitable()),
                () -> assertTrue(day.isWalkSuitable()),
                () -> assertTrue(day.getSunnyReason().contains("8.0 hours")),
                () -> assertTrue(day.getLaundryReason().contains("Good drying")),
                () -> assertTrue(day.getWalkReason().contains("Good walking"))
        );
    }

    @Test
    void rejectsRainForLaundryAndThunderstormsForWalking() {
        WeatherDay rainy = day(61, 4.0, 80, 1.0, 15, 20, 3);
        WeatherDay storm = day(95, 0.0, 10, 7.0, 18, 25, 5);

        assertAll(
                () -> assertFalse(rainy.isSunny()),
                () -> assertFalse(rainy.isLaundrySuitable()),
                () -> assertTrue(rainy.getLaundryReason().contains("not recommended")),
                () -> assertFalse(storm.isWalkSuitable()),
                () -> assertTrue(storm.getWalkReason().contains("Thunderstorm".toLowerCase())
                        || storm.getWalkReason().contains("thunderstorm"))
        );
    }

    @Test
    void validatesWeatherRanges() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new WeatherDay(DATE, 25, 20, 0, 0, 0, 6, 10, 2)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new WeatherDay(DATE, 10, 20, 0, -1, 0, 6, 10, 2)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new WeatherDay(DATE, 10, 20, 0, 0, 101, 6, 10, 2)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new WeatherDay(DATE, 10, 20, 0, 0, 0, 25, 10, 2))
        );
    }

    private static WeatherDay day(
            int code, double rain, int rainChance, double sunHours,
            double minimum, double maximum, double uv
    ) {
        return new WeatherDay(DATE, minimum, maximum, code, rain, rainChance, sunHours, 20, uv);
    }
}
