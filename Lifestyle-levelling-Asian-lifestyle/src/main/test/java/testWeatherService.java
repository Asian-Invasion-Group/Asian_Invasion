
import com.example.lifestylelevellingasianlifestyle.WeatherService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
public class testWeatherService {

    WeatherService service;
    @BeforeEach
    public void setUp() {
        service = new WeatherService();
    }
    @Test
    public void testEmptyRecommendationsInput() {
        boolean success = false;
        String[] result = service.getRecommendations("");
        if (result.length > 0) {
            success = true;
        }
        assertFalse(success, "Invalid JSON should return nothing");
    }
    @Test
    public void testGetWeatherAPI() {
        service.getWeather(-27.46794, 153.02809);
    }
}
