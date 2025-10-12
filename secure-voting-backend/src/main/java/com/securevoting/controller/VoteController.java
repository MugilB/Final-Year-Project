package com.securevoting.controller;

import com.securevoting.model.Block;
import com.securevoting.model.Candidate;
import com.securevoting.model.Election;
import com.securevoting.model.UserDetails;
import com.securevoting.repository.BlockRepository;
import com.securevoting.repository.CandidateRepository;
import com.securevoting.repository.ElectionRepository;
import com.securevoting.repository.UserDetailsRepository;
import com.securevoting.security.services.UserDetailsImpl;
import com.securevoting.service.CryptoService;
import com.securevoting.service.SteganographyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/votes")
@CrossOrigin(origins = "*")
public class VoteController {

    @Autowired
    private ElectionRepository electionRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private SteganographyService steganographyService;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @GetMapping("/debug-auth")
    public ResponseEntity<Map<String, Object>> debugAuth() {
        Map<String, Object> response = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            response.put("authenticated", false);
            response.put("error", "No authentication found");
        } else {
            response.put("authenticated", authentication.isAuthenticated());
            response.put("principalType", authentication.getPrincipal().getClass().getSimpleName());
            response.put("principal", authentication.getPrincipal().toString());
            response.put("authorities", authentication.getAuthorities().toString());
            
            // Get voter_id and check if user details exist
            Object principal = authentication.getPrincipal();
            String voterId;
            if (principal instanceof UserDetailsImpl) {
                voterId = ((UserDetailsImpl) principal).getUsername(); // This is now voter_id
            } else if (principal instanceof String) {
                voterId = (String) principal;
            } else {
                voterId = "unknown";
            }
            
            response.put("voterId", voterId);
            Optional<UserDetails> userDetails = userDetailsRepository.findByVoterId(voterId);
            response.put("userDetailsExists", userDetails.isPresent());
            if (userDetails.isPresent()) {
                response.put("userDetailsVoterId", userDetails.get().getVoterId());
            }
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-user-details")
    public ResponseEntity<Map<String, Object>> createUserDetails() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not authenticated");
                return ResponseEntity.status(401).body(errorResponse);
            }

            // Get voter_id
            Object principal = authentication.getPrincipal();
            String voterId;
            if (principal instanceof UserDetailsImpl) {
                voterId = ((UserDetailsImpl) principal).getUsername(); // This is now voter_id
            } else if (principal instanceof String) {
                voterId = (String) principal;
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Unsupported authentication type");
                return ResponseEntity.status(401).body(errorResponse);
            }

            // Check if user details already exist
            Optional<UserDetails> existingUserDetails = userDetailsRepository.findByVoterId(voterId);
            if (existingUserDetails.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "User details already exist");
                response.put("voterId", existingUserDetails.get().getVoterId());
                return ResponseEntity.ok(response);
            }

            // Create new user details
            UserDetails userDetails = new UserDetails();
            userDetails.setVoterId(voterId);
            userDetails.setUserVoterId(voterId);
            userDetails.setNoElectionsVoted(0);
            
            userDetailsRepository.save(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User details created successfully");
            response.put("voterId", voterId);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create user details: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitVote(@RequestBody Map<String, Object> voteRequest) {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not authenticated");
                return ResponseEntity.status(401).body(errorResponse);
            }

            // Handle different authentication principal types
            Object principal = authentication.getPrincipal();
            String voterId;
            
            if (principal instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) principal;
                voterId = userDetails.getUsername(); // This is now voter_id
            } else if (principal instanceof String) {
                // Handle case where principal is just the voter_id string
                voterId = (String) principal;
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Unsupported authentication type: " + principal.getClass().getSimpleName());
                return ResponseEntity.status(401).body(errorResponse);
            }

            // Get user details using voter_id
            System.out.println("Looking for user details with voter_id: " + voterId);
            Optional<UserDetails> userDetailsOpt = userDetailsRepository.findByVoterId(voterId);
            if (userDetailsOpt.isEmpty()) {
                System.out.println("No user details found for voter_id: " + voterId);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User details not found for voter_id: " + voterId);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            System.out.println("Found user details for voter_id: " + voterId);

            String userVoterId = userDetailsOpt.get().getVoterId();
            Integer electionId = null;
            Integer candidateId = null;
            
            // Safely convert electionId to Integer
            Object electionIdObj = voteRequest.get("electionId");
            if (electionIdObj != null) {
                try {
                    if (electionIdObj instanceof Integer) {
                        electionId = (Integer) electionIdObj;
                    } else if (electionIdObj instanceof String) {
                        electionId = Integer.valueOf((String) electionIdObj);
                    } else if (electionIdObj instanceof Number) {
                        electionId = ((Number) electionIdObj).intValue();
                    }
                } catch (NumberFormatException e) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid election ID format");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }
            
            // Safely convert candidateId to Integer
            Object candidateIdObj = voteRequest.get("candidateId");
            if (candidateIdObj != null) {
                try {
                    if (candidateIdObj instanceof Integer) {
                        candidateId = (Integer) candidateIdObj;
                    } else if (candidateIdObj instanceof String) {
                        candidateId = Integer.valueOf((String) candidateIdObj);
                    } else if (candidateIdObj instanceof Number) {
                        candidateId = ((Number) candidateIdObj).intValue();
                    }
                } catch (NumberFormatException e) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid candidate ID format");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }

            // Validate input
            if (userVoterId == null || userVoterId.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Voter ID is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (electionId == null || candidateId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Election ID and Candidate ID are required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Check if election exists
            Optional<Election> electionOpt = electionRepository.findById(electionId);
            if (electionOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Election not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Check if candidate exists and belongs to the election
            Optional<Candidate> candidateOpt = candidateRepository.findById(candidateId);
            if (candidateOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Candidate not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Candidate candidate = candidateOpt.get();
            if (candidate.getElectionId() != electionId) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Candidate does not belong to the specified election");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Check if voter has already voted in this election
            boolean hasVoted = blockRepository.findByVoterIdAndElectionId(userVoterId, electionId).isPresent();
            if (hasVoted) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "You have already voted in this election");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Create vote data
            Election election = electionOpt.get();
            String voteJson = String.format(
                "{\"voterId\":\"%s\", \"voteData\":\"%s\", \"electionId\":%d, \"electionName\":\"%s\"}",
                userVoterId, candidate.getName(), electionId, election.getName()
            );

            // Encrypt the vote using hybrid cryptography
            String encryptedPayloadJson = cryptoService.encryptVote(voteJson);

            // Hide the encrypted data in steganographic image
            byte[] stegoImageData = steganographyService.embedData(encryptedPayloadJson.getBytes());

            // Get previous block hash and calculate new block height
            String previousHash = blockRepository.findTopByOrderByBlockHeightDesc()
                .map(Block::getHash)
                .orElse("0");
            
            int newBlockHeight = blockRepository.findTopByOrderByBlockHeightDesc()
                .map(b -> b.getBlockHeight() + 1)
                .orElse(0);

            // Create new block
            Block newBlock = new Block(
                "Encrypted vote saved in stego_image_data.",
                previousHash,
                userVoterId,
                newBlockHeight,
                electionId,
                election.getName()
            );

            // Mine the block (simplified - just calculate hash)
            newBlock.mineBlock(4);
            newBlock.setStegoImageData(stegoImageData);

            // Save block to database
            blockRepository.save(newBlock);

            // Update user's vote count and last election voted
            UserDetails userDetailsToUpdate = userDetailsOpt.get();
            userDetailsToUpdate.setNoElectionsVoted(
                (userDetailsToUpdate.getNoElectionsVoted() == null ? 0 : userDetailsToUpdate.getNoElectionsVoted()) + 1
            );
            userDetailsToUpdate.setLastElectionVoted(election.getName());
            userDetailsRepository.save(userDetailsToUpdate);

            // Prepare success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vote submitted successfully");
            response.put("blockHeight", newBlock.getBlockHeight());
            response.put("blockHash", newBlock.getHash());
            response.put("electionName", election.getName());
            response.put("candidateName", candidate.getName());
            response.put("timestamp", newBlock.getTimestamp());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error submitting vote: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to submit vote: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/status/{voterId}/{electionId}")
    public ResponseEntity<Map<String, Object>> getVoteStatus(@PathVariable String voterId, @PathVariable Integer electionId) {
        try {
            Optional<Block> voteBlock = blockRepository.findByVoterIdAndElectionId(voterId, electionId);
            
            Map<String, Object> response = new HashMap<>();
            if (voteBlock.isPresent()) {
                Block block = voteBlock.get();
                response.put("hasVoted", true);
                response.put("blockHeight", block.getBlockHeight());
                response.put("timestamp", block.getTimestamp());
                response.put("electionName", block.getElectionName());
            } else {
                response.put("hasVoted", false);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error checking vote status: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to check vote status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}