# Authentication Debug Guide

## Common Sign-in Issues and Solutions

### Issue 1: Database Column Mismatch
The User model has two password columns:
- `hashed_password` (line 14-15)
- `password` (line 35-36)

**Problem**: Authentication might be reading from the wrong column.

### Issue 2: User Account Status
Users might have `is_active = false` in the database.

### Issue 3: Password Encoding
Passwords might not be properly encoded or there's a mismatch between stored and verified passwords.

## Debugging Steps

### Step 1: Check User Data in Database
```sql
SELECT username, hashed_password, password, is_active, role FROM users WHERE username = 'your_username';
```

### Step 2: Verify Password Encoding
Check if the stored password is properly BCrypt encoded (should start with `$2a$` or `$2b$`).

### Step 3: Check User Status
Ensure `is_active = 1` (true) for the user.

### Step 4: Test with Default Admin
Try logging in with:
- Username: `admin`
- Password: `Admin123!`

## Quick Fixes

### Fix 1: Reset User Password
```sql
UPDATE users SET hashed_password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi' WHERE username = 'your_username';
-- This sets password to 'password123'
```

### Fix 2: Activate User Account
```sql
UPDATE users SET is_active = 1 WHERE username = 'your_username';
```

### Fix 3: Check for Duplicate Password Columns
```sql
-- Check which column has the actual password
SELECT username, 
       CASE WHEN hashed_password IS NOT NULL AND hashed_password != '' THEN 'hashed_password' ELSE 'password' END as password_column
FROM users;
```
