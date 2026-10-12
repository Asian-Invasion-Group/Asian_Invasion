
import com.example.lifestylelevellingasianlifestyle.AuthenticationService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestAuthenticationService {
    AuthenticationService service;
    private static final String EMAIL = "will@testing.com";
    private static final String PASSWORD = "Testing";
    @BeforeEach
    public void setUp() {
        service = new AuthenticationService();
        service.removeAccount(EMAIL, PASSWORD);
    }
    @AfterEach
    public void Remove() {
        service.removeAccount(EMAIL, PASSWORD);
    }

    @Test
    public void testSignupWithNewAccount() {
        boolean result = service.signUp(EMAIL, PASSWORD);
        assertTrue(result, "Signing up with a new account should succeed");
    }

    @Test
    public void testSignupWithExistingAccount() {
        service.signUp(EMAIL, PASSWORD);

        boolean result = service.signUp(EMAIL, PASSWORD);

        assertFalse(result, "Signing up with an existing email should fail");
    }

    @Test
    public void testSignupWithEmptyEmail() {
        boolean result = service.signUp("", PASSWORD);

        assertFalse(result, "Signing up with an empty email should fail");
    }

    @Test
    public void testSignupWithEmptyPassword() {
        boolean result = service.signUp(EMAIL, "");
        assertFalse(result, "Signing up with an empty password should fail");
    }

    @Test
    public void testSignup() {
        boolean result = service.signUp(EMAIL,PASSWORD);
        assertTrue(result);
    }

    // login tests

    @Test
    public void testLoginWithCorrectCredentials() {
        service.signUp(EMAIL, PASSWORD);

        int userId = service.login(EMAIL, PASSWORD);

        assertNotEquals(
                -1,
                userId,
                "Login with correct credentials should succeed"
        );
    }

    @Test
    public void testLoginWithIncorrectPassword() {
        service.signUp(EMAIL, PASSWORD);

        int userId = service.login(EMAIL, "WrongPassword123");

        assertEquals(
                -1,
                userId,
                "Login with an incorrect password should fail"
        );
    }

    @Test
    public void testLoginWithNonexistentAccount() {
        int userId = service.login("nonexistent@testing.com", PASSWORD);

        assertEquals(
                -1,
                userId,
                "Login with a nonexistent account should fail"
        );
    }

    @Test
    public void testLoginWithEmptyEmail() {
        int userId = service.login("", PASSWORD);

        assertEquals(
                -1,
                userId,
                "Login with an empty email should fail"
        );
    }

    @Test
    public void testLoginWithEmptyPassword() {
        service.signUp(EMAIL, PASSWORD);

        int userId = service.login(EMAIL, "");

        assertEquals(
                -1,
                userId,
                "Login with an empty password should fail"
        );
    }
}


