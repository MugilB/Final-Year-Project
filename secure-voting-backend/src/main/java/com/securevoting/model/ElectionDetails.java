package com.securevoting.model;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "election_details")
public class ElectionDetails {

    @Id
    @Column(name = "election_id")
    private int electionId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "rules", columnDefinition = "TEXT")
    private String rules;

    // One-to-one relationship with Election
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id")
    @JsonBackReference
    private Election election;

    // Constructors
    public ElectionDetails() {
    }

    public ElectionDetails(int electionId, String description, String rules) {
        this.electionId = electionId;
        this.description = description;
        this.rules = rules;
    }

    // Getters and Setters
    public int getElectionId() {
        return electionId;
    }

    public void setElectionId(int electionId) {
        this.electionId = electionId;
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

    public Election getElection() {
        return election;
    }

    public void setElection(Election election) {
        this.election = election;
    }
}
