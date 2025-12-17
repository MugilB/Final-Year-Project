package com.securevoting.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class CreateCandidateRequest {

    @NotBlank(message = "Candidate name is required")
    @Pattern(regexp = "^[a-zA-Z\\s\\-']+$", message = "Candidate name cannot contain numbers")
    private String name;

    @NotNull(message = "Election ID is required")
    private Integer electionId;

    private Integer partyId;
    private Integer wardId;
    private String biography;
    private String manifestoSummary;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getElectionId() {
        return electionId;
    }

    public void setElectionId(Integer electionId) {
        this.electionId = electionId;
    }

    public Integer getPartyId() {
        return partyId;
    }

    public void setPartyId(Integer partyId) {
        this.partyId = partyId;
    }

    public Integer getWardId() {
        return wardId;
    }

    public void setWardId(Integer wardId) {
        this.wardId = wardId;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getManifestoSummary() {
        return manifestoSummary;
    }

    public void setManifestoSummary(String manifestoSummary) {
        this.manifestoSummary = manifestoSummary;
    }
}























