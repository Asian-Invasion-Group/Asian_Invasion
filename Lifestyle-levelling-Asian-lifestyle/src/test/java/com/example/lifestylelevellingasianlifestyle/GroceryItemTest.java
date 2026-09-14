package com.example.lifestylelevellingasianlifestyle;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GroceryItemTest {
    private static final LocalDate PURCHASE_DATE = LocalDate.of(2026, 9, 1);
    private static final LocalDate EXPIRY_DATE = LocalDate.of(2026, 9, 15);

    @Test
    void normalizesTextAndRetainsInventoryValues() {
        GroceryItem item = new GroceryItem(
                7,
                "  Milk  ",
                "  Dairy ",
                1.5,
                " L ",
                PURCHASE_DATE,
                EXPIRY_DATE
        );

        assertAll(
                () -> assertEquals(0, item.getId()),
                () -> assertEquals(7, item.getUserId()),
                () -> assertEquals("Milk", item.getName()),
                () -> assertEquals("Dairy", item.getCategory()),
                () -> assertEquals(1.5, item.getQuantity()),
                () -> assertEquals("L", item.getUnit()),
                () -> assertEquals(PURCHASE_DATE, item.getPurchaseDate()),
                () -> assertEquals(EXPIRY_DATE, item.getExpiryDate())
        );
    }

    @Test
    void calculatesEveryExpiryStatusAndRecommendationDeterministically() {
        GroceryItem item = new GroceryItem(
                1,
                "Spinach",
                "Vegetables",
                1,
                "bag",
                PURCHASE_DATE,
                EXPIRY_DATE
        );

        assertAll(
                () -> assertEquals(-2, item.daysUntilExpiry(LocalDate.of(2026, 9, 17))),
                () -> assertEquals(GroceryItem.ExpiryStatus.EXPIRED,
                        item.expiryStatus(LocalDate.of(2026, 9, 17))),
                () -> assertEquals("Expired 2 days ago. Discard it safely and update your stock.",
                        item.recommendation(LocalDate.of(2026, 9, 17))),
                () -> assertEquals(GroceryItem.ExpiryStatus.EXPIRING_TODAY,
                        item.expiryStatus(EXPIRY_DATE)),
                () -> assertEquals("Use today to avoid food waste.", item.recommendation(EXPIRY_DATE)),
                () -> assertEquals(GroceryItem.ExpiryStatus.EXPIRING_SOON,
                        item.expiryStatus(LocalDate.of(2026, 9, 12))),
                () -> assertEquals("Use within 3 days; plan it into your next meal.",
                        item.recommendation(LocalDate.of(2026, 9, 12))),
                () -> assertEquals(GroceryItem.ExpiryStatus.FRESH,
                        item.expiryStatus(LocalDate.of(2026, 9, 11))),
                () -> assertEquals("Fresh for 4 more days.",
                        item.recommendation(LocalDate.of(2026, 9, 11)))
        );
    }

    @Test
    void rejectsInvalidInventoryValues() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> item(-1, 1, "Milk", "Dairy", 1, "L", PURCHASE_DATE, EXPIRY_DATE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> item(0, 0, "Milk", "Dairy", 1, "L", PURCHASE_DATE, EXPIRY_DATE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> item(0, 1, " ", "Dairy", 1, "L", PURCHASE_DATE, EXPIRY_DATE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> item(0, 1, "Milk", null, 1, "L", PURCHASE_DATE, EXPIRY_DATE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> item(0, 1, "Milk", "Dairy", 0, "L", PURCHASE_DATE, EXPIRY_DATE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> item(0, 1, "Milk", "Dairy", Double.NaN, "L", PURCHASE_DATE, EXPIRY_DATE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> item(0, 1, "Milk", "Dairy", Double.POSITIVE_INFINITY, "L",
                                PURCHASE_DATE, EXPIRY_DATE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> item(0, 1, "Milk", "Dairy", 1, " ", PURCHASE_DATE, EXPIRY_DATE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> item(0, 1, "Milk", "Dairy", 1, "L", null, EXPIRY_DATE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> item(0, 1, "Milk", "Dairy", 1, "L", EXPIRY_DATE, PURCHASE_DATE))
        );
    }

    private static GroceryItem item(
            long id,
            long userId,
            String name,
            String category,
            double quantity,
            String unit,
            LocalDate purchaseDate,
            LocalDate expiryDate
    ) {
        return new GroceryItem(id, userId, name, category, quantity, unit, purchaseDate, expiryDate);
    }
}
