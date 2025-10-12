package com.securevoting.controller;

import com.securevoting.model.User;
import com.securevoting.model.UserDetails;
import com.securevoting.model.UserRole;
import com.securevoting.repository.UserRepository;
import com.securevoting.repository.UserDetailsRepository;
import com.securevoting.dto.CreateUserRequest;
import com.securevoting.dto.UpdateUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            List<Map<String, Object>> usersWithDetails = new ArrayList<>();
            
            for (User user : users) {
                Map<String, Object> userData = new HashMap<>();
                
                // Add basic user information
                userData.put("voterId", user.getVoterId());
                userData.put("email", user.getEmail());
                userData.put("role", user.getRole());
                userData.put("active", user.isActive());
                userData.put("approvalStatus", user.getApprovalStatus());
                userData.put("createdAt", user.getCreatedAt());
                userData.put("lastLogin", user.getLastLogin());
                
                // Try to get user details
                System.out.println("Looking for UserDetails with voterId: " + user.getVoterId());
                Optional<UserDetails> userDetailsOpt = userDetailsRepository.findByVoterId(user.getVoterId());
                if (userDetailsOpt.isPresent()) {
                    UserDetails userDetails = userDetailsOpt.get();
                    System.out.println("Found UserDetails for " + user.getVoterId() + ": " + 
                                     "firstName=" + userDetails.getFirstName() + 
                                     ", lastName=" + userDetails.getLastName() + 
                                     ", phoneNumber=" + userDetails.getPhoneNumber());
                    
                    userData.put("firstName", userDetails.getFirstName());
                    userData.put("lastName", userDetails.getLastName());
                    userData.put("phoneNumber", userDetails.getPhoneNumber());
                    userData.put("dateOfBirth", userDetails.getDob());
                    userData.put("gender", userDetails.getGender());
                    userData.put("address", userDetails.getAddress());
                    userData.put("wardId", userDetails.getWardId());
                    userData.put("bloodGroup", userDetails.getBloodGroup());
                    userData.put("aadharCardLink", userDetails.getAadharCardLink());
                    userData.put("profilePictureLink", userDetails.getProfilePictureLink());
                } else {
                    System.out.println("No UserDetails found for voterId: " + user.getVoterId());
                    // Set default values if no user details found
                    userData.put("firstName", null);
                    userData.put("lastName", null);
                    userData.put("phoneNumber", null);
                    userData.put("dateOfBirth", null);
                    userData.put("gender", null);
                    userData.put("address", null);
                    userData.put("wardId", null);
                    userData.put("bloodGroup", null);
                    userData.put("aadharCardLink", null);
                    userData.put("profilePictureLink", null);
                }
                
                usersWithDetails.add(userData);
            }
            
            return ResponseEntity.ok(usersWithDetails);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Temporarily disabled - needs to be updated for VoterID system
    // @PostMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        try {
            // Check if email already exists
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Generate voter ID
            String voterId = "VOTER_" + System.currentTimeMillis();
            
            // Create user
            User user = new User();
            user.setVoterId(voterId);
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            
            // Convert roles list to UserRole enum and string
            String roleString = request.getRoles().get(0); // Take first role
            try {
                UserRole role = UserRole.valueOf(roleString.toUpperCase());
                user.setRole(role);
                user.setRoles(roleString.toUpperCase()); // Also set the string field
            } catch (IllegalArgumentException e) {
                user.setRole(UserRole.USER); // Default to USER
                user.setRoles("USER"); // Also set the string field
            }
            
            user.setCreatedAt(System.currentTimeMillis());
            user.setActive(true);

            User savedUser = userRepository.save(user);

            // Create user details (using the existing UserDetails model)
            UserDetails userDetails = new UserDetails();
            userDetails.setVoterId(voterId);
            userDetails.setUserVoterId(voterId); // Reference to users table
            userDetails.setEmail(request.getEmail()); // Set email in UserDetails
            userDetails.setNoElectionsVoted(0);

            userDetailsRepository.save(userDetails);

            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            e.printStackTrace(); // Log the actual error
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<User> updateUser(@PathVariable String userId, @RequestBody UpdateUserRequest request) {
        try {
            System.out.println("Attempting to update user with ID: " + userId);
            
            // Find user by voterId (which is the primary key)
            Optional<User> userOpt = userRepository.findByVoterId(userId);
            if (userOpt.isEmpty()) {
                System.out.println("User not found with voterId: " + userId);
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            System.out.println("Found user: " + user.getVoterId() + " - " + user.getEmail());
            
            // Update fields if provided
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                user.setEmail(request.getEmail());
                System.out.println("Updated email to: " + request.getEmail());
            }
            
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                System.out.println("Updated password");
            }
            
            if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                String roleString = request.getRoles().get(0);
                try {
                    UserRole role = UserRole.valueOf(roleString.toUpperCase());
                    user.setRole(role);
                    user.setRoles(roleString.toUpperCase());
                    System.out.println("Updated role to: " + roleString);
                } catch (IllegalArgumentException e) {
                    user.setRole(UserRole.USER);
                    user.setRoles("USER");
                    System.out.println("Invalid role, defaulting to USER");
                }
            }
            
            if (request.getApprovalStatus() != null) {
                user.setApprovalStatus(request.getApprovalStatus());
                System.out.println("Updated approval status to: " + request.getApprovalStatus());
            }

            User savedUser = userRepository.save(user);
            System.out.println("User updated successfully: " + savedUser.getVoterId());
            
            // Also update UserDetails table if personal information is provided
            Optional<UserDetails> userDetailsOpt = userDetailsRepository.findByVoterId(userId);
            if (userDetailsOpt.isPresent()) {
                UserDetails userDetails = userDetailsOpt.get();
                boolean userDetailsUpdated = false;
                
                if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
                    userDetails.setFirstName(request.getFirstName());
                    userDetailsUpdated = true;
                    System.out.println("Updated firstName in UserDetails: " + request.getFirstName());
                }
                
                if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
                    userDetails.setLastName(request.getLastName());
                    userDetailsUpdated = true;
                    System.out.println("Updated lastName in UserDetails: " + request.getLastName());
                }
                
                if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
                    userDetails.setPhoneNumber(request.getPhoneNumber());
                    userDetailsUpdated = true;
                    System.out.println("Updated phoneNumber in UserDetails: " + request.getPhoneNumber());
                }
                
                if (request.getDateOfBirth() != null) {
                    userDetails.setDob(request.getDateOfBirth());
                    userDetailsUpdated = true;
                    System.out.println("Updated dateOfBirth in UserDetails: " + request.getDateOfBirth());
                }
                
                if (request.getGender() != null && !request.getGender().trim().isEmpty()) {
                    userDetails.setGender(request.getGender());
                    userDetailsUpdated = true;
                    System.out.println("Updated gender in UserDetails: " + request.getGender());
                }
                
                if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
                    userDetails.setAddress(request.getAddress());
                    userDetailsUpdated = true;
                    System.out.println("Updated address in UserDetails: " + request.getAddress());
                }
                
                if (request.getWardId() != null) {
                    userDetails.setWardId(request.getWardId());
                    userDetailsUpdated = true;
                    System.out.println("Updated wardId in UserDetails: " + request.getWardId());
                }
                
                if (request.getBloodGroup() != null && !request.getBloodGroup().trim().isEmpty()) {
                    userDetails.setBloodGroup(request.getBloodGroup());
                    userDetailsUpdated = true;
                    System.out.println("Updated bloodGroup in UserDetails: " + request.getBloodGroup());
                }
                
                if (request.getAadharCardLink() != null && !request.getAadharCardLink().trim().isEmpty()) {
                    userDetails.setAadharCardLink(request.getAadharCardLink());
                    userDetailsUpdated = true;
                    System.out.println("Updated aadharCardLink in UserDetails: " + request.getAadharCardLink());
                }
                
                if (request.getProfilePictureLink() != null && !request.getProfilePictureLink().trim().isEmpty()) {
                    userDetails.setProfilePictureLink(request.getProfilePictureLink());
                    userDetailsUpdated = true;
                    System.out.println("Updated profilePictureLink in UserDetails: " + request.getProfilePictureLink());
                }
                
                if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                    userDetails.setEmail(request.getEmail());
                    userDetailsUpdated = true;
                    System.out.println("Updated email in UserDetails: " + request.getEmail());
                }
                
                if (userDetailsUpdated) {
                    userDetailsRepository.save(userDetails);
                    System.out.println("UserDetails updated successfully for voterId: " + userId);
                }
            } else {
                System.out.println("UserDetails not found for voterId: " + userId);
            }

            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        try {
            System.out.println("Attempting to delete user with voterId: " + userId);
            
            // Find user by voterId
            Optional<User> userOpt = userRepository.findByVoterId(userId);
            if (userOpt.isEmpty()) {
                System.out.println("User not found with voterId: " + userId);
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            System.out.println("Found user to delete: " + user.getVoterId() + " - " + user.getEmail());
            
            // Delete user
            userRepository.delete(user);
            System.out.println("User deleted successfully: " + user.getVoterId());

            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/debug/userdetails")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> debugUserDetails() {
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            // Get all users
            List<User> users = userRepository.findAll();
            debugInfo.put("totalUsers", users.size());
            
            // Get all user details
            List<UserDetails> allUserDetails = userDetailsRepository.findAll();
            debugInfo.put("totalUserDetails", allUserDetails.size());
            
            // Check which users have user details
            List<Map<String, Object>> userDetailsStatus = new ArrayList<>();
            for (User user : users) {
                Map<String, Object> status = new HashMap<>();
                status.put("voterId", user.getVoterId());
                status.put("email", user.getEmail());
                status.put("role", user.getRole());
                
                Optional<UserDetails> userDetailsOpt = userDetailsRepository.findByVoterId(user.getVoterId());
                if (userDetailsOpt.isPresent()) {
                    UserDetails userDetails = userDetailsOpt.get();
                    status.put("hasUserDetails", true);
                    status.put("firstName", userDetails.getFirstName());
                    status.put("lastName", userDetails.getLastName());
                    status.put("phoneNumber", userDetails.getPhoneNumber());
                } else {
                    status.put("hasUserDetails", false);
                }
                
                userDetailsStatus.add(status);
            }
            
            debugInfo.put("userDetailsStatus", userDetailsStatus);
            
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
