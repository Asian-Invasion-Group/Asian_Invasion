package com.example.lifestylelevellingasianlifestyle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Retrieves and parses seven-day forecasts from Open-Meteo. */
public class WeatherService {
    private static final String GEOCODING_ENDPOINT =
            "https://geocoding-api.open-meteo.com/v1/search";
    private static final String FORECAST_ENDPOINT =
            "https://api.open-meteo.com/v1/forecast";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String DAILY_FIELDS = String.join(",",
            "weather_code",
            "temperature_2m_max",
            "temperature_2m_min",
            "precipitation_sum",
            "precipitation_probability_max",
            "sunshine_duration",
            "wind_speed_10m_max",
            "uv_index_max"
    );

    private final JsonRequester requester;
    private final Locale locale;

    /** Creates a service using the platform HTTP client and the user's locale. */
    public WeatherService() {
        this(HttpClient.newBuilder()
                        .connectTimeout(DEFAULT_TIMEOUT)
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build(),
                DEFAULT_TIMEOUT,
                Locale.getDefault());
    }

    /** Creates a service with an injectable, reusable HTTP client. */
    public WeatherService(HttpClient httpClient, Duration requestTimeout, Locale locale) {
        Objects.requireNonNull(httpClient, "httpClient");
        Objects.requireNonNull(requestTimeout, "requestTimeout");
        if (requestTimeout.isZero() || requestTimeout.isNegative()) {
            throw new IllegalArgumentException("requestTimeout must be positive");
        }
        this.requester = (uri, requestName) ->
                requestJson(httpClient, uri, requestTimeout, requestName);
        this.locale = Objects.requireNonNull(locale, "locale");
    }

    WeatherService(JsonRequester requester, Locale locale) {
        this.requester = Objects.requireNonNull(requester, "requester");
        this.locale = Objects.requireNonNull(locale, "locale");
    }

    /** Finds a city and returns its best Open-Meteo match. */
    public Location searchCity(String city) throws WeatherServiceException {
        String query = requireCity(city);
        URI uri = buildGeocodingUri(query, locale);
        String json = requester.get(uri, "city search");
        return parseLocationJson(json, query);
    }

    /** Finds {@code city}, then returns its local-time seven-day forecast. */
    public WeatherForecast getForecast(String city) throws WeatherServiceException {
        return getForecast(searchCity(city));
    }

    /** Readable alias that makes the forecast period explicit at call sites. */
    public WeatherForecast getSevenDayForecast(String city) throws WeatherServiceException {
        return getForecast(city);
    }

    /** Returns a local-time seven-day forecast for a resolved location. */
    public WeatherForecast getForecast(Location location) throws WeatherServiceException {
        Objects.requireNonNull(location, "location");
        URI uri = buildForecastUri(location.latitude(), location.longitude(), location.timezone());
        String json = requester.get(uri, "weather forecast");
        return parseForecastJson(
                json,
                location.displayName(),
                location.latitude(),
                location.longitude(),
                location.timezone()
        );
    }

    static URI buildGeocodingUri(String city, Locale locale) {
        String language = locale == null || locale.getLanguage().isBlank()
                ? "en"
                : locale.getLanguage().toLowerCase(Locale.ROOT);
        return URI.create(GEOCODING_ENDPOINT
                + "?name=" + encode(city)
                + "&count=1"
                + "&language=" + encode(language)
                + "&format=json");
    }

    static URI buildForecastUri(double latitude, double longitude, String timezone) {
        validateCoordinates(latitude, longitude);
        String zone = ZoneId.of(Objects.requireNonNull(timezone, "timezone")).getId();
        return URI.create(FORECAST_ENDPOINT
                + "?latitude=" + Double.toString(latitude)
                + "&longitude=" + Double.toString(longitude)
                + "&daily=" + encode(DAILY_FIELDS)
                + "&timezone=" + encode(zone)
                + "&forecast_days=7");
    }

    static Location parseLocationJson(String json, String originalQuery)
            throws WeatherServiceException {
        JsonNode root = readTree(json, "city-search");
        JsonNode results = root.path("results");
        if (!results.isArray() || results.isEmpty()) {
            throw new WeatherServiceException(
                    "No city was found for \"" + originalQuery
                            + "\". Try adding a state, region, or country.");
        }

        JsonNode result = results.get(0);
        String name = requiredText(result, "name", "city-search");
        double latitude = requiredDouble(result, "latitude", "city-search");
        double longitude = requiredDouble(result, "longitude", "city-search");
        String timezone = requiredText(result, "timezone", "city-search");
        try {
            validateCoordinates(latitude, longitude);
            ZoneId.of(timezone);
        } catch (IllegalArgumentException exception) {
            throw new WeatherServiceException(
                    "Open-Meteo returned an invalid location for \"" + originalQuery + "\".",
                    exception);
        }

        Set<String> nameParts = new LinkedHashSet<>();
        addIfPresent(nameParts, name);
        addIfPresent(nameParts, result.path("admin1").asText(null));
        addIfPresent(nameParts, result.path("country").asText(null));
        return new Location(String.join(", ", nameParts), latitude, longitude, timezone);
    }

    static WeatherForecast parseForecastJson(
            String json,
            String locationName,
            double latitude,
            double longitude,
            String timezone
    ) throws WeatherServiceException {
        JsonNode root = readTree(json, "forecast");
        JsonNode daily = root.path("daily");
        if (!daily.isObject()) {
            throw new WeatherServiceException(
                    "Open-Meteo forecast data is missing the daily forecast section.");
        }

        JsonNode dates = requiredArray(daily, "time", -1);
        if (dates.isEmpty()) {
            throw new WeatherServiceException("Open-Meteo returned an empty daily forecast.");
        }
        int dayCount = dates.size();

        JsonNode weatherCodes = requiredArray(daily, "weather_code", dayCount);
        JsonNode maxTemperatures = requiredArray(daily, "temperature_2m_max", dayCount);
        JsonNode minTemperatures = requiredArray(daily, "temperature_2m_min", dayCount);
        JsonNode rainSums = requiredArray(daily, "precipitation_sum", dayCount);
        JsonNode precipitationProbabilities = requiredArray(
                daily, "precipitation_probability_max", dayCount);
        JsonNode sunshineDurations = requiredArray(daily, "sunshine_duration", dayCount);
        JsonNode maxWindSpeeds = requiredArray(daily, "wind_speed_10m_max", dayCount);
        JsonNode maxUvIndexes = requiredArray(daily, "uv_index_max", dayCount);

        List<WeatherDay> days = new ArrayList<>(dayCount);
        for (int index = 0; index < dayCount; index++) {
            try {
                LocalDate date = LocalDate.parse(requiredArrayText(dates, "time", index));
                days.add(new WeatherDay(
                        date,
                        requiredArrayDouble(minTemperatures, "temperature_2m_min", index),
                        requiredArrayDouble(maxTemperatures, "temperature_2m_max", index),
                        requiredArrayInt(weatherCodes, "weather_code", index),
                        requiredArrayDouble(rainSums, "precipitation_sum", index),
                        requiredArrayInt(
                                precipitationProbabilities,
                                "precipitation_probability_max",
                                index),
                        requiredArrayDouble(sunshineDurations, "sunshine_duration", index)
                                / 3_600.0,
                        requiredArrayDouble(maxWindSpeeds, "wind_speed_10m_max", index),
                        requiredArrayDouble(maxUvIndexes, "uv_index_max", index)
                ));
            } catch (DateTimeParseException | IllegalArgumentException exception) {
                throw new WeatherServiceException(
                        "Open-Meteo returned invalid forecast values for day " + (index + 1) + ".",
                        exception);
            }
        }

        try {
            return new WeatherForecast(locationName, latitude, longitude, timezone, days);
        } catch (IllegalArgumentException exception) {
            throw new WeatherServiceException(
                    "Open-Meteo returned invalid forecast location data.", exception);
        }
    }

    private static String requestJson(
            HttpClient httpClient,
            URI uri,
            Duration requestTimeout,
            String requestName
    ) throws WeatherServiceException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(requestTimeout)
                .header("Accept", "application/json")
                .header("User-Agent", "Lifestyle-Levelling/1.0")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new WeatherServiceException(
                        "Open-Meteo " + requestName + " failed with HTTP "
                                + response.statusCode() + ". Please try again later.");
            }
            return response.body();
        } catch (HttpTimeoutException exception) {
            throw new WeatherServiceException(
                    "The Open-Meteo " + requestName
                            + " timed out. Check your connection and try again.",
                    exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new WeatherServiceException(
                    "The Open-Meteo " + requestName + " was interrupted.", exception);
        } catch (IOException exception) {
            throw new WeatherServiceException(
                    "Could not reach Open-Meteo for the " + requestName
                            + ". Check your internet connection and try again.",
                    exception);
        }
    }

    private static JsonNode readTree(String json, String responseName)
            throws WeatherServiceException {
        if (json == null || json.isBlank()) {
            throw new WeatherServiceException(
                    "Open-Meteo returned an empty " + responseName + " response.");
        }
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException exception) {
            throw new WeatherServiceException(
                    "Open-Meteo returned malformed " + responseName + " data.", exception);
        }
    }

    private static JsonNode requiredArray(JsonNode parent, String field, int expectedSize)
            throws WeatherServiceException {
        JsonNode value = parent.path(field);
        if (!value.isArray()) {
            throw new WeatherServiceException(
                    "Open-Meteo forecast data is missing daily." + field + ".");
        }
        if (expectedSize >= 0 && value.size() != expectedSize) {
            throw new WeatherServiceException(
                    "Open-Meteo forecast field daily." + field
                            + " contains " + value.size() + " values; expected "
                            + expectedSize + ".");
        }
        return value;
    }

    private static String requiredArrayText(JsonNode array, String field, int index)
            throws WeatherServiceException {
        JsonNode value = array.get(index);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw invalidArrayValue(field, index);
        }
        return value.asText();
    }

    private static double requiredArrayDouble(JsonNode array, String field, int index)
            throws WeatherServiceException {
        JsonNode value = array.get(index);
        if (value == null || !value.isNumber() || !Double.isFinite(value.asDouble())) {
            throw invalidArrayValue(field, index);
        }
        return value.asDouble();
    }

    private static int requiredArrayInt(JsonNode array, String field, int index)
            throws WeatherServiceException {
        JsonNode value = array.get(index);
        if (value == null || !value.isIntegralNumber()) {
            throw invalidArrayValue(field, index);
        }
        return value.asInt();
    }

    private static WeatherServiceException invalidArrayValue(String field, int index) {
        return new WeatherServiceException(
                "Open-Meteo forecast field daily." + field
                        + " has an invalid value for day " + (index + 1) + ".");
    }

    private static String requiredText(JsonNode node, String field, String responseName)
            throws WeatherServiceException {
        JsonNode value = node.path(field);
        if (!value.isTextual() || value.asText().isBlank()) {
            throw new WeatherServiceException(
                    "Open-Meteo " + responseName + " data is missing " + field + ".");
        }
        return value.asText();
    }

    private static double requiredDouble(JsonNode node, String field, String responseName)
            throws WeatherServiceException {
        JsonNode value = node.path(field);
        if (!value.isNumber() || !Double.isFinite(value.asDouble())) {
            throw new WeatherServiceException(
                    "Open-Meteo " + responseName + " data is missing " + field + ".");
        }
        return value.asDouble();
    }

    private static String requireCity(String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City must not be blank.");
        }
        return city.trim();
    }

    private static void validateCoordinates(double latitude, double longitude) {
        if (!Double.isFinite(latitude) || latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90.");
        }
        if (!Double.isFinite(longitude) || longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180.");
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static void addIfPresent(Set<String> parts, String value) {
        if (value != null && !value.isBlank()) {
            parts.add(value.trim());
        }
    }

    @FunctionalInterface
    interface JsonRequester {
        String get(URI uri, String requestName) throws WeatherServiceException;
    }

    /** A city result returned by Open-Meteo geocoding. */
    public record Location(
            String displayName,
            double latitude,
            double longitude,
            String timezone
    ) {
        public Location {
            if (displayName == null || displayName.isBlank()) {
                throw new IllegalArgumentException("displayName must not be blank");
            }
            validateCoordinates(latitude, longitude);
            ZoneId.of(Objects.requireNonNull(timezone, "timezone"));
        }
    }

    /** A user-facing failure from city lookup, HTTP transport, or forecast parsing. */
    public static class WeatherServiceException extends Exception {
        public WeatherServiceException(String message) {
            super(message);
        }

        public WeatherServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
