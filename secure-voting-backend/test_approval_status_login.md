# Approval Status Login Test

## Overview
This document explains what happens when users with different approval statuses try to login.

## Approval Status Values
- **0** = Rejected
- **1** = Approved  
- **2** = Pending

## Expected Behavior After Fix

### 1. Approved User (approval_status = 1)
- ✅ **Login succeeds**
- ✅ **JWT token generated**
- ✅ **Access granted to system**
- ✅ **Can vote and use all features**

### 2. Pending User (approval_status = 2)
- ❌ **Login fails**
- ❌ **No JWT token generated**
- ❌ **Access denied**
- ❌ **Cannot access system**

### 3. Rejected User (approval_status = 0)
- ❌ **Login fails**
- ❌ **No JWT token generated**
- ❌ **Access denied**
- ❌ **Cannot access system**

## Test Commands

### Test Approved User
```bash
curl -X POST http://localhost:8081/api/auth/test-voterid-login \
  -H "Content-Type: application/json" \
  -d '{
    "voterId": "VOTER_APPROVED_USER",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "VoterID login successful",
  "username": "approved_user",
  "email": "approved_user",
  "approvalStatus": 1,
  "isEnabled": true,
  "roles": ["ROLE_USER"]
}
```

### Test Pending User
```bash
curl -X POST http://localhost:8081/api/auth/test-voterid-login \
  -H "Content-Type: application/json" \
  -d '{
    "voterId": "VOTER_PENDING_USER",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "success": false,
  "error": "VoterID login failed: User account is disabled"
}
```

### Test Rejected User
```bash
curl -X POST http://localhost:8081/api/auth/test-voterid-login \
  -H "Content-Type: application/json" \
  -d '{
    "voterId": "VOTER_REJECTED_USER",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "success": false,
  "error": "VoterID login failed: User account is disabled"
}
```

## Security Benefits

### Before Fix (Security Issue)
- ❌ Pending users could login
- ❌ Rejected users could login
- ❌ Approval process was meaningless
- ❌ Security vulnerability existed

### After Fix (Secure)
- ✅ Only approved users can login
- ✅ Pending users must wait for approval
- ✅ Rejected users cannot access system
- ✅ Proper security controls in place

## Implementation Details

### UserDetailsImpl.isEnabled() Method
```java
@Override
public boolean isEnabled() {
    // Only allow login if user is approved (approvalStatus = 1)
    // 0 = rejected, 1 = approved, 2 = pending
    return approvalStatus != null && approvalStatus == 1;
}
```

### Spring Security Integration
- Spring Security automatically calls `isEnabled()` during authentication
- If `isEnabled()` returns `false`, authentication fails
- User receives "User account is disabled" error message

## Database Schema
The `users` table has the `approval_status` column:
```sql
ALTER TABLE users ADD COLUMN approval_status INT DEFAULT 2;
```

## Workflow
1. **User registers** → `approval_status = 2` (pending)
2. **Admin reviews** → Can approve (1) or reject (0)
3. **User tries to login**:
   - If approved (1) → Login succeeds
   - If pending (2) → Login fails
   - If rejected (0) → Login fails

## Error Messages
- **Pending users**: "User account is disabled"
- **Rejected users**: "User account is disabled"
- **Invalid credentials**: "Bad credentials"
- **User not found**: "User Not Found with VoterID: [voterId]"


















