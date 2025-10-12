package com.securevoting.config;

import com.securevoting.model.User;
import com.securevoting.model.UserRole;
import com.securevoting.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


@Component
@DependsOn("webSecurityConfig")
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@votingsystem.com").isEmpty()) {
            User admin = new User();
            admin.setVoterId("ADMIN_VOTER_ID");
            admin.setEmail("admin@votingsystem.com");
            admin.setPassword(passwordEncoder.encode("Admin123!"));
            admin.setRole(UserRole.ADMIN);
            admin.setRoles("ADMIN"); // Also set the string field
            admin.setCreatedAt(System.currentTimeMillis());
            admin.setActive(true);
            admin.setApprovalStatus(1); // Admin is always approved
            try {
                userRepository.save(admin);
                System.out.println("Default admin user created.");
            } catch (Exception e) {
                System.out.println("Admin user already exists or email conflict: " + e.getMessage());
            }
        }

        if (userRepository.findByEmail("system@votingsystem.com").isEmpty()) {
            User systemUser = new User();
            systemUser.setVoterId("SYSTEM_VOTER_ID");
            systemUser.setEmail("system@votingsystem.com");
            systemUser.setPassword(passwordEncoder.encode("SystemPassword123!"));
            systemUser.setRole(UserRole.SYSTEM);
            systemUser.setRoles("SYSTEM"); // Also set the string field
            systemUser.setCreatedAt(System.currentTimeMillis());
            systemUser.setActive(true);
            systemUser.setApprovalStatus(1); // System user is always approved
            try {
                userRepository.save(systemUser);
                System.out.println("Placeholder SYSTEM user created.");
            } catch (Exception e) {
                System.out.println("SYSTEM user already exists or email conflict: " + e.getMessage());
            }
        }

        // Fix admin user approval status
        fixAdminUserStatus();
        
        // Fix for voting chart 500 error - ensure required data exists
        fixVotingChartData();
    }

    private void fixAdminUserStatus() {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Starting admin user status fix...");

            // Fix users with empty or invalid role values
            String fixEmptyRoles = "UPDATE users SET role = 'USER' WHERE role IS NULL OR role = ''";
            try (PreparedStatement stmt = connection.prepareStatement(fixEmptyRoles)) {
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Fixed " + rowsAffected + " users with empty/invalid role values");
            }

            // Update admin users to be approved and active
            String updateAdminUsers = "UPDATE users SET approval_status = 1, is_active = 1 WHERE voter_id LIKE '%ADMIN%' OR voter_id LIKE '%SYSTEM%' OR role = 'ADMIN'";
            try (PreparedStatement stmt = connection.prepareStatement(updateAdminUsers)) {
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Updated " + rowsAffected + " admin/system users to approved status");
            }

            // Fix any users with NULL approval_status (set to approved for existing users)
            String fixNullStatus = "UPDATE users SET approval_status = 1 WHERE approval_status IS NULL";
            try (PreparedStatement stmt = connection.prepareStatement(fixNullStatus)) {
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Fixed " + rowsAffected + " users with NULL approval status");
            }

            // Also fix the roles string field to match the enum field
            String fixRolesString = "UPDATE users SET roles = role WHERE roles IS NULL OR roles = '' OR roles != role";
            try (PreparedStatement stmt = connection.prepareStatement(fixRolesString)) {
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Fixed " + rowsAffected + " users with mismatched roles string field");
            }

            // Verify the changes
            String verifyQuery = "SELECT voter_id, email, approval_status, is_active, role, roles FROM users WHERE voter_id LIKE '%ADMIN%' OR voter_id LIKE '%SYSTEM%' OR role = 'ADMIN'";
            try (PreparedStatement stmt = connection.prepareStatement(verifyQuery);
                 ResultSet rs = stmt.executeQuery()) {
                System.out.println("Current admin/system user status:");
                while (rs.next()) {
                    System.out.println("  " + rs.getString("voter_id") + " | " + 
                                     rs.getString("email") + " | " + 
                                     "Approval: " + rs.getInt("approval_status") + " | " +
                                     "Active: " + rs.getInt("is_active") + " | " +
                                     "Role: " + rs.getString("role") + " | " +
                                     "Roles: " + rs.getString("roles"));
                }
            }

            // Also show all users to check for any remaining issues
            String allUsersQuery = "SELECT voter_id, email, approval_status, is_active, role, roles FROM users";
            try (PreparedStatement stmt = connection.prepareStatement(allUsersQuery);
                 ResultSet rs = stmt.executeQuery()) {
                System.out.println("All users status:");
                while (rs.next()) {
                    System.out.println("  " + rs.getString("voter_id") + " | " + 
                                     rs.getString("email") + " | " + 
                                     "Approval: " + rs.getInt("approval_status") + " | " +
                                     "Active: " + rs.getInt("is_active") + " | " +
                                     "Role: " + rs.getString("role") + " | " +
                                     "Roles: " + rs.getString("roles"));
                }
            }

            System.out.println("Admin user status fix completed successfully");

        } catch (SQLException e) {
            System.err.println("Error fixing admin user status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fixVotingChartData() {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Starting voting chart data fix...");

            // Step 1: Check if election 1 exists, if not create it
            String checkElection = "SELECT COUNT(*) FROM elections WHERE election_id = 1";
            try (PreparedStatement stmt = connection.prepareStatement(checkElection);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertElection = "INSERT IGNORE INTO elections (election_id, name, start_date, end_date, status, description, rules) VALUES (1, 'Test Election 2024', UNIX_TIMESTAMP('2024-01-01 00:00:00') * 1000, UNIX_TIMESTAMP('2024-12-31 23:59:59') * 1000, 'SCHEDULED', 'Test election for development', 'Test rules')";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertElection)) {
                        insertStmt.executeUpdate();
                        System.out.println("Created election 1");
                    }
                } else {
                    System.out.println("Election 1 already exists");
                }
            }

            // Step 2: Ensure party_details table has required data
            String insertParties = "INSERT IGNORE INTO party_details (party_id, party_name, party_symbol, party_secret_code) VALUES (1, 'Independent Candidate', 'IND', 'INDEPENDENT_SECRET_2024'), (2, 'Test Party', 'TEST', 'TEST_SECRET_2024')";
            try (PreparedStatement stmt = connection.prepareStatement(insertParties)) {
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Inserted " + rowsAffected + " party records");
            }

            // Step 3: Check if candidate_image_link column exists, if not add it
            String checkColumn = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'secure_voting' AND TABLE_NAME = 'candidate_details' AND COLUMN_NAME = 'candidate_image_link'";
            try (PreparedStatement stmt = connection.prepareStatement(checkColumn);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String addColumn = "ALTER TABLE candidate_details ADD COLUMN candidate_image_link varchar(500) DEFAULT NULL AFTER aadhar_card_link";
                    try (PreparedStatement alterStmt = connection.prepareStatement(addColumn)) {
                        alterStmt.executeUpdate();
                        System.out.println("Added candidate_image_link column");
                    }
                } else {
                    System.out.println("candidate_image_link column already exists");
                }
            }

            // Step 4: Create test candidates for election 1
            String insertCandidates = "INSERT IGNORE INTO candidates (candidate_id, name, election_id, party_id, status, created_at, updated_at) VALUES (1, 'John Doe', 1, 1, 'APPROVED', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000), (2, 'Jane Smith', 1, 2, 'APPROVED', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000), (3, 'Bob Johnson', 1, 1, 'APPROVED', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000)";
            try (PreparedStatement stmt = connection.prepareStatement(insertCandidates)) {
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Inserted " + rowsAffected + " candidate records");
            }

            // Step 5: Create candidate_details for the test candidates
            String insertCandidateDetails = "INSERT IGNORE INTO candidate_details (candidate_id, email, phone_number, gender, age, address, aadhar_card_link, candidate_image_link) VALUES (1, 'john.doe@example.com', '1234567890', 'Male', 35, '123 Main St, City, State', 'https://drive.google.com/file/d/john_aadhar', 'https://drive.google.com/file/d/john_image'), (2, 'jane.smith@example.com', '0987654321', 'Female', 28, '456 Oak Ave, City, State', 'https://drive.google.com/file/d/jane_aadhar', 'https://drive.google.com/file/d/jane_image'), (3, 'bob.johnson@example.com', '1122334455', 'Male', 42, '789 Pine Rd, City, State', 'https://drive.google.com/file/d/bob_aadhar', 'https://drive.google.com/file/d/bob_image')";
            try (PreparedStatement stmt = connection.prepareStatement(insertCandidateDetails)) {
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Inserted " + rowsAffected + " candidate detail records");
            }

            System.out.println("Voting chart data fix completed successfully");

        } catch (SQLException e) {
            System.err.println("Error fixing voting chart data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}