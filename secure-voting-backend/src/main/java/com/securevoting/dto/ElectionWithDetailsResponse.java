package com.securevoting.dto;

import com.securevoting.model.Election;
import com.securevoting.model.ElectionDetails;

public class ElectionWithDetailsResponse {
    private int electionId;
    private String name;
    private long startDate;
    private long endDate;
    private String status;
    private String description;
    private String rules;

    // Constructors
    public ElectionWithDetailsResponse() {
    }

    public ElectionWithDetailsResponse(Election election, ElectionDetails details) {
        this.electionId = election.getElectionId();
        this.name = election.getName();
        this.startDate = election.getStartDate();
        this.endDate = election.getEndDate();
        this.status = election.getStatus();
        this.description = details != null ? details.getDescription() : null;
        this.rules = details != null ? details.getRules() : null;
    }

    // Getters and Setters
    public int getElectionId() {
        return electionId;
    }

    public void setElectionId(int electionId) {
        this.electionId = electionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}













