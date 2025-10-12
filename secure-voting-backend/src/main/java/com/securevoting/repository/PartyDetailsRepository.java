package com.securevoting.repository;

import com.securevoting.model.PartyDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartyDetailsRepository extends JpaRepository<PartyDetails, Integer> {
    
    // Find party by name
    Optional<PartyDetails> findByPartyName(String partyName);
    
    // Check if party exists by name
    boolean existsByPartyName(String partyName);
    
    // Find party by secret code
    Optional<PartyDetails> findByPartySecretCode(String partySecretCode);
    
    // Check if party exists by secret code
    boolean existsByPartySecretCode(String partySecretCode);
}
