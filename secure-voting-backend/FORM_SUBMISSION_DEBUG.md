# Form Submission Debug Guide

## Issue: Form data not being stored in database

### Root Cause Found
The frontend was using **simulated API calls** instead of real HTTP requests to the backend. This has been fixed.

## Changes Made

### 1. Frontend Fixes
**Files Updated**:
- `candidate-nomination.component.ts`
- `voter-registration.component.ts`

**Changes**:
- ✅ Added `HttpClient` and `HttpClientModule` imports
- ✅ Replaced `setTimeout` simulation with real HTTP POST calls
- ✅ Added proper error handling for API responses
- ✅ Added proper success/error message handling

### 2. API Endpoints
**Candidate Nomination**: `POST http://localhost:8081/api/candidate-nominations`
**Voter Registration**: `POST http://localhost:8081/api/voter-registration` (needs to be created)

## Debugging Steps

### Step 1: Check Backend is Running
```bash
# Make sure your Spring Boot backend is running on port 8081
curl http://localhost:8081/api/candidate-nominations
```

### Step 2: Check Database Migration
Run the migration script to ensure tables are properly set up:
```sql
-- Run this in your MySQL database
source database_migrations/001_update_candidate_tables.sql
```

### Step 3: Check Backend Logs
Look for errors in your Spring Boot console when submitting the form:
- Database connection errors
- Validation errors
- Missing table errors

### Step 4: Test API Endpoint Directly
```bash
# Test candidate nomination endpoint
curl -X POST http://localhost:8081/api/candidate-nominations \
  -H "Content-Type: application/json" \
  -d '{
    "candidateName": "Test Candidate",
    "gender": "Male",
    "age": 25,
    "email": "test@example.com",
    "phoneNumber": "1234567890",
    "address": "Test Address",
    "party": "Independent Candidate",
    "partySecretCode": "INDEPENDENT_SECRET_2024",
    "aadharCardLink": "https://drive.google.com/file/d/test",
    "electionId": 1
  }'
```

### Step 5: Check Database Tables
Verify tables exist and have correct structure:
```sql
-- Check if tables exist
SHOW TABLES LIKE 'candidates';
SHOW TABLES LIKE 'candidate_details';
SHOW TABLES LIKE 'party_details';

-- Check table structure
DESCRIBE candidates;
DESCRIBE candidate_details;
DESCRIBE party_details;

-- Check if Independent party exists
SELECT * FROM party_details WHERE party_name = 'Independent Candidate';
```

## Common Issues and Solutions

### Issue 1: CORS Error
**Error**: `Access to XMLHttpRequest at 'http://localhost:8081' from origin 'http://localhost:4200' has been blocked by CORS policy`

**Solution**: Backend already has `@CrossOrigin(origins = "*")` annotation

### Issue 2: Database Connection Error
**Error**: `Could not connect to database`

**Solution**: 
- Check database is running
- Verify connection properties in `application.properties`
- Check database credentials

### Issue 3: Table Not Found
**Error**: `Table 'secure_voting.candidates' doesn't exist`

**Solution**: Run the migration script:
```sql
source database_migrations/001_update_candidate_tables.sql
```

### Issue 4: Party Not Found
**Error**: `Party not found: Independent Candidate`

**Solution**: Ensure Independent party exists:
```sql
INSERT IGNORE INTO party_details (party_name, party_symbol, party_secret_code) 
VALUES ('Independent Candidate', 'IND', 'INDEPENDENT_SECRET_2024');
```

### Issue 5: Validation Errors
**Error**: `Invalid party secret code`

**Solution**: Use correct secret codes:
- Independent Candidate: `INDEPENDENT_SECRET_2024`
- Other parties: Use their specific secret codes

## Testing Checklist

### Frontend Testing
- [ ] Form validation works
- [ ] HTTP requests are sent to correct endpoints
- [ ] Error messages display properly
- [ ] Success messages display properly
- [ ] Form resets after successful submission

### Backend Testing
- [ ] API endpoints respond correctly
- [ ] Database connection works
- [ ] Data is inserted into correct tables
- [ ] Validation works properly
- [ ] Error handling works

### Database Testing
- [ ] Tables exist with correct structure
- [ ] Independent party exists
- [ ] Data is actually stored
- [ ] Foreign key relationships work

## Next Steps

1. **Run the migration script** to ensure database is set up correctly
2. **Start the backend** and verify it's running on port 8081
3. **Test the form submission** and check browser console for errors
4. **Check backend logs** for any error messages
5. **Verify data in database** after successful submission

## API Endpoints Status

### ✅ Working
- `POST /api/candidate-nominations` - Candidate nomination submission

### ❌ Needs Creation
- `POST /api/voter-registration` - Voter registration submission

The candidate nomination should now work properly with real database storage!
