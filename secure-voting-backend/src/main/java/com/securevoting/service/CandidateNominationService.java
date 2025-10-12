package com.securevoting.service;

import com.securevoting.dto.CandidateNominationRequest;
import com.securevoting.model.Candidate;
import com.securevoting.model.CandidateDetails;
import com.securevoting.model.CandidateStatus;
import com.securevoting.model.Election;
import com.securevoting.model.PartyDetails;
import com.securevoting.repository.CandidateRepository;
import com.securevoting.repository.CandidateDetailsRepository;
import com.securevoting.repository.PartyDetailsRepository;
import com.securevoting.repository.ElectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CandidateNominationService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateDetailsRepository candidateDetailsRepository;

    @Autowired
    private PartyDetailsRepository partyDetailsRepository;

    @Autowired
    private ElectionRepository electionRepository;

    public Candidate submitNomination(CandidateNominationRequest request) {
        // Validate election exists and is active
        Optional<Election> electionOpt = electionRepository.findById(request.getElectionId());
        if (electionOpt.isEmpty()) {
            throw new IllegalArgumentException("Election not found with ID: " + request.getElectionId());
        }

        Election election = electionOpt.get();
        
        // Check if election is still accepting nominations
        long currentTime = System.currentTimeMillis();
        if (currentTime > election.getStartDate()) {
            throw new IllegalArgumentException("Election has already started. Nominations are closed.");
        }
        
        // Additional validation: ensure election is in SCHEDULED status
        if (!"SCHEDULED".equals(election.getStatus())) {
            throw new IllegalArgumentException("Election is not open for nominations. Current status: " + election.getStatus());
        }

        // Validate party exists and secret code matches
        Optional<PartyDetails> partyOpt = partyDetailsRepository.findByPartyName(request.getParty());
        if (partyOpt.isEmpty()) {
            throw new IllegalArgumentException("Party not found: " + request.getParty());
        }

        PartyDetails party = partyOpt.get();
        
        // Special handling for Independent Candidate
        if ("Independent Candidate".equals(request.getParty())) {
            if (!"INDEPENDENT_SECRET_2024".equals(request.getPartySecretCode())) {
                throw new IllegalArgumentException("Invalid secret code for Independent Candidate. Please use: INDEPENDENT_SECRET_2024");
            }
        } else {
            // For other parties, use their specific secret code
            if (!party.getPartySecretCode().equals(request.getPartySecretCode())) {
                throw new IllegalArgumentException("Invalid party secret code");
            }
        }

        // Check if candidate already exists for this election
        boolean candidateExists = candidateRepository.existsByNameAndElectionId(
            request.getCandidateName(), request.getElectionId());
        
        if (candidateExists) {
            throw new IllegalArgumentException("A candidate with this name already exists for this election");
        }

        // Create new candidate
        Candidate candidate = new Candidate();
        candidate.setName(request.getCandidateName());
        candidate.setPartyId(party.getPartyId());
        candidate.setElectionId(request.getElectionId());
        candidate.setWardId(request.getWardId());
        candidate.setStatus(CandidateStatus.PENDING);
        candidate.setCreatedAt(System.currentTimeMillis());
        candidate.setUpdatedAt(System.currentTimeMillis());

        // Save candidate
        Candidate savedCandidate = candidateRepository.save(candidate);

        // Create candidate details with personal information
        CandidateDetails candidateDetails = new CandidateDetails();
        candidateDetails.setCandidateId(savedCandidate.getCandidateId());
        candidateDetails.setEmail(request.getEmail());
        candidateDetails.setPhoneNumber(request.getPhoneNumber());
        candidateDetails.setGender(request.getGender());
        candidateDetails.setAge(request.getAge());
        candidateDetails.setAddress(request.getAddress());
        candidateDetails.setAadharCardLink(request.getAadharCardLink());
        candidateDetails.setCandidateImageLink(request.getCandidateImageLink());
        candidateDetails.setBiography("Biography not provided");
        candidateDetails.setManifestoSummary("Manifesto not provided");

        // Save candidate details
        candidateDetailsRepository.save(candidateDetails);

        return savedCandidate;
    }

    public List<Candidate> getPendingNominations() {
        return candidateRepository.findByStatus(CandidateStatus.PENDING);
    }

    public List<Candidate> getApprovedCandidates() {
        return candidateRepository.findByStatus(CandidateStatus.APPROVED);
    }

    public Candidate approveNomination(int candidateId, String reviewedBy, String reviewNotes) {
        Optional<Candidate> candidateOpt = candidateRepository.findById(candidateId);
        if (candidateOpt.isEmpty()) {
            throw new IllegalArgumentException("Candidate not found with ID: " + candidateId);
        }

        Candidate candidate = candidateOpt.get();
        candidate.setStatus(CandidateStatus.APPROVED);
        candidate.setUpdatedAt(System.currentTimeMillis());

        // Update candidate details with review information
        CandidateDetails candidateDetails = candidateDetailsRepository.findByCandidateId(candidateId);
        if (candidateDetails != null) {
            candidateDetails.setReviewedBy(reviewedBy);
            candidateDetails.setReviewNotes(reviewNotes);
            candidateDetails.setReviewedAt(System.currentTimeMillis());
            candidateDetailsRepository.save(candidateDetails);
        }

        return candidateRepository.save(candidate);
    }

    public Candidate rejectNomination(int candidateId, String reviewedBy, String reviewNotes, String reason) {
        Optional<Candidate> candidateOpt = candidateRepository.findById(candidateId);
        if (candidateOpt.isEmpty()) {
            throw new IllegalArgumentException("Candidate not found with ID: " + candidateId);
        }

        Candidate candidate = candidateOpt.get();
        candidate.setStatus(CandidateStatus.REJECTED);
        candidate.setUpdatedAt(System.currentTimeMillis());

        // Update candidate details with review information
        CandidateDetails candidateDetails = candidateDetailsRepository.findByCandidateId(candidateId);
        if (candidateDetails != null) {
            candidateDetails.setReviewedBy(reviewedBy);
            candidateDetails.setReviewNotes(reviewNotes + " - Reason: " + reason);
            candidateDetails.setReviewedAt(System.currentTimeMillis());
            candidateDetailsRepository.save(candidateDetails);
        }

        return candidateRepository.save(candidate);
    }

    public Optional<Candidate> getCandidateById(int candidateId) {
        return candidateRepository.findById(candidateId);
    }

    public List<Candidate> getCandidatesByElection(int electionId) {
        return candidateRepository.findByElectionId(electionId);
    }

    public List<Candidate> getCandidatesByStatus(CandidateStatus status) {
        return candidateRepository.findByStatus(status);
    }
}
