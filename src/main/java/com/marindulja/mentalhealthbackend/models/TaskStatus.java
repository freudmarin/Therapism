package com.marindulja.mentalhealthbackend.models;

public enum TaskStatus {
    ASSIGNED,

    REASSIGNED,

    CANCELED,
    IN_PROGRESS,

    COMPLETED;
    public static TaskStatus fromString(String status) {
        return TaskStatus.valueOf(status.toUpperCase());
    };
}
