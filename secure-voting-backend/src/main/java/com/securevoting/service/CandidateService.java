package com.securevoting.service;

import com.securevoting.model.Candidate;
import com.securevoting.model.CandidateDetails;
import com.securevoting.model.CandidateStatus;
import com.securevoting.model.Block;
import com.securevoting.payload.request.CreateCandidateRequest;
import com.securevoting.payload.request.UpdateCandidateRequest;
import com.securevoting.repository.CandidateRepository;
import com.securevoting.repository.CandidateDetailsRepository;
import com.securevoting.repository.BlockRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateDetailsRepository candidateDetailsRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private CryptoService cryptoService;

    // Get all candidates
    public List<Candidate> getAllCandidates() {
        List<Candidate> candidates = candidateRepository.findAll();
        // Ensure candidateDetails is properly initialized for each candidate
        for (Candidate candidate : candidates) {
            if (candidate.getCandidateDetails() == null) {
                // Create an empty CandidateDetails object if none exists
                CandidateDetails details = new CandidateDetails();
                details.setCandidateId(candidate.getCandidateId());
                details.setBiography(null);
                details.setManifestoSummary(null);
                candidate.setCandidateDetails(details);
            }
        }
        return candidates;
    }

    // Get only approved candidates
    public List<Candidate> getApprovedCandidates() {
        List<Candidate> candidates = candidateRepository.findByStatus(CandidateStatus.APPROVED);
        // Ensure candidateDetails is properly initialized for each candidate
        for (Candidate candidate : candidates) {
            if (candidate.getCandidateDetails() == null) {
                // Create an empty CandidateDetails object if none exists
                CandidateDetails details = new CandidateDetails();
                details.setCandidateId(candidate.getCandidateId());
                details.setBiography(null);
                details.setManifestoSummary(null);
                candidate.setCandidateDetails(details);
            }
        }
        return candidates;
    }

    // Get candidates for a specific election
    public List<Candidate> getCandidatesByElectionId(int electionId) {
        List<Candidate> candidates = candidateRepository.findByElectionId(electionId);
        // Ensure candidateDetails is properly initialized for each candidate
        for (Candidate candidate : candidates) {
            if (candidate.getCandidateDetails() == null) {
                // Create an empty CandidateDetails object if none exists
                CandidateDetails details = new CandidateDetails();
                details.setCandidateId(candidate.getCandidateId());
                details.setBiography(null);
                details.setManifestoSummary(null);
                candidate.setCandidateDetails(details);
            }
        }
        return candidates;
    }

    // Get only approved candidates for a specific election
    public List<Candidate> getApprovedCandidatesByElectionId(int electionId) {
        List<Candidate> candidates = candidateRepository.findByElectionIdAndStatus(electionId, CandidateStatus.APPROVED);
        // Ensure candidateDetails is properly initialized for each candidate
        for (Candidate candidate : candidates) {
            if (candidate.getCandidateDetails() == null) {
                // Create an empty CandidateDetails object if none exists
                CandidateDetails details = new CandidateDetails();
                details.setCandidateId(candidate.getCandidateId());
                details.setBiography(null);
                details.setManifestoSummary(null);
                candidate.setCandidateDetails(details);
            }
        }
        return candidates;
    }

    // Get candidates by party
    public List<Candidate> getCandidatesByPartyId(Integer partyId) {
        return candidateRepository.findByPartyId(partyId);
    }

    // Get candidates by ward
    public List<Candidate> getCandidatesByWardId(Integer wardId) {
        return candidateRepository.findByWardId(wardId);
    }

    // Get candidates by election and party
    public List<Candidate> getCandidatesByElectionAndParty(int electionId, Integer partyId) {
        return candidateRepository.findByElectionIdAndPartyId(electionId, partyId);
    }

    // Get candidates by election and ward
    public List<Candidate> getCandidatesByElectionAndWard(int electionId, Integer wardId) {
        return candidateRepository.findByElectionIdAndWardId(electionId, wardId);
    }

    // Get candidate by ID
    public Optional<Candidate> getCandidateById(int candidateId) {
        Optional<Candidate> candidateOpt = candidateRepository.findById(candidateId);
        if (candidateOpt.isPresent()) {
            Candidate candidate = candidateOpt.get();
            if (candidate.getCandidateDetails() == null) {
                // Create an empty CandidateDetails object if none exists
                CandidateDetails details = new CandidateDetails();
                details.setCandidateId(candidate.getCandidateId());
                details.setBiography(null);
                details.setManifestoSummary(null);
                candidate.setCandidateDetails(details);
            }
        }
        return candidateOpt;
    }

    // Create new candidate
    @Transactional
    public Candidate createCandidate(CreateCandidateRequest request) {
        // Create candidate
        Candidate candidate = new Candidate();
        candidate.setName(request.getName());
        candidate.setElectionId(request.getElectionId());
        candidate.setPartyId(request.getPartyId());
        candidate.setWardId(request.getWardId());
        
        Candidate savedCandidate = candidateRepository.save(candidate);
        
        // Create candidate details if provided
        if (request.getBiography() != null || request.getManifestoSummary() != null) {
            CandidateDetails candidateDetails = new CandidateDetails();
            candidateDetails.setCandidateId(savedCandidate.getCandidateId());
            candidateDetails.setBiography(request.getBiography());
            candidateDetails.setManifestoSummary(request.getManifestoSummary());
            candidateDetailsRepository.save(candidateDetails);
        }
        
        return savedCandidate;
    }

    // Update candidate
    @Transactional
    public Candidate updateCandidate(int candidateId, UpdateCandidateRequest request) {
        Optional<Candidate> candidateOpt = candidateRepository.findById(candidateId);
        if (candidateOpt.isEmpty()) {
            throw new RuntimeException("Candidate not found");
        }
        
        Candidate candidate = candidateOpt.get();
        candidate.setName(request.getName());
        candidate.setPartyId(request.getPartyId());
        candidate.setWardId(request.getWardId());
        
        Candidate savedCandidate = candidateRepository.save(candidate);
        
        // Update or create candidate details
        CandidateDetails candidateDetails = candidateDetailsRepository.findByCandidateId(candidateId);
        
        if (candidateDetails == null) {
            candidateDetails = new CandidateDetails();
            candidateDetails.setCandidateId(candidateId);
        }
        
        candidateDetails.setBiography(request.getBiography());
        candidateDetails.setManifestoSummary(request.getManifestoSummary());
        candidateDetailsRepository.save(candidateDetails);
        
        return savedCandidate;
    }

    // Update candidate status
    @Transactional
    public Candidate updateCandidateStatus(int candidateId, String status, String reviewNotes, String reviewedBy) {
        Optional<Candidate> candidateOpt = candidateRepository.findById(candidateId);
        if (candidateOpt.isEmpty()) {
            throw new RuntimeException("Candidate not found");
        }
        
        Candidate candidate = candidateOpt.get();
        candidate.setStatus(CandidateStatus.valueOf(status.toUpperCase()));
        candidate.setUpdatedAt(System.currentTimeMillis());
        
        Candidate savedCandidate = candidateRepository.save(candidate);
        
        // Update candidate details with review information
        CandidateDetails candidateDetails = candidateDetailsRepository.findByCandidateId(candidateId);
        if (candidateDetails != null) {
            candidateDetails.setReviewNotes(reviewNotes);
            candidateDetails.setReviewedBy(reviewedBy);
            candidateDetails.setReviewedAt(System.currentTimeMillis());
            candidateDetailsRepository.save(candidateDetails);
        }
        
        return savedCandidate;
    }

    // Delete candidate
    @Transactional
    public void deleteCandidate(int candidateId) {
        // Delete candidate details first (due to foreign key constraint)
        candidateDetailsRepository.deleteByCandidateId(candidateId);
        // Delete candidate
        candidateRepository.deleteById(candidateId);
    }

    // Check if candidate exists in election
    public boolean candidateExistsInElection(int electionId, String candidateName) {
        return candidateRepository.existsByElectionIdAndName(electionId, candidateName);
    }

    // Get candidate count for election
    public long getCandidateCountByElectionId(int electionId) {
        return candidateRepository.findByElectionId(electionId).size();
    }

    // Get candidate vote counts for a specific election
    public Map<String, Integer> getCandidateVoteCounts(int electionId) {
        Map<String, Integer> voteCounts = new HashMap<>();
        
        // Get all candidates for this election
        List<Candidate> candidates = candidateRepository.findByElectionId(electionId);
        
        // Initialize vote counts for all candidates to 0
        for (Candidate candidate : candidates) {
            voteCounts.put(candidate.getName(), 0);
        }
        
        // Get all blocks for this election
        List<Block> blocks = blockRepository.findByElectionIdOrderByBlockHeightAsc(electionId);
        
        // Process each vote block
        for (Block block : blocks) {
            if ("SYSTEM".equals(block.getVoterId())) {
                continue; // Skip Genesis Block
            }
            
            try {
                // Decrypt the vote data
                String decryptedVoteJson = cryptoService.decryptVote(block.getStegoImageData());
                Gson gson = new Gson();
                @SuppressWarnings("unchecked")
                Map<String, Object> voteMap = gson.fromJson(decryptedVoteJson, Map.class);
                String voteData = (String) voteMap.get("voteData");
                
                // Increment vote count for the voted candidate
                if (voteData != null && voteCounts.containsKey(voteData)) {
                    voteCounts.put(voteData, voteCounts.get(voteData) + 1);
                }
            } catch (Exception e) {
                System.err.println("Could not process vote from block " + block.getBlockHeight() + ". Error: " + e.getMessage());
            }
        }
        
        return voteCounts;
    }
}
