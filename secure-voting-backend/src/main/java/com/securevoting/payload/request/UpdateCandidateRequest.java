package com.securevoting.payload.request;

import javax.validation.constraints.NotBlank;

public class UpdateCandidateRequest {

    @NotBlank(message = "Candidate name is required")
    private String name;

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













