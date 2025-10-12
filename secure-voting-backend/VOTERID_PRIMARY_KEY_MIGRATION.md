# VoterID Primary Key Migration

## Overview
Changed the `users` table primary key from `username` to `voter_id` to make the system more consistent and eliminate the need for separate `user_details` table lookups.

## Changes Made

### 1. Database Migration
- **File**: `database_migrations/006_migrate_to_voterid_primary_key.sql`
- **Changes**:
  - Added `voter_id` column to `users` table
  - Populated `voter_id` from `user_details` table
  - Made `voter_id` the primary key
  - **Dropped `username` column** (no longer needed)

### 2. User Model Updates
- **File**: `src/main/java/com/securevoting/model/User.java`
- **Changes**:
  - Changed `@Id` from `username` to `voterId`
  - Added `@Column(name = "voter_id")` annotation
  - Added `getVoterId()` and `setVoterId()` methods
  - **Removed `username` field completely**

### 3. Repository Updates
- **File**: `src/main/java/com/securevoting/repository/UserRepository.java`
- **Changes**:
  - Added `findByVoterId(String voterId)` method
  - Added `existsByVoterId(String voterId)` method
  - Added `deleteByVoterId(String voterId)` method
  - **Removed all username-related methods**

### 4. Authentication Updates
- **File**: `src/main/java/com/securevoting/security/services/UserDetailsServiceImpl.java`
- **Changes**:
  - Updated `loadUserByUsername()` to use `findByVoterId()` only
  - **Removed username fallback** (no longer needed)
  - Removed dependency on `UserDetailsRepository`

- **File**: `src/main/java/com/securevoting/security/services/UserDetailsImpl.java`
- **Changes**:
  - Updated `build()` method to use `user.getVoterId()` as identifier
  - Updated to use `user.getEmail()` instead of username as email

### 5. Controller Updates
- **File**: `src/main/java/com/securevoting/controller/AuthController.java`
- **Changes**:
  - Updated to use `findByVoterId()` for last login tracking
  - Updated test endpoint to return `voterId` instead of `username`

## Benefits

### 1. Simplified Authentication
- **Before**: Had to lookup `user_details` table to get `voter_id`
- **After**: `voter_id` is directly in `users` table as primary key

### 2. Better Performance
- **Before**: Required JOIN between `users` and `user_details` tables
- **After**: Single table lookup by primary key

### 3. Consistent Data Model
- **Before**: Primary key was `username`, but users login with `voter_id`
- **After**: Primary key is `voter_id`, which matches login behavior

### 4. Simplified Structure
- **Removed**: Username field completely eliminated
- **Enhanced**: VoterID is now the only identifier

## Migration Steps

### 1. Run Database Migration
```sql
-- Execute the migration script
SOURCE database_migrations/006_migrate_to_voterid_primary_key.sql;
```

### 2. Restart Application
- The application will automatically use the new structure
- No code changes needed in other parts of the system

### 3. Verify Changes
```sql
-- Check the new table structure
DESCRIBE users;

-- Verify primary key change
SHOW CREATE TABLE users;
```

## Testing

### Test VoterID Login
```bash
curl -X POST http://localhost:8081/api/auth/test-voterid-login \
  -H "Content-Type: application/json" \
  -d '{
    "voterId": "VOTER_1759900077221",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "VoterID login successful",
  "voterId": "VOTER_1759900077221",
  "email": "user@example.com",
  "approvalStatus": 1,
  "isEnabled": true,
  "roles": ["ROLE_USER"]
}
```

## Impact on Other Systems

### ✅ No Breaking Changes
- **Frontend**: No changes needed - still sends VoterID for login
- **API Endpoints**: All existing endpoints continue to work
- **Authentication**: Improved performance, same functionality
- **User Management**: All existing features preserved

### 🔄 Simplified Authentication
- **All Login**: Now works directly with VoterID (primary key)
- **No Username**: Username field completely removed
- **Existing Data**: All existing users preserved with new structure

## Future Considerations

### 1. UserDetails Table
- **Current**: Still exists and contains personal information
- **Future**: Can be merged into `users` table if needed
- **Recommendation**: Keep separate for now to maintain data organization

### 2. Foreign Key Updates
- **Current**: `blocks` table references `user_details.voter_id`
- **Future**: Can be updated to reference `users.voter_id` directly
- **Impact**: No immediate changes needed

## Summary
This migration successfully changes the primary key from `username` to `voter_id` and completely removes the username field, simplifying the system architecture. The authentication system now works exclusively with VoterID as the primary identifier, making the system cleaner, more intuitive, and more efficient.
