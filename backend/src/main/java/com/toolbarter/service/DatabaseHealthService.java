package com.toolbarter.service;

import com.toolbarter.dto.DatabaseHealth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.regex.Pattern;

@Service
public class DatabaseHealthService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthService.class);

    private static final Pattern PASSWORD_PARAM_PATTERN = Pattern.compile("(?i)(password=)([^;&\\s]+)");
    private static final Pattern USER_PARAM_PATTERN = Pattern.compile("(?i)(user=)([^;&\\s]+)");

    private final DataSource dataSource;

    @Autowired
    public DatabaseHealthService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Verifies connectivity to the configured database and extracts metadata.
     * Sanitizes errors to ensure sensitive credentials are never leaked.
     */
    public DatabaseHealth checkDatabaseConnectivity() {
        String dbName = "toolbarter";

        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(3)) {
                return DatabaseHealth.down(dbName, "Database connection validation check timed out.", "08001");
            }

            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            if (catalog != null && !catalog.isBlank()) {
                dbName = catalog;
            }

            String productName = metaData.getDatabaseProductName();
            String productVersion = metaData.getDatabaseProductVersion();
            String driverName = metaData.getDriverName() + " " + metaData.getDriverVersion();

            return DatabaseHealth.up(dbName, productName, productVersion, driverName);

        } catch (SQLException ex) {
            log.warn("Database connectivity check failed: SQLState={}, ErrorCode={}", ex.getSQLState(), ex.getErrorCode());
            String diagnostic = buildSanitizedDiagnostic(ex);
            return DatabaseHealth.down(dbName, diagnostic, ex.getSQLState());
        } catch (Exception ex) {
            log.warn("Unexpected failure during database health check: {}", ex.getMessage());
            String sanitized = sanitizeSecretData(ex.getMessage() != null ? ex.getMessage() : "Unknown connection error");
            return DatabaseHealth.down(dbName, sanitized, "GENERAL_ERROR");
        }
    }

    /**
     * Builds developer-friendly diagnostics for connection errors without leaking credentials.
     */
    public String buildSanitizedDiagnostic(SQLException ex) {
        String sqlState = ex.getSQLState();
        int errorCode = ex.getErrorCode();
        String rawMessage = ex.getMessage() != null ? ex.getMessage() : "";

        // MySQL Error 1045 (SQLState 28000): Access denied
        if ("28000".equals(sqlState) || errorCode == 1045) {
            return "Authentication Failed (Error 1045): Access denied. Verify DB_USERNAME and DB_PASSWORD environment variables.";
        }

        // MySQL Error 1049 (SQLState 42000): Unknown database
        if (errorCode == 1049 || rawMessage.contains("Unknown database")) {
            return "Database Not Found (Error 1049): Database 'toolbarter' does not exist. Run the creation script in MySQL: backend/src/main/resources/db/init_database.sql.";
        }

        // Communications / Connection refused
        if ("08S01".equals(sqlState) || "08001".equals(sqlState) || rawMessage.toLowerCase().contains("communications link failure")) {
            return "Connection Refused: Unable to connect to MySQL host/port. Verify MySQL service is running and DB_URL points to a reachable instance (port 3306).";
        }

        // Fallback: Strip any raw secrets that might have been included in the driver message
        return sanitizeSecretData(rawMessage);
    }

    /**
     * Strips passwords and sensitive parameters from raw strings.
     */
    public String sanitizeSecretData(String input) {
        if (input == null) {
            return "";
        }
        String sanitized = PASSWORD_PARAM_PATTERN.matcher(input).replaceAll("$1******");
        return sanitized;
    }
}
