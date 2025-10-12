package com.securevoting.dto;

import javax.validation.constraints.*;

public class CandidateNominationRequest {
    
    @NotBlank(message = "Candidate name is required")
    @Size(min = 2, max = 100, message = "Candidate name must be between 2 and 100 characters")
    private String candidateName;
    
    @NotBlank(message = "Gender is required")
    private String gender;
    
    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must not exceed 100")
    private Integer age;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Please provide a valid phone number")
    private String phoneNumber;
    
    @NotBlank(message = "Address is required")
    @Size(min = 10, message = "Address must be at least 10 characters")
    private String address;
    
    @NotBlank(message = "Party is required")
    @Size(min = 2, max = 100, message = "Party name must be between 2 and 100 characters")
    private String party;
    
    @NotBlank(message = "Party secret code is required")
    @Size(min = 6, message = "Party secret code must be at least 6 characters")
    private String partySecretCode;
    
    @NotBlank(message = "Aadhar card link is required")
    @Pattern(regexp = "^https://drive\\.google\\.com/.*$", message = "Please provide a valid Google Drive link")
    private String aadharCardLink;
    
    @Pattern(regexp = "^https://drive\\.google\\.com/.*$", message = "Candidate image link must be a valid Google Drive link")
    private String candidateImageLink;
    
    @NotNull(message = "Election ID is required")
    private Integer electionId;
    
    private Integer wardId;
    
    // Getters and Setters
    public String getCandidateName() {
        return candidateName;
    }
    
    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getParty() {
        return party;
    }
    
    public void setParty(String party) {
        this.party = party;
    }
    
    public String getPartySecretCode() {
        return partySecretCode;
    }
    
    public void setPartySecretCode(String partySecretCode) {
        this.partySecretCode = partySecretCode;
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
    
    public Integer getElectionId() {
        return electionId;
    }
    
    public void setElectionId(Integer electionId) {
        this.electionId = electionId;
    }
    
    public Integer getWardId() {
        return wardId;
    }
    
    public void setWardId(Integer wardId) {
        this.wardId = wardId;
    }
}
