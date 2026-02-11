package ru.itmo.model;

import ru.itmo.model.util.ImportStatus;

import java.time.ZonedDateTime;

public class ImportOperation {
    private long id;

    // кто запустил
    private String username;
    private String role;

    // что импортировали: "creatures" или "cities"
    private String entityType;

    private ImportStatus status;

    private ZonedDateTime createdAt;
    private ZonedDateTime finishedAt;

    // заполняем только при SUCCESS
    private Integer insertedCount;

    // заполняем только при FAILED (коротко)
    private String errorMessage;

    public ImportOperation() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public ImportStatus getStatus() { return status; }
    public void setStatus(ImportStatus status) { this.status = status; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(ZonedDateTime finishedAt) { this.finishedAt = finishedAt; }

    public Integer getInsertedCount() { return insertedCount; }
    public void setInsertedCount(Integer insertedCount) { this.insertedCount = insertedCount; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}