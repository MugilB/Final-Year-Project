package com.securevoting.model;

import javax.persistence.*;

@Entity
@Table(name = "user_details")
public class UserDetails {

    @Id
    @Column(name = "voter_id")
    private String voterId;

    @Column(name = "user_voter_id", nullable = false)
    private String userVoterId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "gender")
    private String gender;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Column(name = "ward_id")
    private Integer wardId;

    @Column(name = "no_elections_voted")
    private Integer noElectionsVoted = 0;

    @Column(name = "last_election_voted")
    private String lastElectionVoted;

    @Column(name = "dob")
    private Long dob;

    @Column(name = "email")
    private String email;

    @Column(name = "aadhar_card_link", columnDefinition = "TEXT")
    private String aadharCardLink;

    @Column(name = "profile_picture_link", columnDefinition = "TEXT")
    private String profilePictureLink;

    // Constructors
    public UserDetails() {}

    public UserDetails(String voterId, String userVoterId) {
        this.voterId = voterId;
        this.userVoterId = userVoterId;
    }

    // Getters and Setters
    public String getVoterId() {
        return voterId;
    }

    public void setVoterId(String voterId) {
        this.voterId = voterId;
    }

    public String getUserVoterId() {
        return userVoterId;
    }

    public void setUserVoterId(String userVoterId) {
        this.userVoterId = userVoterId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public Integer getWardId() {
        return wardId;
    }

    public void setWardId(Integer wardId) {
        this.wardId = wardId;
    }

    public Integer getNoElectionsVoted() {
        return noElectionsVoted;
    }

    public void setNoElectionsVoted(Integer noElectionsVoted) {
        this.noElectionsVoted = noElectionsVoted;
    }

    public String getLastElectionVoted() {
        return lastElectionVoted;
    }

    public void setLastElectionVoted(String lastElectionVoted) {
        this.lastElectionVoted = lastElectionVoted;
    }

    public Long getDob() {
        return dob;
    }

    public void setDob(Long dob) {
        this.dob = dob;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAadharCardLink() {
        return aadharCardLink;
    }

    public void setAadharCardLink(String aadharCardLink) {
        this.aadharCardLink = aadharCardLink;
    }

    public String getProfilePictureLink() {
        return profilePictureLink;
    }

    public void setProfilePictureLink(String profilePictureLink) {
        this.profilePictureLink = profilePictureLink;
    }
}