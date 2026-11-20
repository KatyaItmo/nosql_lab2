package com.fitness.nosql_lab1.db.repos.data;

public class DaoResponse {
    private boolean isSuccess;
    private String message;

    public DaoResponse() {}

    public DaoResponse(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
