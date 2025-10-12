package com.securevoting.controller;

import com.securevoting.model.Election;
import com.securevoting.model.ElectionDetails;
import com.securevoting.service.ElectionService;
import com.securevoting.service.ElectionDetailsService;
import com.securevoting.dto.CreateElectionRequest;
import com.securevoting.dto.UpdateElectionRequest;
import com.securevoting.dto.ElectionWithDetailsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/elections")
public class ElectionController {

    @Autowired
    private ElectionService electionService;

    @Autowired
    private ElectionDetailsService electionDetailsService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Election> createElection(@RequestBody CreateElectionRequest request) {
        return ResponseEntity.ok(electionService.createElection(request));
    }

    @GetMapping
    public List<ElectionWithDetailsResponse> getAllElections() {
        return electionService.getAllElectionsWithDetails();
    }

    @GetMapping("/open")
    public List<Election> getOpenElections() {
        return electionService.getOpenElections();
    }

    @GetMapping("/for-nominations")
    public List<Election> getElectionsForNominations() {
        return electionService.getElectionsForNominations();
    }

    @GetMapping("/with-candidates")
    public List<Election> getAllElectionsWithCandidates() {
        return electionService.getAllElectionsWithCandidates();
    }

    @GetMapping("/with-approved-candidates")
    public List<Election> getAllElectionsWithApprovedCandidates() {
        return electionService.getAllElectionsWithApprovedCandidates();
    }

    @GetMapping("/{electionId}/with-candidates")
    public ResponseEntity<Election> getElectionWithCandidates(@PathVariable int electionId) {
        Election election = electionService.getElectionWithCandidates(electionId);
        return election != null ? ResponseEntity.ok(election) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{electionId}/with-approved-candidates")
    public ResponseEntity<Election> getElectionWithApprovedCandidates(@PathVariable int electionId) {
        Election election = electionService.getElectionWithApprovedCandidates(electionId);
        return election != null ? ResponseEntity.ok(election) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{electionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Election> updateElection(@PathVariable int electionId, @RequestBody UpdateElectionRequest request) {
        Election updatedElection = electionService.updateElection(electionId, request);
        return updatedElection != null ? ResponseEntity.ok(updatedElection) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{electionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteElection(@PathVariable int electionId) {
        boolean deleted = electionService.deleteElection(electionId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{electionId}/details")
    public ResponseEntity<ElectionDetails> getElectionDetails(@PathVariable int electionId) {
        return electionDetailsService.getElectionDetailsByElectionId(electionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/update-statuses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateElectionStatuses() {
        try {
            System.out.println("Received request to update election statuses");
            electionService.updateElectionStatuses();
            System.out.println("Election statuses updated successfully");
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                    .body("Election statuses updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating election statuses: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                    .body("Error updating election statuses: " + e.getMessage());
        }
    }
    
    @PostMapping("/{electionId}/update-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Election> updateElectionStatus(@PathVariable int electionId) {
        try {
            System.out.println("Received request to update election status for ID: " + electionId);
            Election updatedElection = electionService.updateElectionStatus(electionId);
            if (updatedElection != null) {
                System.out.println("Election status updated successfully for: " + updatedElection.getName());
                return ResponseEntity.ok(updatedElection);
            } else {
                System.out.println("Election not found with ID: " + electionId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error updating election status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                .body("Backend is working!");
    }
}