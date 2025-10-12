package com.securevoting.model;

import javax.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "elections")
public class Election {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "election_id")
    private int electionId;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "start_date")
    private long startDate;
    
    @Column(name = "end_date")
    private long endDate;
    
    @Column(name = "status")
    private String status;

    // Relationship with Candidates
    @OneToMany(mappedBy = "election", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Candidate> candidates;

    // Relationship with ElectionDetails
    @OneToOne(mappedBy = "election", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private ElectionDetails electionDetails;

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

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }

    public ElectionDetails getElectionDetails() {
        return electionDetails;
    }

    public void setElectionDetails(ElectionDetails electionDetails) {
        this.electionDetails = electionDetails;
    }
}