package com.securevoting.repository;

import com.securevoting.model.CandidateDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateDetailsRepository extends JpaRepository<CandidateDetails, Integer> {
    
    // Find candidate details by candidate ID
    CandidateDetails findByCandidateId(int candidateId);
    
    // Check if candidate details exist for a candidate
    boolean existsByCandidateId(int candidateId);
    
    // Delete candidate details by candidate ID
    void deleteByCandidateId(int candidateId);
}