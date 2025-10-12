package com.securevoting.model;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.List;

@Entity
@Table(name = "party_details")
public class PartyDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "party_id")
    private int partyId;

    @Column(name = "party_name", nullable = false, unique = true)
    private String partyName;

    @Column(name = "party_symbol")
    private String partySymbol;

    @Column(name = "party_secret_code", nullable = false)
    private String partySecretCode;

    // One-to-many relationship with Candidate
    @OneToMany(mappedBy = "partyId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Candidate> candidates;

    // Getters and Setters
    public int getPartyId() {
        return partyId;
    }

    public void setPartyId(int partyId) {
        this.partyId = partyId;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getPartySymbol() {
        return partySymbol;
    }

    public void setPartySymbol(String partySymbol) {
        this.partySymbol = partySymbol;
    }

    public String getPartySecretCode() {
        return partySecretCode;
    }

    public void setPartySecretCode(String partySecretCode) {
        this.partySecretCode = partySecretCode;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }
}
