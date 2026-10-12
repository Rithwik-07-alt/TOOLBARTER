package com.toolbarter.dto;

public class DatabaseHealth {

    private String status;
    private String databaseName;
    private String engine;
    private String version;
    private String driver;
    private String message;
    private String diagnostic;
    private String sqlState;

    public DatabaseHealth() {
    }

    public static DatabaseHealth up(String databaseName, String engine, String version, String driver) {
        DatabaseHealth health = new DatabaseHealth();
        health.setStatus("UP");
        health.setDatabaseName(databaseName);
        health.setEngine(engine);
        health.setVersion(version);
        health.setDriver(driver);
        health.setMessage("Database connection established successfully.");
        return health;
    }

    public static DatabaseHealth down(String databaseName, String diagnostic, String sqlState) {
        DatabaseHealth health = new DatabaseHealth();
        health.setStatus("DOWN");
        health.setDatabaseName(databaseName);
        health.setDiagnostic(diagnostic);
        health.setSqlState(sqlState);
        health.setMessage("Database connection failed. See diagnostic for details.");
        return health;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDiagnostic() {
        return diagnostic;
    }

    public void setDiagnostic(String diagnostic) {
        this.diagnostic = diagnostic;
    }

    public String getSqlState() {
        return sqlState;
    }

    public void setSqlState(String sqlState) {
        this.sqlState = sqlState;
    }
}
