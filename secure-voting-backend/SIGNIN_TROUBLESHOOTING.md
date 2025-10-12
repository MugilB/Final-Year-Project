# Sign-in Troubleshooting Guide

## Quick Diagnosis Steps

### Step 1: Test with Default Admin Account
Try logging in with the default admin account:
- **Username**: `admin`
- **Password**: `Admin123!`

### Step 2: Debug Your User Account
Use the new debug endpoint to check your user account:

**POST** `/api/auth/debug-user`
```json
{
  "username": "your_username",
  "testPassword": "your_password"
}
```

This will return:
- User account status
- Whether password matches
- Account activation status
- Role information

### Step 3: Reset Your Password
If the debug shows issues, reset your password:

**POST** `/api/auth/reset-password`
```json
{
  "username": "your_username",
  "newPassword": "new_password"
}
```

## Common Issues and Solutions

### Issue 1: User Account Not Active
**Symptom**: Login fails even with correct password
**Solution**: 
```sql
UPDATE users SET is_active = 1 WHERE username = 'your_username';
```

### Issue 2: Password Not Properly Encoded
**Symptom**: Password doesn't match even when correct
**Solution**: Use the reset-password endpoint or:
```sql
UPDATE users SET hashed_password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi' WHERE username = 'your_username';
-- This sets password to 'password123'
```

### Issue 3: Database Column Issues
**Symptom**: User exists but authentication fails
**Check**: 
```sql
SELECT username, hashed_password, is_active, role FROM users WHERE username = 'your_username';
```

### Issue 4: Role Issues
**Symptom**: Login succeeds but access denied
**Solution**: Ensure user has proper role:
```sql
UPDATE users SET role = 'USER', roles = 'USER' WHERE username = 'your_username';
```

## Testing Steps

1. **Test Default Admin**:
   - Username: `admin`
   - Password: `Admin123!`

2. **Debug Your Account**:
   ```bash
   curl -X POST http://localhost:8081/api/auth/debug-user \
     -H "Content-Type: application/json" \
     -d '{"username": "your_username", "testPassword": "your_password"}'
   ```

3. **Reset Password if Needed**:
   ```bash
   curl -X POST http://localhost:8081/api/auth/reset-password \
     -H "Content-Type: application/json" \
     -d '{"username": "your_username", "newPassword": "new_password"}'
   ```

4. **Test Login Again**:
   Try logging in with the reset password.

## Expected Debug Response
```json
{
  "username": "your_username",
  "email": "your_email@example.com",
  "role": "USER",
  "isActive": true,
  "hashedPassword": "SET",
  "hashedPasswordLength": 60,
  "createdAt": 1234567890,
  "lastLogin": null,
  "passwordMatches": true
}
```

## If All Else Fails
1. Check server logs for authentication errors
2. Verify database connection
3. Ensure Spring Security configuration is correct
4. Try creating a new user account
