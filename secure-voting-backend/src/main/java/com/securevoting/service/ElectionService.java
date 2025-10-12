package com.securevoting.service;

import com.securevoting.model.Election;
import com.securevoting.model.Candidate;
import com.securevoting.model.CandidateStatus;
import com.securevoting.model.ElectionDetails;
import com.securevoting.repository.ElectionRepository;
import com.securevoting.repository.CandidateRepository;
import com.securevoting.dto.CreateElectionRequest;
import com.securevoting.dto.UpdateElectionRequest;
import com.securevoting.dto.ElectionWithDetailsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ElectionService {

    private static final Logger logger = LoggerFactory.getLogger(ElectionService.class);

    @Autowired
    private ElectionRepository electionRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ElectionDetailsService electionDetailsService;

    public Election createElection(CreateElectionRequest request) {
        logger.info("Creating election with name: {}, description: {}, rules: {}", 
                   request.getName(), request.getDescription(), request.getRules());
        
        // Create the election
        Election election = new Election();
        election.setName(request.getName());
        election.setStartDate(request.getStartDate());
        election.setEndDate(request.getEndDate());
        election.setStatus(request.getStatus() != null ? request.getStatus() : "SCHEDULED");
        
        Election savedElection = electionRepository.save(election);
        logger.info("Election saved with ID: {}", savedElection.getElectionId());
        
        // Always create election details (even if description and rules are null)
        ElectionDetails details = new ElectionDetails();
        details.setElectionId(savedElection.getElectionId());
        details.setDescription(request.getDescription());
        details.setRules(request.getRules());
        electionDetailsService.createElectionDetails(details);
        
        return savedElection;
    }

    public List<Election> getAllElections() {
        return electionRepository.findAll();
    }

    public List<ElectionWithDetailsResponse> getAllElectionsWithDetails() {
        List<Election> elections = electionRepository.findAll();
        return elections.stream()
                .map(election -> {
                    Optional<ElectionDetails> details = electionDetailsService.getElectionDetailsByElectionId(election.getElectionId());
                    return new ElectionWithDetailsResponse(election, details.orElse(null));
                })
                .collect(Collectors.toList());
    }

    public List<Election> getOpenElections() {
        long now = System.currentTimeMillis();
        return electionRepository.findByStatus("OPENED").stream()
                .filter(election -> now >= election.getStartDate() && now <= election.getEndDate())
                .collect(Collectors.toList());
    }

    // Get election with candidates
    public Election getElectionWithCandidates(int electionId) {
        Election election = electionRepository.findById(electionId).orElse(null);
        if (election != null) {
            List<Candidate> candidates = candidateRepository.findByElectionId(electionId);
            election.setCandidates(candidates);
        }
        return election;
    }

    // Get election with approved candidates only
    public Election getElectionWithApprovedCandidates(int electionId) {
        Election election = electionRepository.findById(electionId).orElse(null);
        if (election != null) {
            List<Candidate> approvedCandidates = candidateRepository.findByElectionIdAndStatus(electionId, CandidateStatus.APPROVED);
            election.setCandidates(approvedCandidates);
        }
        return election;
    }

    // Get all elections with candidates
    public List<Election> getAllElectionsWithCandidates() {
        List<Election> elections = electionRepository.findAll();
        for (Election election : elections) {
            List<Candidate> candidates = candidateRepository.findByElectionId(election.getElectionId());
            election.setCandidates(candidates);
        }
        return elections;
    }

    // Get all elections with approved candidates only
    public List<Election> getAllElectionsWithApprovedCandidates() {
        List<Election> elections = electionRepository.findAll();
        for (Election election : elections) {
            List<Candidate> approvedCandidates = candidateRepository.findByElectionIdAndStatus(election.getElectionId(), CandidateStatus.APPROVED);
            election.setCandidates(approvedCandidates);
        }
        return elections;
    }

    // Get elections that are open for nominations (not yet started)
    public List<Election> getElectionsForNominations() {
        long currentTime = System.currentTimeMillis();
        logger.info("Getting elections for nominations. Current time: {}", currentTime);
        
        List<Election> allElections = electionRepository.findAll();
        logger.info("Found {} total elections in database", allElections.size());
        
        for (Election election : allElections) {
            logger.info("Election: {} - Start: {} - Status: {}", 
                election.getName(), election.getStartDate(), election.getStatus());
        }
        
        List<Election> futureElections = allElections.stream()
                .filter(election -> election.getStartDate() > currentTime)
                .collect(java.util.stream.Collectors.toList());
                
        logger.info("Found {} elections open for nominations", futureElections.size());
        return futureElections;
    }

    // Update election
    public Election updateElection(int electionId, UpdateElectionRequest request) {
        Optional<Election> existingElection = electionRepository.findById(electionId);
        if (existingElection.isPresent()) {
            Election election = existingElection.get();
            election.setName(request.getName());
            election.setStartDate(request.getStartDate());
            election.setEndDate(request.getEndDate());
            
            // Automatically determine status based on current time and new dates
            long currentTime = System.currentTimeMillis();
            String newStatus;
            
            if (currentTime < election.getStartDate()) {
                newStatus = "SCHEDULED";
            } else if (currentTime >= election.getStartDate() && currentTime <= election.getEndDate()) {
                newStatus = "OPENED";
            } else {
                newStatus = "CLOSED";
            }
            
            election.setStatus(newStatus);
            logger.info("Election '{}' status automatically updated to '{}' based on new dates", election.getName(), newStatus);
            
            Election updatedElection = electionRepository.save(election);
            
            // Update election details
            Optional<ElectionDetails> existingDetails = electionDetailsService.getElectionDetailsByElectionId(electionId);
            if (existingDetails.isPresent()) {
                ElectionDetails details = existingDetails.get();
                details.setDescription(request.getDescription());
                details.setRules(request.getRules());
                electionDetailsService.updateElectionDetails(details);
            } else if (request.getDescription() != null || request.getRules() != null) {
                // Create new details if they don't exist
                ElectionDetails details = new ElectionDetails();
                details.setElectionId(electionId);
                details.setDescription(request.getDescription());
                details.setRules(request.getRules());
                electionDetailsService.createElectionDetails(details);
            }
            
            return updatedElection;
        }
        return null;
    }

    // Delete election
    public boolean deleteElection(int electionId) {
        Optional<Election> election = electionRepository.findById(electionId);
        if (election.isPresent()) {
            // Delete election details first (due to foreign key constraint)
            electionDetailsService.deleteElectionDetails(electionId);
            electionRepository.deleteById(electionId);
            return true;
        }
        return false;
    }

    // Manually trigger election status updates (for testing/admin use)
    @Transactional
    public void updateElectionStatuses() {
        updateElectionStatusesInternal();
    }
    
    // Update status for a specific election
    @Transactional
    public Election updateElectionStatus(int electionId) {
        Optional<Election> electionOpt = electionRepository.findById(electionId);
        if (electionOpt.isPresent()) {
            Election election = electionOpt.get();
            long currentTime = System.currentTimeMillis();
            String newStatus;
            
            if (currentTime < election.getStartDate()) {
                newStatus = "SCHEDULED";
            } else if (currentTime >= election.getStartDate() && currentTime <= election.getEndDate()) {
                newStatus = "OPENED";
            } else {
                newStatus = "CLOSED";
            }
            
            if (!newStatus.equals(election.getStatus())) {
                election.setStatus(newStatus);
                election = electionRepository.save(election);
                logger.info("Election '{}' status updated from {} to {}", election.getName(), election.getStatus(), newStatus);
            }
            
            return election;
        }
        return null;
    }
    
    // Automatically update election statuses based on current time
    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void updateElectionStatusesScheduled() {
        updateElectionStatusesInternal();
    }
    
    // Internal method to update election statuses
    private void updateElectionStatusesInternal() {
        long currentTime = System.currentTimeMillis();
        logger.info("Starting election status update check at {}", new java.util.Date(currentTime));
        
        try {
            // Find elections that should be started (SCHEDULED -> OPENED)
            List<Election> scheduledElections = electionRepository.findByStatus("SCHEDULED");
            logger.info("Found {} scheduled elections", scheduledElections.size());
            for (Election election : scheduledElections) {
                if (currentTime >= election.getStartDate() && currentTime <= election.getEndDate()) {
                    election.setStatus("OPENED");
                    electionRepository.save(election);
                    logger.info("Election '{}' status updated from SCHEDULED to OPENED", election.getName());
                }
            }
            
            // Find elections that should be ended (OPENED/ACTIVE -> CLOSED)
            List<Election> activeElections = electionRepository.findByStatusIn(List.of("OPENED", "ACTIVE"));
            logger.info("Found {} active elections", activeElections.size());
            for (Election election : activeElections) {
                if (currentTime > election.getEndDate()) {
                    String oldStatus = election.getStatus();
                    election.setStatus("CLOSED");
                    electionRepository.save(election);
                    logger.info("Election '{}' status updated from {} to CLOSED", election.getName(), oldStatus);
                }
            }
            logger.info("Election status update check completed");
        } catch (Exception e) {
            logger.error("Error during election status update: {}", e.getMessage(), e);
            throw e;
        }
    }
}