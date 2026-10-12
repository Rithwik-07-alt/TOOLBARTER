package com.toolbarter.service;

import com.toolbarter.dto.DatabaseHealth;
import com.toolbarter.service.DatabaseHealthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthServiceTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData metaData;

    private DatabaseHealthService service;

    @BeforeEach
    void setUp() {
        service = new DatabaseHealthService(dataSource);
    }

    @Test
    void checkDatabaseConnectivity_Success() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(3)).thenReturn(true);
        when(connection.getCatalog()).thenReturn("toolbarter");
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");
        when(metaData.getDatabaseProductVersion()).thenReturn("8.0.43");
        when(metaData.getDriverName()).thenReturn("MySQL Connector/J");
        when(metaData.getDriverVersion()).thenReturn("8.3.0");

        DatabaseHealth health = service.checkDatabaseConnectivity();

        assertNotNull(health);
        assertEquals("UP", health.getStatus());
        assertEquals("toolbarter", health.getDatabaseName());
        assertEquals("MySQL", health.getEngine());
        assertEquals("8.0.43", health.getVersion());
    }

    @Test
    void checkDatabaseConnectivity_AccessDenied_ShouldSanitizeAndAdvise() throws Exception {
        SQLException accessDeniedEx = new SQLException("Access denied for user 'root'@'localhost' (using password: YES)", "28000", 1045);
        when(dataSource.getConnection()).thenThrow(accessDeniedEx);

        DatabaseHealth health = service.checkDatabaseConnectivity();

        assertNotNull(health);
        assertEquals("DOWN", health.getStatus());
        assertEquals("28000", health.getSqlState());
        assertTrue(health.getDiagnostic().contains("Authentication Failed"));
        assertTrue(health.getDiagnostic().contains("DB_USERNAME and DB_PASSWORD"));
        assertFalse(health.getDiagnostic().contains("password: YES"));
    }

    @Test
    void checkDatabaseConnectivity_DatabaseNotFound_ShouldInstructInitScript() throws Exception {
        SQLException unknownDbEx = new SQLException("Unknown database 'toolbarter'", "42000", 1049);
        when(dataSource.getConnection()).thenThrow(unknownDbEx);

        DatabaseHealth health = service.checkDatabaseConnectivity();

        assertNotNull(health);
        assertEquals("DOWN", health.getStatus());
        assertEquals("42000", health.getSqlState());
        assertTrue(health.getDiagnostic().contains("Database Not Found"));
        assertTrue(health.getDiagnostic().contains("init_database.sql"));
    }

    @Test
    void sanitizeSecretData_ShouldMaskPasswords() {
        String input = "jdbc:mysql://localhost:3306/toolbarter?user=root&password=SuperSecretPassword123&useSSL=false";
        String sanitized = service.sanitizeSecretData(input);

        assertFalse(sanitized.contains("SuperSecretPassword123"));
        assertTrue(sanitized.contains("password=******"));
    }
}
