package com.securevoting.model;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "candidate_details")
public class CandidateDetails {

    @Id
    @Column(name = "candidate_id")
    private int candidateId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "age", nullable = false)
    private int age;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "aadhar_card_link", nullable = false)
    private String aadharCardLink;

    @Column(name = "candidate_image_link")
    private String candidateImageLink;

    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;

    @Column(name = "manifesto_summary", columnDefinition = "TEXT")
    private String manifestoSummary;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Long reviewedAt;

    // One-to-one relationship with Candidate
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", insertable = false, updatable = false)
    @JsonBackReference
    private Candidate candidate;

    // Getters and Setters
    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
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

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    // New getters and setters for personal information fields
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
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

    // New getters and setters for review fields
    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Long getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Long reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
