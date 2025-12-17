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
    private UnifiedCryptoService cryptoService;

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
        if (request.getElectionId() != null) {
            candidate.setElectionId(request.getElectionId());
        }
        candidate.setPartyId(request.getPartyId());
        candidate.setWardId(request.getWardId());
        
        Candidate savedCandidate = candidateRepository.save(candidate);
        
        // Update candidate details if they exist
        // We only update existing candidateDetails to avoid constraint violations
        CandidateDetails candidateDetails = candidateDetailsRepository.findByCandidateId(candidateId);
        
        if (candidateDetails != null) {
            boolean detailsUpdated = false;
            
            System.out.println("Updating candidate details for candidateId: " + candidateId);
            System.out.println("Request email: " + request.getEmail());
            System.out.println("Request phoneNumber: " + request.getPhoneNumber());
            System.out.println("Request gender: " + request.getGender());
            System.out.println("Request age: " + request.getAge());
            System.out.println("Request address: " + request.getAddress());
            
            // Update biography and manifesto - allow empty strings to clear the field
            if (request.getBiography() != null) {
                String bioValue = request.getBiography().trim();
                candidateDetails.setBiography(bioValue.isEmpty() ? null : bioValue);
                detailsUpdated = true;
            }
            if (request.getManifestoSummary() != null) {
                String manifestoValue = request.getManifestoSummary().trim();
                candidateDetails.setManifestoSummary(manifestoValue.isEmpty() ? null : manifestoValue);
                detailsUpdated = true;
            }
            
            // Update personal information fields if provided (required fields)
            // In edit mode, these fields should always be provided and non-empty (validated on frontend)
            if (request.getEmail() != null) {
                String emailValue = request.getEmail().trim();
                if (!emailValue.isEmpty()) {
                    candidateDetails.setEmail(emailValue);
                    detailsUpdated = true;
                }
            }
            if (request.getPhoneNumber() != null) {
                String phoneValue = request.getPhoneNumber().trim();
                if (!phoneValue.isEmpty()) {
                    candidateDetails.setPhoneNumber(phoneValue);
                    detailsUpdated = true;
                }
            }
            if (request.getGender() != null) {
                String genderValue = request.getGender().trim();
                if (!genderValue.isEmpty()) {
                    candidateDetails.setGender(genderValue);
                    detailsUpdated = true;
                }
            }
            if (request.getAge() != null && request.getAge() > 0) {
                candidateDetails.setAge(request.getAge());
                detailsUpdated = true;
            }
            if (request.getAddress() != null) {
                String addressValue = request.getAddress().trim();
                if (!addressValue.isEmpty()) {
                    candidateDetails.setAddress(addressValue);
                    detailsUpdated = true;
                }
            }
            if (request.getAadharCardLink() != null) {
                String aadharValue = request.getAadharCardLink().trim();
                if (!aadharValue.isEmpty()) {
                    candidateDetails.setAadharCardLink(aadharValue);
                    detailsUpdated = true;
                }
            }
            // Optional field - allow empty string to clear it
            if (request.getCandidateImageLink() != null) {
                String imageValue = request.getCandidateImageLink().trim();
                candidateDetails.setCandidateImageLink(imageValue.isEmpty() ? null : imageValue);
                detailsUpdated = true;
            }
            
            // Save candidateDetails if any fields were updated
            if (detailsUpdated) {
                System.out.println("Saving candidateDetails with updates...");
                candidateDetailsRepository.save(candidateDetails);
                System.out.println("CandidateDetails saved successfully");
            } else {
                System.out.println("No candidateDetails fields were updated");
            }
            
            // Reload candidateDetails to ensure it's attached to the candidate for response
            candidateDetails = candidateDetailsRepository.findByCandidateId(candidateId);
            if (candidateDetails != null) {
                savedCandidate.setCandidateDetails(candidateDetails);
                System.out.println("CandidateDetails attached to candidate response");
            }
        } else {
            // If candidateDetails doesn't exist, we can only create it if all required fields are provided
            // For now, we'll skip creating it to avoid constraint violations
            // In the future, we could add validation to ensure all required fields are present
            System.err.println("Warning: CandidateDetails not found for candidateId: " + candidateId + ". Cannot update details.");
        }
        
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
        
        // Validate and convert status
        try {
            candidate.setStatus(CandidateStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status, e);
        }
        
        candidate.setUpdatedAt(System.currentTimeMillis());
        
        // IMPORTANT: Clear candidateDetails reference BEFORE saving to prevent cascade issues
        // The CascadeType.ALL on Candidate.candidateDetails can cause Hibernate to try
        // to persist a new CandidateDetails even if it's null or transient
        candidate.setCandidateDetails(null);
        
        // Save candidate first - this is the primary operation
        Candidate savedCandidate = candidateRepository.save(candidate);
        
        // Now try to update candidate details separately
        // This is a secondary operation and should not block the status update
        updateCandidateDetailsReviewInfo(candidateId, reviewNotes, reviewedBy);
        
        return savedCandidate;
    }
    
    /**
     * Helper method to update candidate details review information.
     * This is separated to ensure it doesn't block the candidate status update.
     * If candidateDetails doesn't exist, this method silently skips the update.
     * This method is NOT transactional to prevent it from affecting the main transaction.
     */
    @Transactional(noRollbackFor = {org.springframework.dao.DataIntegrityViolationException.class, Exception.class})
    private void updateCandidateDetailsReviewInfo(int candidateId, String reviewNotes, String reviewedBy) {
        try {
            // Try to load candidateDetails - if it doesn't exist, findByCandidateId returns null
            // We don't use existsByCandidateId as it might trigger a flush that causes issues
            CandidateDetails candidateDetails = candidateDetailsRepository.findByCandidateId(candidateId);
            
            if (candidateDetails != null) {
                // Only update review-related fields, don't modify required fields
                if (reviewNotes != null) {
                    candidateDetails.setReviewNotes(reviewNotes);
                }
                if (reviewedBy != null) {
                    candidateDetails.setReviewedBy(reviewedBy);
                }
                candidateDetails.setReviewedAt(System.currentTimeMillis());
                // Save the existing entity (this should be an UPDATE, not INSERT)
                candidateDetailsRepository.save(candidateDetails);
            }
            // If candidateDetails is null, it doesn't exist - this is fine, just skip silently
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Specifically catch constraint violations and log but don't fail
            // This can happen if Hibernate tries to INSERT instead of UPDATE
            System.err.println("Warning: Could not update candidate details - constraint violation: " + e.getMessage());
            System.err.println("This is expected if candidateDetails doesn't exist or has missing required fields.");
            System.err.println("Candidate status was updated successfully.");
        } catch (Exception e) {
            // Log other errors but don't fail
            System.err.println("Warning: Could not update candidate details review information: " + e.getMessage());
            System.err.println("Candidate status was updated successfully.");
            e.printStackTrace();
        }
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
