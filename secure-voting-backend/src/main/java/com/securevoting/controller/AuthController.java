package com.securevoting.controller;

// (Keep all your other imports)

import com.securevoting.dto.AuthRequest;
import com.securevoting.dto.AuthResponse;
import com.securevoting.dto.MessageResponse;
import com.securevoting.dto.RegisterRequest;
import com.securevoting.model.User;
import com.securevoting.model.UserRole;
import com.securevoting.repository.UserRepository;
import com.securevoting.security.jwt.JwtUtils;
import com.securevoting.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        // ... (The signin method remains the same)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        userRepository.findByVoterId(userDetails.getUsername()).ifPresent(user -> {
            user.setLastLogin(System.currentTimeMillis());
            userRepository.save(user);
        });

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new AuthResponse(jwt,
                userDetails.getUsername(), // This is now the voterId
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/test-voterid-login")
    public ResponseEntity<?> testVoterIdLogin(@RequestBody Map<String, String> request) {
        String voterId = request.get("voterId");
        String password = request.get("password");
        
        if (voterId == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "VoterID and password are required"));
        }
        
        try {
            // Test the new VoterID authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(voterId, password));
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "VoterID login successful",
                "voterId", userDetails.getUsername(), // This is now the voterId
                "email", userDetails.getEmail(),
                "approvalStatus", userDetails.getApprovalStatus(),
                "isEnabled", userDetails.isEnabled(),
                "roles", userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList())
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "VoterID login failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/debug-user")
    public ResponseEntity<?> debugUser(@RequestBody Map<String, String> request) {
        try {
            String voterId = request.get("voterId");
            if (voterId == null || voterId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "VoterID is required"));
            }
            
            Optional<User> userOpt = userRepository.findByVoterId(voterId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("voterId", user.getVoterId());
            debugInfo.put("email", user.getEmail());
            debugInfo.put("role", user.getRole());
            debugInfo.put("isActive", user.isActive());
            debugInfo.put("approvalStatus", user.getApprovalStatus());
            debugInfo.put("hashedPassword", user.getPassword() != null ? "SET" : "NULL");
            debugInfo.put("hashedPasswordLength", user.getPassword() != null ? user.getPassword().length() : 0);
            debugInfo.put("createdAt", user.getCreatedAt());
            debugInfo.put("lastLogin", user.getLastLogin());
            
            // Test password encoding
            String testPassword = request.get("testPassword");
            if (testPassword != null) {
                boolean passwordMatches = encoder.matches(testPassword, user.getPassword());
                debugInfo.put("passwordMatches", passwordMatches);
            }
            
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetUserPassword(@RequestBody Map<String, String> request) {
        try {
            String voterId = request.get("voterId");
            String newPassword = request.get("newPassword");
            
            if (voterId == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "VoterID and newPassword are required"));
            }
            
            Optional<User> userOpt = userRepository.findByVoterId(voterId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            user.setPassword(encoder.encode(newPassword));
            user.setActive(true); // Ensure user is active
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of("message", "Password reset successfully for voter: " + voterId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // Generate a unique voter ID
        String voterId = "VOTER_" + System.currentTimeMillis();
        
        if (userRepository.existsByVoterId(voterId)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: VoterID already exists!"));
        }

        // Create and save the user account with voterId as primary key
        User user = new User();
        user.setVoterId(voterId);
        user.setEmail(registerRequest.getEmail());
        user.setPassword(encoder.encode(registerRequest.getPassword()));
        user.setRole(UserRole.USER);
        user.setCreatedAt(System.currentTimeMillis());
        user.setActive(true);
        user.setApprovalStatus(1); // Default to approved for signup
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully with VoterID: " + voterId));
    }
}