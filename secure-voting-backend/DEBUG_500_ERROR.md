# Debug: 500 Internal Server Error for Candidates API

## Problem
The API endpoint `GET http://localhost:8081/api/candidates/election/1` is returning a 500 Internal Server Error.

## Error Details
- **URL**: `http://localhost:8081/api/candidates/election/1`
- **Status**: 500 (Internal Server Error)
- **StatusText**: OK (unusual for 500 error)
- **Component**: `voting-chart.component.ts:141`

## Debugging Steps

### Step 1: Check Backend Server Logs
**Look at the Spring Boot console output for error details:**

The backend server console should show the actual error that's causing the 500 response. Look for:
- Stack traces
- SQL errors
- Null pointer exceptions
- Database connection issues

### Step 2: Test API Endpoint Directly
**Test the problematic endpoint:**

```bash
# Test the specific endpoint that's failing
curl -X GET http://localhost:8081/api/candidates/election/1

# Test other candidate endpoints
curl -X GET http://localhost:8081/api/candidates
curl -X GET http://localhost:8081/api/candidates/approved
```

### Step 3: Check Database Connection
**Verify database is accessible:**

```sql
-- Test basic database connection
USE secure_voting;

-- Check if candidates table exists
SHOW TABLES LIKE 'candidates';

-- Check table structure
DESCRIBE candidates;

-- Check if election 1 exists
SELECT * FROM elections WHERE election_id = 1;
```

### Step 4: Check for Missing Dependencies
**Common causes of 500 errors:**

1. **Missing database tables**
2. **Missing columns** (like the new `candidate_image_link` column)
3. **Database connection issues**
4. **Missing imports** (we fixed CandidateStatus import)
5. **Null pointer exceptions**

### Step 5: Run Database Migrations
**Ensure all database changes are applied:**

```sql
-- Run the candidate image migration
USE secure_voting;

-- Check if candidate_image_link column exists
SHOW COLUMNS FROM candidate_details LIKE 'candidate_image_link';

-- If it doesn't exist, add it
ALTER TABLE `candidate_details`
ADD COLUMN `candidate_image_link` varchar(500) DEFAULT NULL AFTER `aadhar_card_link`;
```

## Common Solutions

### Solution 1: Missing Database Column
**If the `candidate_image_link` column is missing:**

```sql
USE secure_voting;
ALTER TABLE `candidate_details`
ADD COLUMN `candidate_image_link` varchar(500) DEFAULT NULL AFTER `aadhar_card_link`;
```

### Solution 2: Database Connection Issues
**Check `application.properties`:**

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/secure_voting
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
```

### Solution 3: Missing Data
**Create test data if tables are empty:**

```sql
USE secure_voting;

-- Ensure election 1 exists
INSERT IGNORE INTO elections (election_id, name, start_date, end_date, status) VALUES
(1, 'Test Election', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 1 DAY)) * 1000, 'SCHEDULED');

-- Ensure party exists
INSERT IGNORE INTO party_details (party_id, party_name, party_symbol, party_secret_code) VALUES
(1, 'Independent Candidate', 'IND', 'INDEPENDENT_SECRET_2024');

-- Create a test candidate
INSERT IGNORE INTO candidates (candidate_id, name, election_id, party_id, status, created_at, updated_at) VALUES
(1, 'Test Candidate', 1, 1, 'APPROVED', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

-- Create candidate details
INSERT IGNORE INTO candidate_details (candidate_id, email, phone_number, gender, age, address, aadhar_card_link) VALUES
(1, 'test@example.com', '1234567890', 'Male', 30, 'Test Address', 'https://drive.google.com/file/d/test');
```

### Solution 4: Restart Backend Server
**Sometimes a restart fixes issues:**

```bash
# Stop the current server (Ctrl+C)
# Then restart
cd secure-voting-backend
mvn spring-boot:run
```

## Quick Diagnostic Script

**Run this complete diagnostic:**

```sql
-- Complete diagnostic script
USE secure_voting;

-- Check if all required tables exist
SHOW TABLES;

-- Check candidates table structure
DESCRIBE candidates;

-- Check candidate_details table structure
DESCRIBE candidate_details;

-- Check if election 1 exists
SELECT * FROM elections WHERE election_id = 1;

-- Check if any candidates exist for election 1
SELECT * FROM candidates WHERE election_id = 1;

-- Check if candidate_details has the new column
SHOW COLUMNS FROM candidate_details LIKE 'candidate_image_link';
```

## Expected Backend Log Output

**When working correctly, you should see:**
```
Started SecureVotingApplication
Tomcat started on port(s): 8081 (http)
```

**When there's an error, you'll see:**
```
ERROR: Could not execute query
ERROR: Table 'secure_voting.candidate_details' doesn't have column 'candidate_image_link'
ERROR: NullPointerException in CandidateService.getCandidatesByElectionId
```

## Test After Fix

**After applying fixes, test these endpoints:**

```bash
# Test basic candidates
curl -X GET http://localhost:8081/api/candidates

# Test election-specific candidates
curl -X GET http://localhost:8081/api/candidates/election/1

# Test approved candidates
curl -X GET http://localhost:8081/api/candidates/approved
```

**Expected successful response:**
```json
[
  {
    "candidateId": 1,
    "name": "Test Candidate",
    "electionId": 1,
    "partyId": 1,
    "status": "APPROVED",
    "createdAt": 1735689600000,
    "updatedAt": 1735689600000
  }
]
```

## Most Likely Fix

Based on the error, the most likely issue is that the `candidate_image_link` column is missing from the `candidate_details` table. Run this:

```sql
USE secure_voting;
ALTER TABLE `candidate_details`
ADD COLUMN `candidate_image_link` varchar(500) DEFAULT NULL AFTER `aadhar_card_link`;
```

Then restart the backend server.
