package com.securevoting.service;

import com.securevoting.model.ElectionDetails;
import com.securevoting.repository.ElectionDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class ElectionDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(ElectionDetailsService.class);

    @Autowired
    private ElectionDetailsRepository electionDetailsRepository;

    public ElectionDetails createElectionDetails(ElectionDetails electionDetails) {
        logger.info("Creating election details for election ID: {}, description: {}, rules: {}", 
                   electionDetails.getElectionId(), electionDetails.getDescription(), electionDetails.getRules());
        ElectionDetails saved = electionDetailsRepository.save(electionDetails);
        logger.info("Election details saved successfully with ID: {}", saved.getElectionId());
        return saved;
    }

    public ElectionDetails updateElectionDetails(ElectionDetails electionDetails) {
        return electionDetailsRepository.save(electionDetails);
    }

    public Optional<ElectionDetails> getElectionDetailsByElectionId(int electionId) {
        return electionDetailsRepository.findByElectionId(electionId);
    }

    public void deleteElectionDetails(int electionId) {
        electionDetailsRepository.deleteByElectionId(electionId);
    }
}
