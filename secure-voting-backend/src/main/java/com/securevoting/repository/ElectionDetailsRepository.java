package com.securevoting.repository;

import com.securevoting.model.ElectionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ElectionDetailsRepository extends JpaRepository<ElectionDetails, Integer> {

    Optional<ElectionDetails> findByElectionId(int electionId);

    void deleteByElectionId(int electionId);

    @Query("SELECT COUNT(e) FROM ElectionDetails e")
    long countAllElectionDetails();
}
