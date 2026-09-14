package com.example.lifestylelevellingasianlifestyle;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * An immutable grocery inventory entry.
 *
 * <p>Dates are represented as {@link LocalDate} values and are persisted by
 * {@link GroceryService} using their ISO-8601 representation.</p>
 */
public final class GroceryItem {
    public static final int EXPIRING_SOON_DAYS = 3;

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_CATEGORY_LENGTH = 50;
    private static final int MAX_UNIT_LENGTH = 30;

    public enum ExpiryStatus {
        EXPIRED,
        EXPIRING_TODAY,
        EXPIRING_SOON,
        FRESH
    }

    private final long id;
    private final long userId;
    private final String name;
    private final String category;
    private final double quantity;
    private final String unit;
    private final LocalDate purchaseDate;
    private final LocalDate expiryDate;

    /**
     * Creates an item that has not yet been persisted. Its id is {@code 0}
     * until {@link GroceryService#add(GroceryItem)} returns the saved item.
     */
    public GroceryItem(
            long userId,
            String name,
            String category,
            double quantity,
            String unit,
            LocalDate purchaseDate,
            LocalDate expiryDate
    ) {
        this(0, userId, name, category, quantity, unit, purchaseDate, expiryDate);
    }

    public GroceryItem(
            long id,
            long userId,
            String name,
            String category,
            double quantity,
            String unit,
            LocalDate purchaseDate,
            LocalDate expiryDate
    ) {
        if (id < 0) {
            throw new IllegalArgumentException("id must not be negative");
        }
        if (userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
        if (!Double.isFinite(quantity) || quantity <= 0) {
            throw new IllegalArgumentException("quantity must be a finite positive number");
        }

        this.id = id;
        this.userId = userId;
        this.name = requireText(name, "name", MAX_NAME_LENGTH);
        this.category = requireText(category, "category", MAX_CATEGORY_LENGTH);
        this.quantity = quantity;
        this.unit = requireText(unit, "unit", MAX_UNIT_LENGTH);
        this.purchaseDate = requireDate(purchaseDate, "purchaseDate");
        this.expiryDate = requireDate(expiryDate, "expiryDate");

        if (this.expiryDate.isBefore(this.purchaseDate)) {
            throw new IllegalArgumentException("expiryDate must not be before purchaseDate");
        }
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    /**
     * Returns the signed number of days from {@code referenceDate} to expiry.
     * A negative value means the item has expired.
     */
    public long daysUntilExpiry(LocalDate referenceDate) {
        return ChronoUnit.DAYS.between(requireDate(referenceDate, "referenceDate"), expiryDate);
    }

    public long getDaysUntilExpiry() {
        return daysUntilExpiry(LocalDate.now());
    }

    public ExpiryStatus expiryStatus(LocalDate referenceDate) {
        long days = daysUntilExpiry(referenceDate);
        if (days < 0) {
            return ExpiryStatus.EXPIRED;
        }
        if (days == 0) {
            return ExpiryStatus.EXPIRING_TODAY;
        }
        if (days <= EXPIRING_SOON_DAYS) {
            return ExpiryStatus.EXPIRING_SOON;
        }
        return ExpiryStatus.FRESH;
    }

    public ExpiryStatus getExpiryStatus() {
        return expiryStatus(LocalDate.now());
    }

    /**
     * Provides concise, display-ready guidance based on the expiry date.
     */
    public String recommendation(LocalDate referenceDate) {
        long days = daysUntilExpiry(referenceDate);
        return switch (expiryStatus(referenceDate)) {
            case EXPIRED -> "Expired " + Math.abs(days)
                    + (Math.abs(days) == 1 ? " day ago. " : " days ago. ")
                    + "Discard it safely and update your stock.";
            case EXPIRING_TODAY -> "Use today to avoid food waste.";
            case EXPIRING_SOON -> "Use within " + days
                    + (days == 1 ? " day" : " days")
                    + "; plan it into your next meal.";
            case FRESH -> "Fresh for " + days
                    + (days == 1 ? " more day." : " more days.");
        };
    }

    public String getRecommendation() {
        return recommendation(LocalDate.now());
    }

    private static String requireText(String value, String fieldName, int maxLength) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters");
        }
        return normalized;
    }

    private static LocalDate requireDate(LocalDate value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof GroceryItem that)) {
            return false;
        }
        return id == that.id
                && userId == that.userId
                && Double.compare(quantity, that.quantity) == 0
                && name.equals(that.name)
                && category.equals(that.category)
                && unit.equals(that.unit)
                && purchaseDate.equals(that.purchaseDate)
                && expiryDate.equals(that.expiryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, name, category, quantity, unit, purchaseDate, expiryDate);
    }

    @Override
    public String toString() {
        return "GroceryItem{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", purchaseDate=" + purchaseDate +
                ", expiryDate=" + expiryDate +
                '}';
    }
}
