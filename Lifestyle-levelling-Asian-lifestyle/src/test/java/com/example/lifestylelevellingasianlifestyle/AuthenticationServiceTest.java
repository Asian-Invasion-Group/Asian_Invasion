package com.example.lifestylelevellingasianlifestyle;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationServiceTest {
    @TempDir
    Path tempDirectory;

    private String previousDatabasePath;
    private AuthenticationService service;

    @BeforeEach
    void setUp() throws Exception {
        previousDatabasePath = System.getProperty(Database.DATABASE_PATH_PROPERTY);
        System.setProperty(Database.DATABASE_PATH_PROPERTY, tempDirectory.resolve("auth-test.db").toString());
        Database.initialize();
        service = new AuthenticationService();
    }

    @AfterEach
    void restoreDatabasePath() {
        if (previousDatabasePath == null) {
            System.clearProperty(Database.DATABASE_PATH_PROPERTY);
        } else {
            System.setProperty(Database.DATABASE_PATH_PROPERTY, previousDatabasePath);
        }
    }

    @Test
    void signsUpWithAHashAndAuthenticatesCaseInsensitively() {
        assertTrue(service.signUp("  Person@Example.com ", "secure-pass"));

        AuthenticationService.UserSession session = service
                .authenticate("PERSON@example.COM", "secure-pass")
                .orElseThrow();

        assertTrue(session.userId() > 0);
        assertEquals("person@example.com", session.email());
        assertFalse(service.login("person@example.com", "wrong-pass"));
    }

    @Test
    void rejectsDuplicateAndInvalidAccounts() {
        assertTrue(service.signUp("person@example.com", "secure-pass"));
        assertEquals(AuthenticationService.SignUpResult.DUPLICATE_EMAIL,
                service.signUpWithResult("PERSON@example.com", "another-pass"));
        assertEquals(AuthenticationService.SignUpResult.INVALID_INPUT,
                service.signUpWithResult("not-an-email", "secure-pass"));
        assertEquals(AuthenticationService.SignUpResult.INVALID_INPUT,
                service.signUpWithResult("short@example.com", "short"));
        assertTrue(service.authenticate(null, null).isEmpty());
    }
}
