package com.example.lifestylelevellingasianlifestyle;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherForecastTest {
    @Test
    void sortsDaysAndCalculatesIntervalsBetweenSunnyDays() {
        LocalDate firstDate = LocalDate.of(2026, 9, 12);
        WeatherDay firstSunny = sunny(firstDate);
        WeatherDay cloudy = cloudy(firstDate.plusDays(1));
        WeatherDay secondSunny = sunny(firstDate.plusDays(3));
        WeatherForecast forecast = new WeatherForecast(
                "Brisbane, Queensland, Australia",
                -27.47,
                153.03,
                "Australia/Brisbane",
                List.of(secondSunny, cloudy, firstSunny)
        );

        assertEquals(List.of(firstSunny, cloudy, secondSunny), forecast.getDays());
        assertEquals(List.of(firstSunny, secondSunny), forecast.getSunnyDays());
        WeatherForecast.SunnyDayInterval interval = forecast.getSunnyDayIntervals().get(0);
        assertEquals(3, interval.daysApart());
        assertEquals(2, interval.nonSunnyDaysBetween());
        assertTrue(forecast.getSunnyIntervalSummary().contains("3 days apart"));
        assertTrue(forecast.getSummary().contains("2 sunny"));
    }

    @Test
    void describesZeroOrOneSunnyDayWithoutInventingAnInterval() {
        LocalDate date = LocalDate.of(2026, 9, 12);
        WeatherForecast none = new WeatherForecast("Hobart", -42.88, 147.33,
                "Australia/Hobart", List.of(cloudy(date)));
        WeatherForecast one = new WeatherForecast("Hobart", -42.88, 147.33,
                "Australia/Hobart", List.of(sunny(date)));

        assertTrue(none.getSunnyIntervalSummary().startsWith("No qualifying"));
        assertTrue(one.getSunnyIntervalSummary().startsWith("One qualifying"));
        assertTrue(one.getSunnyDayIntervals().isEmpty());
    }

    @Test
    void rejectsDuplicateDatesAndBadLocationData() {
        WeatherDay day = sunny(LocalDate.of(2026, 9, 12));
        assertThrows(IllegalArgumentException.class,
                () -> new WeatherForecast("Brisbane", -27, 153, "Australia/Brisbane", List.of(day, day)));
        assertThrows(IllegalArgumentException.class,
                () -> new WeatherForecast("", -27, 153, "Australia/Brisbane", List.of(day)));
        assertThrows(IllegalArgumentException.class,
                () -> new WeatherForecast("Brisbane", -91, 153, "Australia/Brisbane", List.of(day)));
    }

    private static WeatherDay sunny(LocalDate date) {
        return new WeatherDay(date, 15, 25, 0, 0, 10, 7, 20, 4);
    }

    private static WeatherDay cloudy(LocalDate date) {
        return new WeatherDay(date, 15, 23, 3, 0, 15, 2, 15, 3);
    }
}
