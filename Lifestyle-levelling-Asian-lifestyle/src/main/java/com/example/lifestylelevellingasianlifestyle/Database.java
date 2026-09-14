package com.example.lifestylelevellingasianlifestyle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;

public final class Database {
    public static final String DATABASE_PATH_PROPERTY = "lifestyle.db.path";
    private static final int SCHEMA_VERSION = 2;
    private static final String LEGACY_GROCERY_ARCHIVE = "legacy_groceries_archive";
    private static final Set<String> CURRENT_GROCERY_COLUMNS = Set.of(
            "id", "user_id", "name", "category", "quantity", "unit", "purchase_date", "expiry_date"
    );
    private static final Set<String> LEGACY_GROCERY_COLUMNS = Set.of(
            "itemname", "itemid", "userid", "expirydate", "dateadded"
    );

    private Database() {
    }

    public static Connection connect() throws SQLException {
        Path databasePath = getDatabasePath();
        try {
            Path parent = databasePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new SQLException("Could not create the database directory", e);
        }

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
        }
        return connection;
    }

    public static Path getDatabasePath() {
        String configuredPath = System.getProperty(DATABASE_PATH_PROPERTY);
        Path path = configuredPath == null || configuredPath.isBlank()
                ? Path.of("data", "users.db")
                : Path.of(configuredPath);
        return path.toAbsolutePath().normalize();
    }

    public static void initialize() throws SQLException {
        try (Connection connection = connect()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS users (
                            userId INTEGER PRIMARY KEY AUTOINCREMENT,
                            password TEXT NOT NULL,
                            email TEXT UNIQUE NOT NULL
                        )
                        """);
            }

            migrateLegacyGroceriesIfNeeded(connection);

            try (Statement statement = connection.createStatement()) {
                createGroceryTable(statement);
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS electricity_records (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            user_id INTEGER NOT NULL,
                            billing_month TEXT NOT NULL,
                            kwh REAL NOT NULL CHECK (kwh > 0),
                            bill_amount REAL NOT NULL CHECK (bill_amount >= 0),
                            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            UNIQUE (user_id, billing_month),
                            FOREIGN KEY (user_id) REFERENCES users(userId) ON DELETE CASCADE
                        )
                        """);
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS weather_preferences (
                            user_id INTEGER PRIMARY KEY,
                            location_name TEXT NOT NULL,
                            latitude REAL NOT NULL,
                            longitude REAL NOT NULL,
                            timezone TEXT NOT NULL,
                            FOREIGN KEY (user_id) REFERENCES users(userId) ON DELETE CASCADE
                        )
                        """);

                // An early development build created this table without timezone.
                // Preserve any rows while bringing that known schema forward.
                if (!columnNames(connection, "weather_preferences").contains("timezone")) {
                    statement.executeUpdate("""
                            ALTER TABLE weather_preferences
                            ADD COLUMN timezone TEXT NOT NULL DEFAULT 'UTC'
                            """);
                }

                verifyColumns(connection, "users", Set.of("userid", "password", "email"));
                verifyColumns(connection, "groceries", CURRENT_GROCERY_COLUMNS);
                verifyColumns(connection, "electricity_records", Set.of(
                        "id", "user_id", "billing_month", "kwh", "bill_amount"
                ));
                verifyColumns(connection, "weather_preferences", Set.of(
                        "user_id", "location_name", "latitude", "longitude", "timezone"
                ));

                statement.executeUpdate(
                        "CREATE INDEX IF NOT EXISTS idx_groceries_user_expiry ON groceries(user_id, expiry_date)"
                );
                statement.executeUpdate(
                        "CREATE INDEX IF NOT EXISTS idx_electricity_user_month "
                                + "ON electricity_records(user_id, billing_month)"
                );
                statement.executeUpdate(
                        "CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_nocase ON users(email COLLATE NOCASE)"
                );
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS schema_metadata (
                            singleton_id INTEGER PRIMARY KEY CHECK (singleton_id = 1),
                            version INTEGER NOT NULL
                        )
                        """);
                statement.executeUpdate("""
                        INSERT INTO schema_metadata (singleton_id, version)
                        VALUES (1, %d)
                        ON CONFLICT(singleton_id) DO UPDATE SET version = excluded.version
                        """.formatted(SCHEMA_VERSION));
            }
        }
    }

    private static void migrateLegacyGroceriesIfNeeded(Connection connection) throws SQLException {
        Set<String> columns = columnNames(connection, "groceries");
        if (columns.isEmpty() || columns.containsAll(CURRENT_GROCERY_COLUMNS)) {
            return;
        }
        if (!columns.containsAll(LEGACY_GROCERY_COLUMNS)) {
            Set<String> missing = new HashSet<>(CURRENT_GROCERY_COLUMNS);
            missing.removeAll(columns);
            throw new SQLException(
                    "Database schema is incompatible; table groceries is missing columns " + missing
            );
        }
        if (tableExists(connection, LEGACY_GROCERY_ARCHIVE)) {
            throw new SQLException(
                    "Legacy grocery migration cannot continue because the archive table already exists"
            );
        }

        int originalForeignKeys = pragmaInteger(connection, "foreign_keys");
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = OFF");
        }

        connection.setAutoCommit(false);
        try {
            archiveLegacyGroceries(connection);
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DROP TABLE groceries");
                createGroceryTable(statement);
            }
            restoreValidLegacyGroceries(connection);
            verifyForeignKeys(connection);
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackFailure) {
                e.addSuppressed(rollbackFailure);
            }
            throw e;
        } finally {
            try {
                connection.setAutoCommit(true);
            } finally {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("PRAGMA foreign_keys = " + (originalForeignKeys == 0 ? "OFF" : "ON"));
                }
            }
        }
    }

    private static void archiveLegacyGroceries(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE legacy_groceries_archive AS
                    SELECT itemId AS legacy_item_id,
                           itemName AS item_name,
                           userId AS user_id,
                           expiryDate AS expiry_date,
                           dateAdded AS date_added,
                           CURRENT_TIMESTAMP AS archived_at
                    FROM groceries
                    """);
        }
    }

    private static void restoreValidLegacyGroceries(Connection connection) throws SQLException {
        String selectSql = """
                SELECT legacy_item_id, item_name, user_id, expiry_date, date_added
                FROM legacy_groceries_archive
                ORDER BY legacy_item_id
                """;
        String userSql = "SELECT 1 FROM users WHERE userId = ?";
        String insertSql = """
                INSERT INTO groceries
                    (id, user_id, name, category, quantity, unit, purchase_date, expiry_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Statement select = connection.createStatement();
             ResultSet rows = select.executeQuery(selectSql);
             PreparedStatement findUser = connection.prepareStatement(userSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            while (rows.next()) {
                long legacyItemId = rows.getLong("legacy_item_id");
                long userId = rows.getLong("user_id");
                if (legacyItemId <= 0 || userId <= 0 || !userExists(findUser, userId)) {
                    continue;
                }

                try {
                    GroceryItem item = new GroceryItem(
                            legacyItemId,
                            userId,
                            rows.getString("item_name"),
                            "Other",
                            1,
                            "item(s)",
                            LocalDate.parse(rows.getString("date_added")),
                            LocalDate.parse(rows.getString("expiry_date"))
                    );
                    insert.setLong(1, item.getId());
                    insert.setLong(2, item.getUserId());
                    insert.setString(3, item.getName());
                    insert.setString(4, item.getCategory());
                    insert.setDouble(5, item.getQuantity());
                    insert.setString(6, item.getUnit());
                    insert.setString(7, item.getPurchaseDate().toString());
                    insert.setString(8, item.getExpiryDate().toString());
                    insert.executeUpdate();
                } catch (DateTimeParseException | IllegalArgumentException ignored) {
                    // The unchanged source row remains available in the archive for recovery.
                }
            }
        }
    }

    private static boolean userExists(PreparedStatement statement, long userId) throws SQLException {
        statement.setLong(1, userId);
        try (ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next();
        }
    }

    private static void verifyForeignKeys(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA foreign_key_check")) {
            if (resultSet.next()) {
                throw new SQLException("Database migration produced an invalid foreign key");
            }
        }
    }

    private static int pragmaInteger(Connection connection, String pragma) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA " + pragma)) {
            if (!resultSet.next()) {
                throw new SQLException("Could not read database setting " + pragma);
            }
            return resultSet.getInt(1);
        }
    }

    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT 1
                FROM sqlite_master
                WHERE type = 'table' AND name = ? COLLATE NOCASE
                """)) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static void createGroceryTable(Statement statement) throws SQLException {
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS groceries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    quantity REAL NOT NULL CHECK (quantity > 0),
                    unit TEXT NOT NULL,
                    purchase_date TEXT NOT NULL,
                    expiry_date TEXT NOT NULL,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(userId) ON DELETE CASCADE
                )
                """);
    }

    private static void verifyColumns(Connection connection, String table, Set<String> requiredColumns)
            throws SQLException {
        Set<String> actualColumns = columnNames(connection, table);
        if (!actualColumns.containsAll(requiredColumns)) {
            Set<String> missing = new HashSet<>(requiredColumns);
            missing.removeAll(actualColumns);
            throw new SQLException("Database schema is incompatible; table " + table
                    + " is missing columns " + missing);
        }
    }

    private static Set<String> columnNames(Connection connection, String table) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("name").toLowerCase());
            }
        }
        return columns;
    }
}
