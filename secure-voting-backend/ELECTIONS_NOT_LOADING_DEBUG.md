# Elections Not Loading - Debug Guide

## Problem
The election dropdown in both candidate nomination and voter registration forms is empty, showing only "Choose an election..." placeholder.

## Debugging Steps

### 1. Check Backend Server Status
First, ensure the Spring Boot backend server is running:

```bash
cd secure-voting-backend
mvn spring-boot:run
```

Look for these messages in the console:
- "Started SecureVotingApplication"
- "Tomcat started on port(s): 8081"

### 2. Test API Endpoint Directly
Test the elections API endpoint:

```bash
curl -X GET http://localhost:8081/api/elections/for-nominations
```

**Expected Response:**
```json
[
  {
    "electionId": 1,
    "name": "General Election 2024",
    "startDate": 1704067200000,
    "endDate": 1704153600000,
    "status": "SCHEDULED"
  }
]
```

**If you get connection refused:**
- Backend server is not running
- Start the server with `mvn spring-boot:run`

**If you get 404 error:**
- API endpoint not found
- Check if the server restarted after our changes

### 3. Check Database for Elections
Run the test script to check and create elections:

```bash
mysql -u your_username -p your_database_name < test_elections.sql
```

Or manually check:
```sql
-- Check existing elections
SELECT 
    election_id,
    name,
    FROM_UNIXTIME(start_date/1000) as start_date_readable,
    status,
    start_date,
    end_date
FROM elections 
ORDER BY start_date;
```

### 4. Check Browser Console
Open browser developer tools (F12) and check the Console tab for errors:

**Look for:**
- "Loading elections from API..." (should appear)
- "Elections loaded successfully: [...]" (should show data)
- Any error messages in red

**Common errors:**
- `Failed to load resource: the server responded with a status of 404`
- `Failed to load resource: the server responded with a status of 500`
- `CORS policy` errors

### 5. Check Backend Logs
Look at the Spring Boot console output for:

```
Getting elections for nominations. Current time: [timestamp]
Found X total elections in database
Election: [name] - Start: [timestamp] - Status: [status]
Found X elections open for nominations
```

## Common Issues and Solutions

### Issue 1: No Elections in Database
**Problem:** Database has no elections or all elections have past dates.

**Solution:** Create test elections:
```sql
INSERT INTO elections (name, start_date, end_date, status) 
VALUES (
    'Test Election 2024', 
    UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 7 DAY)) * 1000,
    UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 8 DAY)) * 1000,
    'SCHEDULED'
);
```

### Issue 2: Backend Server Not Running
**Problem:** API calls fail with connection refused.

**Solution:** Start the backend server:
```bash
cd secure-voting-backend
mvn spring-boot:run
```

### Issue 3: API Endpoint Not Found (404)
**Problem:** Server is running but endpoint doesn't exist.

**Solution:** 
1. Restart the backend server (our changes need a restart)
2. Check that `ElectionController.java` has the new endpoint
3. Verify security configuration allows the endpoint

### Issue 4: CORS Errors
**Problem:** Browser blocks API calls due to CORS policy.

**Solution:** Check that `@CrossOrigin(origins = "*")` is present in controllers.

### Issue 5: All Elections Have Past Dates
**Problem:** Elections exist but all have start dates in the past.

**Solution:** Update election dates or create new elections with future dates.

## Quick Fix Script

Run this SQL script to create test elections:

```sql
-- Delete old test elections
DELETE FROM elections WHERE name LIKE '%Test%';

-- Create new test elections with future dates
INSERT INTO elections (name, start_date, end_date, status) VALUES
('General Election 2024', UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 7 DAY)) * 1000, UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 8 DAY)) * 1000, 'SCHEDULED'),
('Local Council Election 2024', UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 14 DAY)) * 1000, UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 15 DAY)) * 1000, 'SCHEDULED'),
('Student Union Election 2024', UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 30 DAY)) * 1000, UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 31 DAY)) * 1000, 'SCHEDULED');

-- Verify elections were created
SELECT 
    election_id,
    name,
    FROM_UNIXTIME(start_date/1000) as start_date_readable,
    status
FROM elections 
ORDER BY start_date;
```

## Testing Steps

1. **Start Backend Server**
   ```bash
   cd secure-voting-backend
   mvn spring-boot:run
   ```

2. **Create Test Elections** (run the SQL script above)

3. **Test API Endpoint**
   ```bash
   curl -X GET http://localhost:8081/api/elections/for-nominations
   ```

4. **Open Frontend Form**
   - Go to candidate nomination or voter registration
   - Open browser console (F12)
   - Check for "Loading elections from API..." message
   - Check for "Elections loaded successfully:" message

5. **Verify Dropdown**
   - Elections should appear in the dropdown
   - Format: "Election Name (Starts: Date Time)"

## Expected Results

After following these steps:
- ✅ Backend server running on port 8081
- ✅ API endpoint returns election data
- ✅ Browser console shows successful API calls
- ✅ Dropdown populated with elections
- ✅ Elections show future start dates

If you're still having issues, check the browser console and backend logs for specific error messages.
