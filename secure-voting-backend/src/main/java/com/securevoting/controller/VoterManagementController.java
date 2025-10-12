package com.securevoting.controller;

import com.securevoting.model.User;
import com.securevoting.model.UserDetails;
import com.securevoting.model.UserRole;
import com.securevoting.payload.request.UpdateVoterStatusRequest;
import com.securevoting.service.UserService;
import com.securevoting.service.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/voters")
@CrossOrigin(origins = "*")
public class VoterManagementController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private VoterRegistrationController voterRegistrationController;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllVoters() {
        try {
            List<User> users = userService.getAllUsers();
            System.out.println("Total users found: " + users.size());
            List<Map<String, Object>> voters = new ArrayList<>();

            for (User user : users) {
                System.out.println("Checking user: " + user.getVoterId() + ", role: " + user.getRole() + ", approvalStatus: " + user.getApprovalStatus());
                if (user.getRole() == UserRole.USER) { // Only get voters (USER role)
                    Optional<UserDetails> userDetailsOpt = userDetailsService.getUserDetailsByVoterId(user.getVoterId());
                    
                    Map<String, Object> voter = new HashMap<>();
                    voter.put("voterId", user.getVoterId());
                    voter.put("email", user.getEmail());
                    voter.put("role", user.getRole());
                    voter.put("isActive", user.isActive());
                    voter.put("approvalStatus", user.getApprovalStatus());
                    voter.put("createdAt", user.getCreatedAt());
                    
                    if (userDetailsOpt.isPresent()) {
                        UserDetails userDetails = userDetailsOpt.get();
                        
                        Map<String, Object> voterDetails = new HashMap<>();
                        voterDetails.put("voterId", userDetails.getVoterId());
                        voterDetails.put("voterId", userDetails.getVoterId());
                        voterDetails.put("firstName", userDetails.getFirstName());
                        voterDetails.put("lastName", userDetails.getLastName());
                        voterDetails.put("email", userDetails.getEmail());
                        voterDetails.put("address", userDetails.getAddress());
                        voterDetails.put("phoneNumber", userDetails.getPhoneNumber());
                        voterDetails.put("gender", userDetails.getGender());
                        voterDetails.put("bloodGroup", userDetails.getBloodGroup());
                        voterDetails.put("wardId", userDetails.getWardId());
                        voterDetails.put("dob", userDetails.getDob());
                        voterDetails.put("aadharCardLink", userDetails.getAadharCardLink());
                        voterDetails.put("profilePictureLink", userDetails.getProfilePictureLink());
                        voterDetails.put("approvalStatus", user.getApprovalStatus());
                        voterDetails.put("createdAt", user.getCreatedAt());
                        
                        voter.put("voterDetails", voterDetails);
                    } else {
                        // Create empty voter details if not found
                        Map<String, Object> voterDetails = new HashMap<>();
                        voterDetails.put("voterId", "N/A");
                        voterDetails.put("voterId", user.getVoterId());
                        voterDetails.put("firstName", "N/A");
                        voterDetails.put("lastName", "N/A");
                        voterDetails.put("email", user.getEmail());
                        voterDetails.put("address", "N/A");
                        voterDetails.put("phoneNumber", "N/A");
                        voterDetails.put("gender", "N/A");
                        voterDetails.put("bloodGroup", "N/A");
                        voterDetails.put("wardId", 0);
                        voterDetails.put("dob", 0L);
                        voterDetails.put("aadharCardLink", "N/A");
                        voterDetails.put("profilePictureLink", "N/A");
                        voterDetails.put("approvalStatus", user.getApprovalStatus());
                        voterDetails.put("createdAt", user.getCreatedAt());
                        
                        voter.put("voterDetails", voterDetails);
                    }
                    
                    voters.add(voter);
                }
            }

            System.out.println("Total voters found: " + voters.size());
            return ResponseEntity.ok(voters);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{voterId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getVoterByVoterId(@PathVariable String voterId) {
        try {
            Optional<User> userOpt = userService.getUserByVoterId(voterId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            if (user.getRole() != UserRole.USER) {
                return ResponseEntity.notFound().build();
            }

            Optional<UserDetails> userDetailsOpt = userDetailsService.getUserDetailsByVoterId(voterId);
            if (userDetailsOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            UserDetails userDetails = userDetailsOpt.get();
            
            Map<String, Object> voter = new HashMap<>();
            voter.put("voterId", user.getVoterId());
            voter.put("email", user.getEmail());
            voter.put("role", user.getRole());
            voter.put("isActive", user.isActive());
            voter.put("approvalStatus", user.getApprovalStatus());
            voter.put("createdAt", user.getCreatedAt());
            
            Map<String, Object> voterDetails = new HashMap<>();
            voterDetails.put("voterId", userDetails.getVoterId());
            voterDetails.put("voterId", userDetails.getVoterId());
            voterDetails.put("firstName", userDetails.getFirstName());
            voterDetails.put("lastName", userDetails.getLastName());
            voterDetails.put("email", userDetails.getEmail());
            voterDetails.put("address", userDetails.getAddress());
            voterDetails.put("phoneNumber", userDetails.getPhoneNumber());
            voterDetails.put("gender", userDetails.getGender());
            voterDetails.put("bloodGroup", userDetails.getBloodGroup());
            voterDetails.put("wardId", userDetails.getWardId());
            voterDetails.put("dob", userDetails.getDob());
            voterDetails.put("aadharCardLink", userDetails.getAadharCardLink());
            voterDetails.put("profilePictureLink", userDetails.getProfilePictureLink());
            voterDetails.put("approvalStatus", user.getApprovalStatus());
            voterDetails.put("createdAt", user.getCreatedAt());
            
            voter.put("voterDetails", voterDetails);
            
            return ResponseEntity.ok(voter);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{voterId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateVoterStatus(
            @PathVariable String voterId, 
            @RequestBody UpdateVoterStatusRequest request) {
        try {
            Optional<User> userOpt = userService.getUserByVoterId(voterId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            if (user.getRole() != UserRole.USER) {
                return ResponseEntity.notFound().build();
            }

            // Update user approval status
            int newStatus = "APPROVED".equals(request.getStatus()) ? 1 : 
                           "REJECTED".equals(request.getStatus()) ? 0 : 2;
            user.setApprovalStatus(newStatus);
            
            User updatedUser;
            
            // If approved, also activate the user and generate dynamic password
            if (newStatus == 1) {
                user.setActive(true);
                
                // Generate a dynamic password for the approved user (only if not already set)
                String dynamicPassword;
                if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                    dynamicPassword = generateDynamicPassword(voterId);
                    user.setPassword(passwordEncoder.encode(dynamicPassword));
                    System.out.println("Generated NEW dynamic password for user: " + voterId + " -> " + dynamicPassword);
                } else {
                    // If password already exists, don't change it
                    System.out.println("User " + voterId + " already has a password set, keeping existing password");
                    dynamicPassword = "EXISTING_PASSWORD"; // Placeholder for cURL
                }
                
                // Save user with new password
                updatedUser = userService.saveUser(user);
                
                // Trigger cURL with "Voter Approved" action and include the generated password (only if new)
                try {
                    Optional<UserDetails> userDetailsForCurl = userDetailsService.getUserDetailsByVoterId(voterId);
                    if (userDetailsForCurl.isPresent()) {
                        String voterIdForCurl = userDetailsForCurl.get().getVoterId();
                        System.out.println("Voter approved, triggering approval cURL for: " + voterId + " with voter ID: " + voterIdForCurl);
                        
                        // Only send password in cURL if it's a newly generated password
                        if (!"EXISTING_PASSWORD".equals(dynamicPassword)) {
                            voterRegistrationController.createVoterApprovalTicket(voterId, voterIdForCurl, dynamicPassword);
                            System.out.println("Voter approval cURL triggered successfully with NEW dynamic password");
                        } else {
                            voterRegistrationController.createVoterApprovalTicket(voterId, voterIdForCurl, null);
                            System.out.println("Voter approval cURL triggered successfully (existing password, not sent)");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error triggering voter approval cURL: " + e.getMessage());
                    e.printStackTrace();
                    // Don't fail the approval if cURL fails
                }
            } else {
                updatedUser = userService.saveUser(user);
            }

            // Get updated user details
            Optional<UserDetails> userDetailsOpt = userDetailsService.getUserDetailsByVoterId(voterId);
            if (userDetailsOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            UserDetails userDetails = userDetailsOpt.get();
            
            Map<String, Object> voter = new HashMap<>();
            voter.put("voterId", updatedUser.getVoterId());
            voter.put("email", updatedUser.getEmail());
            voter.put("role", updatedUser.getRole());
            voter.put("isActive", updatedUser.isActive());
            voter.put("approvalStatus", updatedUser.getApprovalStatus());
            voter.put("createdAt", updatedUser.getCreatedAt());
            
            Map<String, Object> voterDetails = new HashMap<>();
            voterDetails.put("voterId", userDetails.getVoterId());
            voterDetails.put("voterId", userDetails.getVoterId());
            voterDetails.put("firstName", userDetails.getFirstName());
            voterDetails.put("lastName", userDetails.getLastName());
            voterDetails.put("email", userDetails.getEmail());
            voterDetails.put("address", userDetails.getAddress());
            voterDetails.put("phoneNumber", userDetails.getPhoneNumber());
            voterDetails.put("gender", userDetails.getGender());
            voterDetails.put("bloodGroup", userDetails.getBloodGroup());
            voterDetails.put("wardId", userDetails.getWardId());
            voterDetails.put("dob", userDetails.getDob());
            voterDetails.put("aadharCardLink", userDetails.getAadharCardLink());
            voterDetails.put("profilePictureLink", userDetails.getProfilePictureLink());
            voterDetails.put("approvalStatus", updatedUser.getApprovalStatus());
            voterDetails.put("createdAt", updatedUser.getCreatedAt());
            
            voter.put("voterDetails", voterDetails);
            
            return ResponseEntity.ok(voter);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getPendingVoters() {
        try {
            List<User> users = userService.getAllUsers();
            List<Map<String, Object>> pendingVoters = new ArrayList<>();

            for (User user : users) {
                if (user.getRole() == UserRole.USER && user.getApprovalStatus() != null && user.getApprovalStatus() == 2) {
                    Optional<UserDetails> userDetailsOpt = userDetailsService.getUserDetailsByVoterId(user.getVoterId());
                    if (userDetailsOpt.isPresent()) {
                        UserDetails userDetails = userDetailsOpt.get();
                        
                        Map<String, Object> voter = new HashMap<>();
                        voter.put("voterId", user.getVoterId());
                        voter.put("email", user.getEmail());
                        voter.put("role", user.getRole());
                        voter.put("isActive", user.isActive());
                        voter.put("approvalStatus", user.getApprovalStatus());
                        voter.put("createdAt", user.getCreatedAt());
                        
                        Map<String, Object> voterDetails = new HashMap<>();
                        voterDetails.put("voterId", userDetails.getVoterId());
                        voterDetails.put("voterId", userDetails.getVoterId());
                        voterDetails.put("firstName", userDetails.getFirstName());
                        voterDetails.put("lastName", userDetails.getLastName());
                        voterDetails.put("email", userDetails.getEmail());
                        voterDetails.put("address", userDetails.getAddress());
                        voterDetails.put("phoneNumber", userDetails.getPhoneNumber());
                        voterDetails.put("gender", userDetails.getGender());
                        voterDetails.put("bloodGroup", userDetails.getBloodGroup());
                        voterDetails.put("wardId", userDetails.getWardId());
                        voterDetails.put("dob", userDetails.getDob());
                        voterDetails.put("aadharCardLink", userDetails.getAadharCardLink());
                        voterDetails.put("profilePictureLink", userDetails.getProfilePictureLink());
                        voterDetails.put("approvalStatus", user.getApprovalStatus());
                        voterDetails.put("createdAt", user.getCreatedAt());
                        
                        voter.put("voterDetails", voterDetails);
                        pendingVoters.add(voter);
                    }
                }
            }

            return ResponseEntity.ok(pendingVoters);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/approved")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getApprovedVoters() {
        try {
            List<User> users = userService.getAllUsers();
            List<Map<String, Object>> approvedVoters = new ArrayList<>();

            for (User user : users) {
                if (user.getRole() == UserRole.USER && user.getApprovalStatus() != null && user.getApprovalStatus() == 1) {
                    Optional<UserDetails> userDetailsOpt = userDetailsService.getUserDetailsByVoterId(user.getVoterId());
                    if (userDetailsOpt.isPresent()) {
                        UserDetails userDetails = userDetailsOpt.get();
                        
                        Map<String, Object> voter = new HashMap<>();
                        voter.put("voterId", user.getVoterId());
                        voter.put("email", user.getEmail());
                        voter.put("role", user.getRole());
                        voter.put("isActive", user.isActive());
                        voter.put("approvalStatus", user.getApprovalStatus());
                        voter.put("createdAt", user.getCreatedAt());
                        
                        Map<String, Object> voterDetails = new HashMap<>();
                        voterDetails.put("voterId", userDetails.getVoterId());
                        voterDetails.put("voterId", userDetails.getVoterId());
                        voterDetails.put("firstName", userDetails.getFirstName());
                        voterDetails.put("lastName", userDetails.getLastName());
                        voterDetails.put("email", userDetails.getEmail());
                        voterDetails.put("address", userDetails.getAddress());
                        voterDetails.put("phoneNumber", userDetails.getPhoneNumber());
                        voterDetails.put("gender", userDetails.getGender());
                        voterDetails.put("bloodGroup", userDetails.getBloodGroup());
                        voterDetails.put("wardId", userDetails.getWardId());
                        voterDetails.put("dob", userDetails.getDob());
                        voterDetails.put("aadharCardLink", userDetails.getAadharCardLink());
                        voterDetails.put("profilePictureLink", userDetails.getProfilePictureLink());
                        voterDetails.put("approvalStatus", user.getApprovalStatus());
                        voterDetails.put("createdAt", user.getCreatedAt());
                        
                        voter.put("voterDetails", voterDetails);
                        approvedVoters.add(voter);
                    }
                }
            }

            return ResponseEntity.ok(approvedVoters);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generates a secure dynamic password for approved users
     * Format: 12-character random password with mixed case, numbers, and special characters
     * Example: V7p@x!Gm#2rLzQw9
     */
    private String generateDynamicPassword(String voterId) {
        // Define character sets for password generation
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "@#$%&*!?";
        
        // Combine all character sets
        String allChars = uppercase + lowercase + numbers + specialChars;
        
        // Generate a random password of length 12
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        // Ensure at least one character from each set
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));
        
        // Fill the remaining 8 characters randomly
        for (int i = 4; i < 12; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle the password to randomize positions
        String passwordStr = password.toString();
        char[] passwordArray = passwordStr.toCharArray();
        
        // Fisher-Yates shuffle algorithm
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
}
