package com.securevoting.payload.request;

import javax.validation.constraints.NotBlank;

public class UpdateCandidateStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;

    private String reviewNotes;
    private String reviewedBy;

    // Constructors
    public UpdateCandidateStatusRequest() {
    }

    public UpdateCandidateStatusRequest(String status, String reviewNotes, String reviewedBy) {
        this.status = status;
        this.reviewNotes = reviewNotes;
        this.reviewedBy = reviewedBy;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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
}



















