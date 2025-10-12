package com.securevoting.controller;

import com.securevoting.model.User;
import com.securevoting.model.UserDetails;
import com.securevoting.model.UserRole;
import com.securevoting.payload.request.VoterRegistrationRequest;
import com.securevoting.service.UserService;
import com.securevoting.service.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@RestController
@RequestMapping("/api/voters")
@CrossOrigin(origins = "*")
public class VoterRegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/register")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> registerVoter(@Valid @RequestBody VoterRegistrationRequest request, BindingResult bindingResult) {
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                for (FieldError error : bindingResult.getFieldErrors()) {
                    errors.put(error.getField(), error.getDefaultMessage());
                }
                return ResponseEntity.status(400).body(createValidationErrorResponse("Validation failed", errors));
            }

            // Check if email already exists
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.status(409).body(createErrorResponse("Email already exists"));
            }

            // Generate voter ID first
            String voterId = generateVoterId();
            
            // Create user account WITHOUT password (password will be generated on approval)
            User user = new User();
            user.setVoterId(voterId);
            user.setEmail(request.getEmail());
            user.setPassword(null); // No password until approved
            user.setRole(UserRole.USER);
            user.setActive(false); // Not active until approved
            user.setApprovalStatus(2); // Waiting for approval
            user.setCreatedAt(System.currentTimeMillis());

            userService.save(user);

            // Create user details
            UserDetails userDetails = new UserDetails();
            userDetails.setVoterId(voterId);
            userDetails.setUserVoterId(voterId); // Reference to users table
            userDetails.setFirstName(request.getFirstName());
            userDetails.setLastName(request.getLastName());
            userDetails.setAddress(request.getAddress());
            userDetails.setPhoneNumber(request.getPhoneNumber());
            userDetails.setGender(request.getGender());
            userDetails.setBloodGroup(request.getBloodGroup());
            userDetails.setWardId(request.getWardId());
            userDetails.setDob(request.getDob());
            userDetails.setEmail(request.getEmail());
            userDetails.setAadharCardLink(request.getAadharCardLink());
            userDetails.setProfilePictureLink(request.getProfilePictureLink());

            UserDetails savedUserDetails = userDetailsService.save(userDetails);

            // Create ticket in external system
            try {
                System.out.println("Attempting to create ticket for voter: " + savedUserDetails.getVoterId());
                createVoterRegistrationTicket(request, savedUserDetails.getVoterId());
                System.out.println("Ticket creation completed successfully for voter: " + savedUserDetails.getVoterId());
            } catch (Exception e) {
                System.err.println("Failed to create ticket for voter " + savedUserDetails.getVoterId() + ": " + e.getMessage());
                e.printStackTrace();
                // Don't fail the registration if ticket creation fails
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Voter registration submitted successfully! Your application is pending approval. You will receive login credentials once approved by admin.");
            response.put("voterId", savedUserDetails.getVoterId());
            response.put("status", "PENDING_APPROVAL");
            response.put("isActive", false);
            response.put("approvalStatus", 2);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse("Internal server error occurred"));
        }
    }

    private String generateVoterId() {
        // Generate a unique voter ID (you can implement your own logic)
        return "VOTER_" + System.currentTimeMillis();
    }

    /**
     * Helper method to safely handle null values for external API
     * @param value The value to check
     * @return The value if not null, empty string if null
     */
    private String safeString(Object value) {
        return value != null ? value.toString() : "";
    }

    /**
     * Helper method to safely handle Integer values for external API
     * @param value The value to check
     * @return The value if not null, 0 if null
     */
    private Integer safeInteger(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private void createVoterRegistrationTicket(VoterRegistrationRequest request, String voterId) {
        try {
            System.out.println("Starting ticket creation for voter: " + voterId);
            String url = "https://dev007test.desk365.io/apis/v3/tickets/create";
            System.out.println("API URL: " + url);
            
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("accept", "application/json");
            headers.set("Authorization", "575a2602b282a056af8f7c4af4be8cdf9d6a4c91a759f4209ff3b187d5659703");
            headers.set("Content-Type", "application/json");
            System.out.println("Headers prepared");

            // Prepare custom fields - all values from form data except cf_Action (handle null values and data types)
            Map<String, Object> customFields = new HashMap<>();
            customFields.put("cf_First Name", safeString(request.getFirstName()));
            customFields.put("cf_Last Name", safeString(request.getLastName()));
            customFields.put("cf_Email", safeString(request.getEmail()));
            customFields.put("cf_Phone Number", safeString(request.getPhoneNumber()));
            customFields.put("cf_Date Of Birth", request.getDob() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(request.getDob())) : "");
            customFields.put("cf_Gender", safeString(request.getGender()));
            customFields.put("cf_Address", safeString(request.getAddress()));
            customFields.put("cf_Ward", safeString(request.getWardId())); // Ward is stored as String
            customFields.put("cf_Blood Group", safeString(request.getBloodGroup()));
            customFields.put("cf_Proof", safeString(request.getAadharCardLink()));
            customFields.put("cf_Profile Picture", safeString(request.getProfilePictureLink()));
            // Note: Username field removed as it's not accepted by the external API
            // customFields.put("cf_Username", request.getUsername());
            // Static value - always "New Voter Approval" for new registrations
            customFields.put("cf_Action", "New Voter Approval");

            // Prepare ticket data
            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("email", request.getEmail());
            ticketData.put("subject", "New Voter Registration Request - " + request.getFirstName() + " " + request.getLastName());
            ticketData.put("description", "New Voter Registration Request - Please review and approve the voter registration application for Voter ID: " + voterId);
            ticketData.put("status", "open");
            ticketData.put("priority", 1);
            ticketData.put("type", "Request");
            ticketData.put("group", "Voter Registration");
            ticketData.put("category", "New Registration");
            ticketData.put("sub_category", "Voter Application");
            ticketData.put("custom_fields", customFields);
            ticketData.put("watchers", new java.util.ArrayList<>());
            ticketData.put("share_to", new java.util.ArrayList<>());

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(ticketData, headers);
            System.out.println("HTTP entity created with ticket data");

            // Make the API call
            System.out.println("Making API call to external service...");
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            System.out.println("API call completed. Response status: " + response.getStatusCode());
            System.out.println("Ticket created successfully: " + response.getBody());
            
        } catch (Exception e) {
            System.err.println("Error creating ticket: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to be caught by the calling method
        }
    }

    public void createVoterApprovalTicket(String username, String voterId, String generatedPassword) {
        try {
            System.out.println("Starting voter approval ticket creation for: " + username);
            String url = "https://dev007test.desk365.io/apis/v3/tickets/create";
            System.out.println("API URL: " + url);
            
            // Get voter details from database
            Optional<UserDetails> userDetailsOpt = userDetailsService.getUserDetailsByVoterId(username);
            if (userDetailsOpt.isEmpty()) {
                System.err.println("User details not found: " + username);
                return;
            }
            
            UserDetails userDetails = userDetailsOpt.get();
            
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("accept", "application/json");
            headers.set("Authorization", "575a2602b282a056af8f7c4af4be8cdf9d6a4c91a759f4209ff3b187d5659703");
            headers.set("Content-Type", "application/json");
            System.out.println("Headers prepared");

            // Prepare custom fields - all values from voter data (handle null values and data types)
            Map<String, Object> customFields = new HashMap<>();
            customFields.put("cf_First Name", safeString(userDetails.getFirstName()));
            customFields.put("cf_Last Name", safeString(userDetails.getLastName()));
            customFields.put("cf_Email", safeString(userDetails.getEmail()));
            customFields.put("cf_Phone Number", safeString(userDetails.getPhoneNumber()));
            customFields.put("cf_Date Of Birth", userDetails.getDob() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(userDetails.getDob())) : "");
            customFields.put("cf_Gender", safeString(userDetails.getGender()));
            customFields.put("cf_Address", safeString(userDetails.getAddress()));
            customFields.put("cf_Ward", safeString(userDetails.getWardId())); // Ward is stored as String
            customFields.put("cf_Blood Group", safeString(userDetails.getBloodGroup()));
            customFields.put("cf_Proof", safeString(userDetails.getAadharCardLink()));
            customFields.put("cf_Profile Picture", safeString(userDetails.getProfilePictureLink()));
            // Static value - "Voter Approved" for approval notifications
            customFields.put("cf_Action", "Voter Approved");
            // Add the generated password and voter ID for the user (only if password is provided)
            if (generatedPassword != null && !generatedPassword.trim().isEmpty()) {
                customFields.put("cf_Password", generatedPassword);
            }
            customFields.put("cf_Voter ID", voterId);

            // Prepare ticket data
            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("email", userDetails.getEmail());
            ticketData.put("subject", "Voter Approved - " + userDetails.getFirstName() + " " + userDetails.getLastName());
            // Build description with or without password
            String description = "Voter Registration Approved - The voter registration application for Voter ID: " + voterId + " has been approved by admin.";
            if (generatedPassword != null && !generatedPassword.trim().isEmpty()) {
                description += " Login credentials: Voter ID: " + voterId + ", Password: " + generatedPassword;
            } else {
                description += " User already has login credentials set.";
            }
            ticketData.put("description", description);
            ticketData.put("status", "open");
            ticketData.put("priority", 1);
            ticketData.put("type", "Request");
            ticketData.put("group", "Voter Registration");
            ticketData.put("category", "New Registration");
            ticketData.put("sub_category", "Voter Application");
            ticketData.put("custom_fields", customFields);
            ticketData.put("watchers", new java.util.ArrayList<>());
            ticketData.put("share_to", new java.util.ArrayList<>());

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(ticketData, headers);
            System.out.println("HTTP entity created with approval ticket data");

            // Make the API call
            System.out.println("Making API call to external service for voter approval...");
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            System.out.println("API call completed. Response status: " + response.getStatusCode());
            System.out.println("Voter approval ticket created successfully: " + response.getBody());
            
        } catch (Exception e) {
            System.err.println("Error creating voter approval ticket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return errorResponse;
    }

    private Map<String, Object> createValidationErrorResponse(String message, Map<String, String> errors) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("validationErrors", errors);
        return errorResponse;
    }

    @PostMapping("/test-curl")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> testCurlRequest() {
        try {
            System.out.println("Testing cURL request...");
            
            // Create a dummy VoterRegistrationRequest for testing
            VoterRegistrationRequest testRequest = new VoterRegistrationRequest();
            testRequest.setEmail("test@example.com");
            testRequest.setFirstName("John");
            testRequest.setLastName("Doe");
            testRequest.setAddress("123 Test Street");
            testRequest.setPhoneNumber("+1-555-123-4567");
            testRequest.setGender("Male");
            testRequest.setBloodGroup("O+");
            testRequest.setWardId(1);
            testRequest.setDob(System.currentTimeMillis() - (25 * 365 * 24 * 60 * 60 * 1000L)); // 25 years ago
            testRequest.setAadharCardLink("https://drive.google.com/file/d/test-aadhar-link");
            testRequest.setProfilePictureLink("https://drive.google.com/file/d/test-profile-link");

            // Test the cURL request
            createVoterRegistrationTicket(testRequest, "VOTER_TEST_123");
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Test cURL request sent successfully!");
            response.put("status", "SUCCESS");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Test cURL request failed: " + e.getMessage());
            errorResponse.put("status", "FAILED");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

