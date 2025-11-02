# Migration Guide: VoterID Primary Key

## 🚨 Foreign Key Constraint Error Fix

You encountered this error:
```
Error Code: 1553
Cannot drop index 'PRIMARY': needed in a foreign key constraint
```

This happens because other tables have foreign key constraints that reference the `users` table's primary key.

## 📋 Step-by-Step Solution

### Step 1: Check Current Constraints
First, run this query to see what foreign keys exist:

```sql
-- Run this in your database
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME = 'users';
```

### Step 2: Choose Your Migration Approach

#### Option A: Use the Safe Migration Script (Recommended)
```bash
# Run the safe migration script
mysql -u your_username -p your_database < database_migrations/006_migrate_to_voterid_primary_key_safe.sql
```

#### Option B: Manual Step-by-Step Migration
```bash
# Run each step manually
mysql -u your_username -p your_database < database_migrations/006_step_by_step_migration.sql
```

#### Option C: Use the Original Script (Now Fixed)
```bash
# The original script now includes foreign key handling
mysql -u your_username -p your_database < database_migrations/006_migrate_to_voterid_primary_key.sql
```

### Step 3: What the Migration Does

1. **Adds `voter_id` column** to `users` table
2. **Populates `voter_id`** from existing `user_details` table
3. **Makes `voter_id` NOT NULL**
4. **Temporarily disables foreign key checks** (`SET FOREIGN_KEY_CHECKS = 0`)
5. **Drops old primary key** (username)
6. **Adds new primary key** (voter_id)
7. **Re-enables foreign key checks** (`SET FOREIGN_KEY_CHECKS = 1`)
8. **Drops username column**
9. **Verifies the new structure**

### Step 4: Verify Migration Success

After running the migration, verify:

```sql
-- Check the new table structure
DESCRIBE `users`;

-- Check that foreign keys are still intact
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME = 'users';

-- Test that data is preserved
SELECT voter_id, email, role, is_active, approval_status 
FROM users 
LIMIT 5;
```

## 🔧 Why This Works

The key fix is using `SET FOREIGN_KEY_CHECKS = 0` before dropping the primary key. This temporarily disables foreign key constraint checking, allowing us to:

1. Drop the old primary key
2. Add the new primary key
3. Re-enable foreign key checks

The foreign key constraints remain intact - we just temporarily bypassed the check during the primary key change.

## ⚠️ Important Notes

- **Backup your database** before running any migration
- **Test on a development environment** first
- **The migration is reversible** if you keep a backup
- **All existing data is preserved**
- **Foreign key constraints remain intact**

## 🧪 Testing After Migration

```bash
# Test the new VoterID login
curl -X POST http://localhost:8081/api/auth/test-voterid-login \
  -H "Content-Type: application/json" \
  -d '{"voterId": "VOTER_1759900077221", "password": "password123"}'

# Test debug endpoint
curl -X POST http://localhost:8081/api/auth/debug-user \
  -H "Content-Type: application/json" \
  -d '{"voterId": "VOTER_1759900077221", "testPassword": "password123"}'
```

## 🎯 Expected Result

After successful migration:
- ✅ `users` table has `voter_id` as primary key
- ✅ `username` column is removed
- ✅ All foreign key constraints are preserved
- ✅ All existing data is intact
- ✅ Authentication works with VoterID only
- ✅ System is simpler and more consistent








