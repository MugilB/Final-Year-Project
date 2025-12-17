package com.securevoting.controller;

import com.securevoting.model.Candidate;
import com.securevoting.payload.request.CreateCandidateRequest;
import com.securevoting.payload.request.UpdateCandidateRequest;
import com.securevoting.payload.request.UpdateCandidateStatusRequest;
import com.securevoting.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    // Get all candidates
    @GetMapping
    public List<Candidate> getAllCandidates() {
        return candidateService.getAllCandidates();
    }

    // Get only approved candidates
    @GetMapping("/approved")
    public List<Candidate> getApprovedCandidates() {
        return candidateService.getApprovedCandidates();
    }

    // Get candidates for a specific election
    @GetMapping("/election/{electionId}")
    public List<Candidate> getCandidatesByElectionId(@PathVariable int electionId) {
        return candidateService.getCandidatesByElectionId(electionId);
    }

    // Get only approved candidates for a specific election
    @GetMapping("/election/{electionId}/approved")
    public List<Candidate> getApprovedCandidatesByElectionId(@PathVariable int electionId) {
        return candidateService.getApprovedCandidatesByElectionId(electionId);
    }

    // Get candidates by party
    @GetMapping("/party/{partyId}")
    public List<Candidate> getCandidatesByPartyId(@PathVariable Integer partyId) {
        return candidateService.getCandidatesByPartyId(partyId);
    }

    // Get candidates by ward
    @GetMapping("/ward/{wardId}")
    public List<Candidate> getCandidatesByWardId(@PathVariable Integer wardId) {
        return candidateService.getCandidatesByWardId(wardId);
    }

    // Get candidates by election and party
    @GetMapping("/election/{electionId}/party/{partyId}")
    public List<Candidate> getCandidatesByElectionAndParty(
            @PathVariable int electionId, 
            @PathVariable Integer partyId) {
        return candidateService.getCandidatesByElectionAndParty(electionId, partyId);
    }

    // Get candidates by election and ward
    @GetMapping("/election/{electionId}/ward/{wardId}")
    public List<Candidate> getCandidatesByElectionAndWard(
            @PathVariable int electionId, 
            @PathVariable Integer wardId) {
        return candidateService.getCandidatesByElectionAndWard(electionId, wardId);
    }

    // Get candidate by ID
    @GetMapping("/{candidateId}")
    public ResponseEntity<Candidate> getCandidateById(@PathVariable int candidateId) {
        Optional<Candidate> candidate = candidateService.getCandidateById(candidateId);
        return candidate.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    // Create new candidate
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Candidate> createCandidate(@RequestBody CreateCandidateRequest request) {
        try {
            // Check if candidate already exists in the election
            if (candidateService.candidateExistsInElection(request.getElectionId(), request.getName())) {
                return ResponseEntity.badRequest().build();
            }
            
            Candidate createdCandidate = candidateService.createCandidate(request);
            return ResponseEntity.ok(createdCandidate);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Update candidate
    @PutMapping("/{candidateId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Candidate> updateCandidate(
            @PathVariable int candidateId, 
            @RequestBody UpdateCandidateRequest request) {
        try {
            if (!candidateService.getCandidateById(candidateId).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Candidate updatedCandidate = candidateService.updateCandidate(candidateId, request);
            return ResponseEntity.ok(updatedCandidate);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Delete candidate
    @DeleteMapping("/{candidateId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCandidate(@PathVariable int candidateId) {
        try {
            if (!candidateService.getCandidateById(candidateId).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            candidateService.deleteCandidate(candidateId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Update candidate status
    @PutMapping("/{candidateId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCandidateStatus(
            @PathVariable int candidateId, 
            @RequestBody UpdateCandidateStatusRequest request) {
        try {
            if (!candidateService.getCandidateById(candidateId).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            // Validate request
            if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Status is required");
            }
            
            Candidate updatedCandidate = candidateService.updateCandidateStatus(
                candidateId, 
                request.getStatus(), 
                request.getReviewNotes(), 
                request.getReviewedBy()
            );
            return ResponseEntity.ok(updatedCandidate);
        } catch (RuntimeException e) {
            e.printStackTrace();
            // Return error message in response body for better debugging
            return ResponseEntity.status(500).body("Error updating candidate status: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }

    // Get candidate count for election
    @GetMapping("/election/{electionId}/count")
    public ResponseEntity<Long> getCandidateCountByElectionId(@PathVariable int electionId) {
        long count = candidateService.getCandidateCountByElectionId(electionId);
        return ResponseEntity.ok(count);
    }

    // Get candidate vote counts for an election
    @GetMapping("/election/{electionId}/vote-counts")
    public ResponseEntity<Map<String, Integer>> getCandidateVoteCounts(@PathVariable int electionId) {
        try {
            Map<String, Integer> voteCounts = candidateService.getCandidateVoteCounts(electionId);
            return ResponseEntity.ok(voteCounts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
