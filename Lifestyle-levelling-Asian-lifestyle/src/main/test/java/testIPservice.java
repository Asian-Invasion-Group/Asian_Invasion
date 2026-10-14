
import com.example.lifestylelevellingasianlifestyle.IPService;
import com.example.lifestylelevellingasianlifestyle.LocationData;
import com.example.lifestylelevellingasianlifestyle.WeatherService;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
public class testIPservice {
    IPService service;
    @BeforeEach
    public void setUp() {
        service = new IPService();
    }

    @Test
    public void testIPservice() throws IOException {
        boolean success = false;
        LocationData location = service.getIp();
        if (location.cityName().length() > 0) {
            success = true;
        }
        assertTrue(success, "Should return the testers Location based on their IP");
    }
}
