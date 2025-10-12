package com.securevoting.repository;

import com.securevoting.model.Candidate;
import com.securevoting.model.CandidateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Integer> {

    // Find all candidates for a specific election
    List<Candidate> findByElectionId(int electionId);

    // Find candidates by party
    List<Candidate> findByPartyId(Integer partyId);

    // Find candidates by ward
    List<Candidate> findByWardId(Integer wardId);

    // Find candidates by election and party
    List<Candidate> findByElectionIdAndPartyId(int electionId, Integer partyId);

    // Find candidates by election and ward
    List<Candidate> findByElectionIdAndWardId(int electionId, Integer wardId);

    // Check if candidate exists in election
    boolean existsByElectionIdAndName(int electionId, String name);
    
    // Check if candidate exists by name and election (for nomination validation)
    boolean existsByNameAndElectionId(String name, int electionId);
    
    // Find candidates by status
    List<Candidate> findByStatus(CandidateStatus status);
    
    // Find candidates by election and status
    List<Candidate> findByElectionIdAndStatus(int electionId, CandidateStatus status);
    
    // Find candidates by party ID
    List<Candidate> findByPartyId(int partyId);
    
    // Find candidates by election and party ID
    List<Candidate> findByElectionIdAndPartyId(int electionId, int partyId);
}

