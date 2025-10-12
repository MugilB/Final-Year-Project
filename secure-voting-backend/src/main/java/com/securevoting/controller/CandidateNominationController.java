package com.securevoting.controller;

import com.securevoting.dto.CandidateNominationRequest;
import com.securevoting.model.Candidate;
import com.securevoting.service.CandidateNominationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/candidate-nominations")
@CrossOrigin(origins = "*")
public class CandidateNominationController {

    @Autowired
    private CandidateNominationService candidateNominationService;

    @PostMapping
    public ResponseEntity<?> submitNomination(@Valid @RequestBody CandidateNominationRequest request) {
        try {
            Candidate savedCandidate = candidateNominationService.submitNomination(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Candidate nomination submitted successfully");
            response.put("candidateId", savedCandidate.getCandidateId());
            response.put("status", "PENDING");
            response.put("submittedAt", savedCandidate.getCreatedAt());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to submit nomination: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingNominations() {
        try {
            return ResponseEntity.ok(candidateNominationService.getPendingNominations());
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch pending nominations: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PutMapping("/{candidateId}/approve")
    public ResponseEntity<?> approveNomination(@PathVariable int candidateId, @RequestBody Map<String, String> request) {
        try {
            String reviewedBy = request.getOrDefault("reviewedBy", "admin");
            String reviewNotes = request.getOrDefault("reviewNotes", "");
            
            Candidate updatedCandidate = candidateNominationService.approveNomination(candidateId, reviewedBy, reviewNotes);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Candidate nomination approved successfully");
            response.put("candidateId", updatedCandidate.getCandidateId());
            response.put("status", "APPROVED");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to approve nomination: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PutMapping("/{candidateId}/reject")
    public ResponseEntity<?> rejectNomination(@PathVariable int candidateId, @RequestBody Map<String, String> request) {
        try {
            String reviewedBy = request.getOrDefault("reviewedBy", "admin");
            String reviewNotes = request.getOrDefault("reviewNotes", "");
            String reason = request.getOrDefault("reason", "No reason provided");
            
            Candidate updatedCandidate = candidateNominationService.rejectNomination(candidateId, reviewedBy, reviewNotes, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Candidate nomination rejected");
            response.put("candidateId", updatedCandidate.getCandidateId());
            response.put("status", "REJECTED");
            response.put("reason", reason);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reject nomination: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
