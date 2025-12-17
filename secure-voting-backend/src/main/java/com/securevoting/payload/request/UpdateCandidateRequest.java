package com.securevoting.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class UpdateCandidateRequest {

    @NotBlank(message = "Candidate name is required")
    @Pattern(regexp = "^[a-zA-Z\\s\\-']+$", message = "Candidate name cannot contain numbers")
    private String name;

    private Integer electionId;
    private Integer partyId;
    private Integer wardId;
    private String biography;
    private String manifestoSummary;
    
    // Candidate Details fields
    private String email;
    private String phoneNumber;
    private String gender;
    private Integer age;
    private String address;
    private String aadharCardLink;
    private String candidateImageLink;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAadharCardLink() {
        return aadharCardLink;
    }

    public void setAadharCardLink(String aadharCardLink) {
        this.aadharCardLink = aadharCardLink;
    }

    public String getCandidateImageLink() {
        return candidateImageLink;
    }

    public void setCandidateImageLink(String candidateImageLink) {
        this.candidateImageLink = candidateImageLink;
    }
}






















