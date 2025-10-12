package com.securevoting.dto;

public class VoteRequest {
    private String voteData;
    private int electionId;

    public String getVoteData() {
        return voteData;
    }

    public void setVoteData(String voteData) {
        this.voteData = voteData;
    }

    public int getElectionId() {
        return electionId;
    }

    public void setElectionId(int electionId) {
        this.electionId = electionId;
    }
}