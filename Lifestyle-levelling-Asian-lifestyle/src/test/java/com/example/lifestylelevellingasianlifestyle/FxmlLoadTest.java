package com.example.lifestylelevellingasianlifestyle;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FxmlLoadTest {
    @BeforeAll
    static void startJavaFx() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(started::countDown);
        assertTrue(started.await(5, TimeUnit.SECONDS), "JavaFX toolkit did not start");
    }

    @AfterAll
    static void stopJavaFx() {
        Platform.exit();
    }

    @Test
    void loadsAuthenticationAndMainViewsWithStylesheets() throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch loaded = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader authLoader = new FXMLLoader(Launcher.class.getResource("authenticator-view.fxml"));
                Parent authRoot = authLoader.load();
                assertNotNull(authLoader.getController());
                assertDoesNotThrow(() -> new Scene(authRoot).getRoot().applyCss());

                FXMLLoader mainLoader = new FXMLLoader(Launcher.class.getResource("main-view.fxml"));
                Parent mainRoot = mainLoader.load();
                assertNotNull(mainLoader.getController());
                assertDoesNotThrow(() -> new Scene(mainRoot).getRoot().applyCss());
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                loaded.countDown();
            }
        });

        assertTrue(loaded.await(10, TimeUnit.SECONDS), "FXML loading timed out");
        if (failure.get() != null) {
            throw new AssertionError("FXML did not load cleanly", failure.get());
        }
    }
}
