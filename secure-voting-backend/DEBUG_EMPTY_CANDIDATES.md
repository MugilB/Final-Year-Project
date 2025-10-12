# Debug: Admin Dashboard Shows No Candidates

## Problem
The admin dashboard shows "Failed to load candidates" and "No candidates found for the selected election."

## Possible Causes
1. Backend server is not running
2. No approved candidates in database
3. API endpoints are not working
4. Frontend is using wrong API endpoints
5. Database connection issues

## Step-by-Step Debugging

### Step 1: Check Backend Server Status
**Check if Spring Boot server is running:**

```bash
# Check if port 8081 is in use
netstat -an | findstr :8081

# Or check running Java processes
jps -l | findstr securevoting
```

**Expected Result**: Should see Spring Boot process running on port 8081

**If not running, start it:**
```bash
cd secure-voting-backend
mvn spring-boot:run
```

### Step 2: Test API Endpoints Directly

**Test basic candidates endpoint:**
```bash
curl -X GET http://localhost:8081/api/candidates
```

**Test approved candidates endpoint:**
```bash
curl -X GET http://localhost:8081/api/candidates/approved
```

**Test elections with candidates:**
```bash
curl -X GET http://localhost:8081/api/elections/with-candidates
```

**Test elections with approved candidates:**
```bash
curl -X GET http://localhost:8081/api/elections/with-approved-candidates
```

### Step 3: Check Database for Candidates

**Check all candidates in database:**
```sql
USE secure_voting;

SELECT 
    candidate_id,
    name,
    election_id,
    status,
    created_at
FROM candidates 
ORDER BY candidate_id;
```

**Check only approved candidates:**
```sql
SELECT 
    candidate_id,
    name,
    election_id,
    status,
    created_at
FROM candidates 
WHERE status = 'APPROVED'
ORDER BY candidate_id;
```

**Check elections:**
```sql
SELECT 
    election_id,
    name,
    start_date,
    end_date,
    status
FROM elections 
ORDER BY election_id;
```

### Step 4: Create Test Data (If No Candidates Exist)

**If no candidates exist, create some test data:**

```sql
-- First, ensure we have elections
INSERT IGNORE INTO elections (name, start_date, end_date, status) VALUES
('Kerala_Election', UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 7 DAY)) * 1000, UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 8 DAY)) * 1000, 'SCHEDULED'),
('General Election 2024', UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 14 DAY)) * 1000, UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 15 DAY)) * 1000, 'SCHEDULED');

-- Ensure we have party_details
INSERT IGNORE INTO party_details (party_name, party_symbol, party_secret_code) VALUES
('Independent Candidate', 'IND', 'INDEPENDENT_SECRET_2024'),
('BJP', 'Lotus', 'BJP_SECRET_2024'),
('Congress', 'Hand', 'CONGRESS_SECRET_2024');

-- Create some approved candidates
INSERT INTO candidates (name, election_id, party_id, status, created_at, updated_at) VALUES
('John Doe', 1, 1, 'APPROVED', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
('Jane Smith', 1, 2, 'APPROVED', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
('Bob Johnson', 2, 3, 'APPROVED', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

-- Create candidate details
INSERT INTO candidate_details (candidate_id, email, phone_number, gender, age, address, aadhar_card_link) VALUES
(1, 'john@example.com', '1234567890', 'Male', 30, '123 Main St, Kerala', 'https://drive.google.com/file/d/test1'),
(2, 'jane@example.com', '0987654321', 'Female', 28, '456 Oak Ave, Kerala', 'https://drive.google.com/file/d/test2'),
(3, 'bob@example.com', '1122334455', 'Male', 35, '789 Pine St, City', 'https://drive.google.com/file/d/test3');
```

### Step 5: Check Frontend API Calls

**Open browser developer tools (F12) and check:**
1. **Console tab** - Look for JavaScript errors
2. **Network tab** - Check if API calls are being made and what responses they get

**Look for these API calls:**
- `GET /api/candidates/approved`
- `GET /api/elections/with-approved-candidates`
- `GET /api/candidates/election/{id}/approved`

### Step 6: Update Frontend to Use Correct Endpoints

**If frontend is using old endpoints, update them:**

**Before (showing all candidates):**
```javascript
// Old endpoints
fetch('/api/candidates')
fetch('/api/elections/with-candidates')
```

**After (showing only approved candidates):**
```javascript
// New endpoints
fetch('/api/candidates/approved')
fetch('/api/elections/with-approved-candidates')
```

## Common Solutions

### Solution 1: Backend Server Not Running
```bash
cd secure-voting-backend
mvn spring-boot:run
```

### Solution 2: No Candidates in Database
Run the test data SQL script above to create sample candidates.

### Solution 3: All Candidates Are PENDING
Update existing candidates to APPROVED:
```sql
UPDATE candidates SET status = 'APPROVED' WHERE status = 'PENDING';
```

### Solution 4: Frontend Using Wrong Endpoints
Update frontend code to use the new approved candidate endpoints.

### Solution 5: Database Connection Issues
Check database connection in `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/secure_voting
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## Expected Results After Fix

### API Response Example
```json
GET /api/candidates/approved

[
  {
    "candidateId": 1,
    "name": "John Doe",
    "electionId": 1,
    "partyId": 1,
    "status": "APPROVED",
    "createdAt": 1735689600000,
    "updatedAt": 1735689600000,
    "candidateDetails": {
      "email": "john@example.com",
      "phoneNumber": "1234567890",
      "gender": "Male",
      "age": 30,
      "address": "123 Main St, Kerala"
    }
  }
]
```

### Admin Dashboard Should Show
- ✅ List of approved candidates
- ✅ Bar chart with candidate data
- ✅ No "Failed to load candidates" error
- ✅ No "No candidates found" message

## Quick Fix Script

Run this complete script to set up test data:

```sql
-- Complete test data setup
USE secure_voting;

-- Create elections
INSERT IGNORE INTO elections (name, start_date, end_date, status) VALUES
('Kerala_Election', UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 7 DAY)) * 1000, UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 8 DAY)) * 1000, 'SCHEDULED');

-- Create parties
INSERT IGNORE INTO party_details (party_name, party_symbol, party_secret_code) VALUES
('Independent Candidate', 'IND', 'INDEPENDENT_SECRET_2024'),
('BJP', 'Lotus', 'BJP_SECRET_2024');

-- Create approved candidates
INSERT IGNORE INTO candidates (name, election_id, party_id, status, created_at, updated_at) VALUES
('John Doe', 1, 1, 'APPROVED', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
('Jane Smith', 1, 2, 'APPROVED', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

-- Create candidate details
INSERT IGNORE INTO candidate_details (candidate_id, email, phone_number, gender, age, address, aadhar_card_link) VALUES
(1, 'john@example.com', '1234567890', 'Male', 30, '123 Main St, Kerala', 'https://drive.google.com/file/d/test1'),
(2, 'jane@example.com', '0987654321', 'Female', 28, '456 Oak Ave, Kerala', 'https://drive.google.com/file/d/test2');

-- Verify data
SELECT 
    c.candidate_id,
    c.name,
    c.status,
    e.name as election_name
FROM candidates c
JOIN elections e ON c.election_id = e.election_id
WHERE c.status = 'APPROVED';
```

After running this script, restart the backend server and refresh the admin dashboard.
