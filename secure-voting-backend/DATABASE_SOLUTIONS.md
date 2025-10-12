# Database-Level Solutions for Foreign Key Constraint Issue

## Current Issue
The foreign key constraint `blocks_ibfk_1` prevents username updates because it references `user_details.voter_id` which changes when username changes.

## Solution 1: Modify Foreign Key Constraint (Recommended)
Instead of dropping the constraint, modify it to use CASCADE updates:

```sql
-- Drop the existing foreign key constraint
ALTER TABLE blocks DROP FOREIGN KEY blocks_ibfk_1;

-- Add new constraint with CASCADE updates
ALTER TABLE blocks ADD CONSTRAINT blocks_ibfk_1 
    FOREIGN KEY (voter_id) REFERENCES user_details (voter_id) 
    ON DELETE RESTRICT ON UPDATE CASCADE;
```

**Benefits:**
- Maintains data integrity
- Automatically updates blocks when voter_id changes
- No need to disable foreign key checks in code

## Solution 2: Remove Foreign Key Constraint (Not Recommended)
```sql
ALTER TABLE blocks DROP FOREIGN KEY blocks_ibfk_1;
```

**Drawbacks:**
- Loses referential integrity
- Could lead to orphaned records
- Not recommended for production

## Solution 3: Change Primary Key Strategy
Instead of using voter_id as primary key, use an auto-incrementing ID:

```sql
-- Add new primary key column
ALTER TABLE user_details ADD COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY FIRST;

-- Update blocks table to reference the new ID
ALTER TABLE blocks ADD COLUMN user_details_id BIGINT;
UPDATE blocks b JOIN user_details u ON b.voter_id = u.voter_id 
SET b.user_details_id = u.id;

-- Drop old foreign key and add new one
ALTER TABLE blocks DROP FOREIGN KEY blocks_ibfk_1;
ALTER TABLE blocks DROP COLUMN voter_id;
ALTER TABLE blocks ADD CONSTRAINT blocks_ibfk_1 
    FOREIGN KEY (user_details_id) REFERENCES user_details (id) 
    ON DELETE RESTRICT ON UPDATE CASCADE;
```

## Recommendation
**Use Solution 1** - it's the simplest and maintains data integrity while allowing voter_id updates to cascade automatically to the blocks table.

## Implementation
If you choose Solution 1, you can simplify the Java code by removing the foreign key check disabling:

```java
// Simple approach with CASCADE constraint
blockRepository.updateVoterIdInBlocks(oldVoterId, newVoterId);
userDetails.setVoterId(newVoterId);
userDetails.setUsername(request.getUsername());
userDetailsRepository.save(userDetails);
```

The CASCADE constraint will automatically update the blocks table when voter_id changes.

