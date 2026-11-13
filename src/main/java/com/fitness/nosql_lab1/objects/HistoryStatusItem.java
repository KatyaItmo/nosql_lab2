package com.fitness.nosql_lab1.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class HistoryStatusItem {
    private Status status;

    @JsonProperty("changed_at")
    private LocalDateTime changedAt;

    public HistoryStatusItem() {}

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
