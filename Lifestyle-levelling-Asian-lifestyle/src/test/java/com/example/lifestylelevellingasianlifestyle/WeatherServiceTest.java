package com.example.lifestylelevellingasianlifestyle;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherServiceTest {
    private static final String LOCATION_JSON = """
            {"results":[{"name":"Brisbane","latitude":-27.47,"longitude":153.03,
            "timezone":"Australia/Brisbane","admin1":"Queensland","country":"Australia"}]}
            """;

    private static final String FORECAST_JSON = """
            {"daily":{
              "time":["2026-09-12","2026-09-13"],
              "weather_code":[0,61],
              "temperature_2m_max":[25.5,21.0],
              "temperature_2m_min":[15.0,14.0],
              "precipitation_sum":[0.0,8.5],
              "precipitation_probability_max":[10,90],
              "sunshine_duration":[25200,1800],
              "wind_speed_10m_max":[18.0,30.0],
              "uv_index_max":[5.0,2.0]
            }}
            """;

    @Test
    void resolvesACityAndParsesForecastWithoutLiveNetwork() throws Exception {
        List<URI> requestedUris = new ArrayList<>();
        WeatherService service = new WeatherService((uri, name) -> {
            requestedUris.add(uri);
            return name.equals("city search") ? LOCATION_JSON : FORECAST_JSON;
        }, Locale.ENGLISH);

        WeatherForecast forecast = service.getSevenDayForecast("  Brisbane CBD  ");

        assertEquals("Brisbane, Queensland, Australia", forecast.getLocationName());
        assertEquals(2, forecast.getDays().size());
        assertTrue(forecast.getDays().get(0).isLaundrySuitable());
        assertEquals(7.0, forecast.getDays().get(0).getSunshineHours());
        assertTrue(requestedUris.get(0).toString().contains("Brisbane+CBD"));
        assertTrue(requestedUris.get(1).toString().contains("forecast_days=7"));
        assertTrue(requestedUris.get(1).toString().contains("Australia%2FBrisbane"));
    }

    @Test
    void rejectsMissingCitiesMalformedJsonAndMismatchedArrays() {
        assertThrows(WeatherService.WeatherServiceException.class,
                () -> WeatherService.parseLocationJson("{\"results\":[]}", "Nowhere"));
        assertThrows(WeatherService.WeatherServiceException.class,
                () -> WeatherService.parseLocationJson("not-json", "Nowhere"));

        String mismatched = FORECAST_JSON.replace(
                "\"weather_code\":[0,61]", "\"weather_code\":[0]");
        assertThrows(WeatherService.WeatherServiceException.class,
                () -> WeatherService.parseForecastJson(mismatched, "Brisbane", -27, 153,
                        "Australia/Brisbane"));
    }

    @Test
    void validatesCityAndCoordinateInputsBeforeRequesting() {
        WeatherService service = new WeatherService((uri, name) -> "{}", Locale.ENGLISH);

        assertThrows(IllegalArgumentException.class, () -> service.searchCity("   "));
        assertThrows(IllegalArgumentException.class,
                () -> WeatherService.buildForecastUri(100, 153, "Australia/Brisbane"));
    }
}
