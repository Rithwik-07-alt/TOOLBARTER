package com.toolbarter.dto;

import java.time.Instant;

public class HealthResponse {

    private String status;
    private String app;
    private String message;
    private Instant timestamp;
    private DatabaseHealth database;

    public HealthResponse() {
    }

    public HealthResponse(String status, String app, String message, Instant timestamp, DatabaseHealth database) {
        this.status = status;
        this.app = app;
        this.message = message;
        this.timestamp = timestamp;
        this.database = database;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public DatabaseHealth getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseHealth database) {
        this.database = database;
    }
}
