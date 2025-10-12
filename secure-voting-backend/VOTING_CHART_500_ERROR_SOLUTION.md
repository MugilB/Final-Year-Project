# Solution: Fix 500 Error in Voting Chart

## Problem
The voting chart component is getting a **500 Internal Server Error** when trying to fetch candidates for election 1 from the API endpoint `http://localhost:8081/api/candidates/election/1`.

## Root Cause Analysis
The 500 error indicates a server-side problem. Most likely causes:

1. **Missing database data** - No candidates exist for election 1
2. **Missing database columns** - `candidate_image_link` column missing from `candidate_details` table
3. **Database schema issues** - Tables not properly set up
4. **Backend service errors** - Issues in CandidateService

## Solution Steps

### Step 1: Fix Database Schema and Data

**Run the database fix script:**

```sql
-- Run this in MySQL
USE secure_voting;
source fix_voting_chart_500_error.sql;
```

This script will:
- ✅ Create election 1 if it doesn't exist
- ✅ Create required party data
- ✅ Add missing `candidate_image_link` column
- ✅ Create test candidates for election 1
- ✅ Create candidate details
- ✅ Verify all data exists

### Step 2: Test API Endpoints

**Run the API test script:**

```bash
# Make the script executable
chmod +x test_api_endpoints.sh

# Run the test
./test_api_endpoints.sh
```

This will test all the API endpoints and show you exactly which ones are failing.

### Step 3: Check Backend Server Logs

**Look at the Spring Boot console output for error details:**

The backend server console should show the actual error that's causing the 500 response. Look for:
- Stack traces
- SQL errors
- Null pointer exceptions
- Database connection issues

### Step 4: Restart Backend Server

**After fixing the database, restart the backend:**

```bash
# Stop the current server (Ctrl+C)
# Then restart
cd secure-voting-backend
mvn spring-boot:run
```

### Step 5: Test Frontend

**After backend is running, test the frontend:**

1. Open the admin dashboard
2. Go to the voting chart section
3. Select election 1 from the dropdown
4. The chart should now load without 500 errors

## Expected Results

After applying the fix:

### ✅ API Endpoints Should Return 200:
```bash
GET /api/candidates/election/1
# Should return: [{"candidateId":1,"name":"John Doe",...}]

GET /api/candidates/election/1/vote-counts  
# Should return: {"John Doe":0,"Jane Smith":0,"Bob Johnson":0}

GET /api/candidates/election/1/approved
# Should return: [{"candidateId":1,"name":"John Doe",...}]
```

### ✅ Frontend Should Work:
- Voting chart loads without errors
- Candidates display in the chart
- Vote counts show (initially 0)
- No more 500 errors in console

## Debugging Commands

### Check Database Data:
```sql
USE secure_voting;

-- Check if election 1 exists
SELECT * FROM elections WHERE election_id = 1;

-- Check candidates for election 1
SELECT * FROM candidates WHERE election_id = 1;

-- Check candidate details
SELECT * FROM candidate_details WHERE candidate_id IN (1,2,3);

-- Check table structure
DESCRIBE candidate_details;
```

### Test API Manually:
```bash
# Test candidates endpoint
curl -X GET http://localhost:8081/api/candidates/election/1

# Test vote counts endpoint  
curl -X GET http://localhost:8081/api/candidates/election/1/vote-counts

# Test approved candidates endpoint
curl -X GET http://localhost:8081/api/candidates/election/1/approved
```

## Common Issues and Fixes

### Issue 1: "Table doesn't have column 'candidate_image_link'"
**Fix:**
```sql
ALTER TABLE candidate_details 
ADD COLUMN candidate_image_link varchar(500) DEFAULT NULL AFTER aadhar_card_link;
```

### Issue 2: "No candidates found for election 1"
**Fix:**
```sql
-- Create test candidates
INSERT INTO candidates (candidate_id, name, election_id, party_id, status, created_at, updated_at) VALUES
(1, 'John Doe', 1, 1, 'APPROVED', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

INSERT INTO candidate_details (candidate_id, email, phone_number, gender, age, address, aadhar_card_link) VALUES
(1, 'john@example.com', '1234567890', 'Male', 35, '123 Main St', 'https://drive.google.com/test');
```

### Issue 3: "Election 1 doesn't exist"
**Fix:**
```sql
INSERT INTO elections (election_id, name, start_date, end_date, status) VALUES
(1, 'Test Election', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 1 DAY)) * 1000, 'SCHEDULED');
```

## Verification

After applying the fix, you should see:

1. **Backend Console:** No error messages when accessing `/api/candidates/election/1`
2. **Frontend Console:** No 500 errors, successful API calls
3. **Voting Chart:** Displays candidates and vote counts
4. **API Test:** All endpoints return 200 status codes

## If Still Having Issues

1. **Check backend logs** for the exact error message
2. **Run the diagnostic script:** `source diagnose_500_error.sql`
3. **Verify database connection** in `application.properties`
4. **Check if all required tables exist** and have the correct structure

The most common fix is running the `fix_voting_chart_500_error.sql` script to ensure all required data exists in the database.
