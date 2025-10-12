-- Update users.voter_id to match user_details.voter_id based on email
-- This will fix the voter_id mismatch between the two tables

-- Step 1: Check current state before update
SELECT 'BEFORE UPDATE - Users table:' as status;
SELECT voter_id, email, role FROM users ORDER BY created_at;

-- Step 2: Check user_details table
SELECT 'User_details table:' as status;
SELECT voter_id, first_name, last_name, email FROM user_details ORDER BY voter_id;

-- Step 3: Show what will be updated
SELECT 'RECORDS TO BE UPDATED:' as status;
SELECT 
    u.voter_id as current_users_voter_id,
    u.email,
    ud.voter_id as new_voter_id_from_user_details,
    ud.first_name,
    ud.last_name
FROM users u
JOIN user_details ud ON u.email = ud.email
WHERE u.voter_id != ud.voter_id;

-- Step 4: THE MAIN UPDATE QUERY
UPDATE users u
JOIN user_details ud ON u.email = ud.email
SET u.voter_id = ud.voter_id;

-- Step 5: Verify the update worked
SELECT 'AFTER UPDATE - Users table:' as status;
SELECT voter_id, email, role FROM users ORDER BY created_at;

-- Step 6: Verify the relationship
SELECT 'VERIFICATION - Joined data:' as status;
SELECT 
    u.voter_id,
    u.email,
    u.role,
    ud.first_name,
    ud.last_name,
    ud.phone_number
FROM users u
JOIN user_details ud ON u.voter_id = ud.voter_id
ORDER BY u.created_at;

-- Step 7: Check for any remaining mismatches
SELECT 'REMAINING MISMATCHES:' as status;
SELECT 
    u.voter_id as users_voter_id,
    u.email as users_email,
    ud.voter_id as user_details_voter_id,
    ud.first_name,
    ud.last_name
FROM users u
LEFT JOIN user_details ud ON u.voter_id = ud.voter_id
WHERE ud.voter_id IS NULL;




