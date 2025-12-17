# Admin Login 401 Error Fix

## Problem
Users are getting "401 Unauthorized" error with "User is disabled" message when trying to log in.

## Root Cause
The `UserDetailsImpl.isEnabled()` method checks if `approvalStatus == 1` (approved). Admin users may have:
- `approvalStatus = 2` (pending) 
- `approvalStatus = NULL`
- `approvalStatus = 0` (rejected)

## Solution

### Step 1: Fix Admin User Status in Database
Run the SQL script to ensure admin users are properly enabled:

```sql
-- Execute: fix_admin_user_status.sql
USE secure_voting;

-- Update admin users to be approved and active
UPDATE users 
SET 
    approval_status = 1,  -- APPROVED
    is_active = 1         -- ACTIVE
WHERE 
    voter_id LIKE '%ADMIN%' 
    OR voter_id LIKE '%SYSTEM%' 
    OR role = 'ADMIN';

-- Fix any users with NULL approval_status
UPDATE users 
SET approval_status = 1 
WHERE approval_status IS NULL;
```

### Step 2: Test Admin Login
Use the test command to verify admin login works:

```bash
curl -X POST http://localhost:8081/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ADMIN_VOTER_ID",
    "password": "Admin123!"
  }'
```

### Step 3: Verify User Status
Check the current status of admin users:

```sql
SELECT 
    voter_id, 
    email, 
    approval_status, 
    is_active, 
    role,
    CASE 
        WHEN approval_status = 1 THEN 'APPROVED'
        WHEN approval_status = 2 THEN 'PENDING'
        WHEN approval_status = 0 THEN 'REJECTED'
        ELSE 'UNKNOWN'
    END as status_description
FROM users 
WHERE voter_id LIKE '%ADMIN%' OR voter_id LIKE '%SYSTEM%' OR role = 'ADMIN';
```

## Expected Result
After running the SQL script:
- Admin users will have `approval_status = 1` (approved)
- Admin users will have `is_active = 1` (active)
- Login should work successfully
- No more "User is disabled" errors

## Files Created
- `fix_admin_user_status.sql` - SQL script to fix admin user status
- `test_admin_login.json` - Test payload for admin login
- `test_admin_login_command.txt` - cURL command to test admin login
- `ADMIN_LOGIN_FIX.md` - This documentation

## Notes
- The `UserDetailsImpl.isEnabled()` method correctly checks `approvalStatus == 1`
- Only approved users (approvalStatus = 1) can log in
- Pending users (approvalStatus = 2) cannot log in until approved by admin
- Rejected users (approvalStatus = 0) cannot log in


















