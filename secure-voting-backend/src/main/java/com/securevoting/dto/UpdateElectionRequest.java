package com.securevoting.dto;

public class UpdateElectionRequest {
    private String name;
    private String description;
    private String rules;
    private long startDate;
    private long endDate;
    private String status;

    // Constructors
    public UpdateElectionRequest() {
    }

    public UpdateElectionRequest(String name, String description, String rules, long startDate, long endDate, String status) {
        this.name = name;
        this.description = description;
        this.rules = rules;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}



























