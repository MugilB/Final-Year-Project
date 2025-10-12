# Timestamp to Bigint Migration

## Overview
Changed all timestamp columns in the candidate-related tables from `timestamp` to `bigint` to store Unix timestamps in milliseconds (e.g., 1735689600000).

## Changes Made

### 1. Database Schema Changes

#### Candidates Table
- **Before**: `created_at timestamp DEFAULT CURRENT_TIMESTAMP`
- **After**: `created_at bigint DEFAULT NULL`
- **Before**: `updated_at timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
- **After**: `updated_at bigint DEFAULT NULL`

#### Candidate Details Table
- **Before**: `reviewed_at timestamp NULL`
- **After**: `reviewed_at bigint DEFAULT NULL`

### 2. Java Model Changes

#### Candidate.java
```java
// Before
@Column(name = "created_at")
private java.sql.Timestamp createdAt;

@Column(name = "updated_at")
private java.sql.Timestamp updatedAt;

// After
@Column(name = "created_at")
private Long createdAt;

@Column(name = "updated_at")
private Long updatedAt;
```

#### CandidateDetails.java
```java
// Before
@Column(name = "reviewed_at")
private java.sql.Timestamp reviewedAt;

// After
@Column(name = "reviewed_at")
private Long reviewedAt;
```

### 3. Service Layer Changes

#### CandidateNominationService.java
```java
// Before
candidate.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
candidate.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

// After
candidate.setCreatedAt(System.currentTimeMillis());
candidate.setUpdatedAt(System.currentTimeMillis());
```

## Migration Scripts

### For Existing Databases (002_change_timestamps_to_bigint.sql)
```sql
-- Convert existing timestamp data to bigint
UPDATE `candidates` SET 
    `created_at` = UNIX_TIMESTAMP(`created_at`) * 1000,
    `updated_at` = UNIX_TIMESTAMP(`updated_at`) * 1000
WHERE `created_at` IS NOT NULL AND `updated_at` IS NOT NULL;

-- Set default values for records without timestamps
UPDATE `candidates` SET 
    `created_at` = UNIX_TIMESTAMP(NOW()) * 1000,
    `updated_at` = UNIX_TIMESTAMP(NOW()) * 1000
WHERE `created_at` IS NULL OR `updated_at` IS NULL;

-- Change column types
ALTER TABLE `candidates` 
MODIFY COLUMN `created_at` bigint DEFAULT NULL,
MODIFY COLUMN `updated_at` bigint DEFAULT NULL;

-- Same for candidate_details
UPDATE `candidate_details` SET 
    `reviewed_at` = UNIX_TIMESTAMP(`reviewed_at`) * 1000
WHERE `reviewed_at` IS NOT NULL;

ALTER TABLE `candidate_details` 
MODIFY COLUMN `reviewed_at` bigint DEFAULT NULL;
```

### For New Databases (001_update_candidate_tables.sql)
Updated to create columns as `bigint` from the start:
```sql
ADD COLUMN `created_at` bigint DEFAULT NULL AFTER `status`,
ADD COLUMN `updated_at` bigint DEFAULT NULL AFTER `created_at`;
```

## Benefits

1. **Consistency**: All timestamps now use the same format (Unix milliseconds)
2. **Performance**: Bigint operations are faster than timestamp operations
3. **Simplicity**: No need to convert between Java Date objects and SQL timestamps
4. **Frontend Compatibility**: Raw timestamp values can be displayed directly

## Usage Examples

### Storing Current Time
```java
candidate.setCreatedAt(System.currentTimeMillis());
candidate.setUpdatedAt(System.currentTimeMillis());
```

### Displaying Timestamps
```javascript
// Frontend can display raw timestamp
formatDate(timestamp: number): string {
    return timestamp.toString(); // Shows: 1735689600000
}
```

### Converting to Date (if needed)
```java
// Convert bigint to Date
Date date = new Date(candidate.getCreatedAt());

// Convert Date to bigint
long timestamp = new Date().getTime();
```

## Migration Steps

### For Existing Database
1. Run the migration script:
   ```sql
   source database_migrations/002_change_timestamps_to_bigint.sql
   ```

2. Restart the Spring Boot application

3. Verify the changes:
   ```sql
   DESCRIBE candidates;
   DESCRIBE candidate_details;
   ```

### For New Database
1. Run the updated migration script:
   ```sql
   source database_migrations/001_update_candidate_tables.sql
   ```

2. Start the Spring Boot application

## Verification

### Check Column Types
```sql
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME IN ('candidates', 'candidate_details')
AND COLUMN_NAME IN ('created_at', 'updated_at', 'reviewed_at');
```

### Check Sample Data
```sql
SELECT 
    candidate_id,
    name,
    created_at,
    updated_at
FROM candidates 
LIMIT 5;
```

Expected output should show bigint values like:
```
candidate_id | name           | created_at    | updated_at
1           | John Doe       | 1735689600000 | 1735689600000
```

## Notes

- All timestamps are stored in UTC milliseconds since epoch
- Frontend displays raw timestamp values (e.g., 1735689600000)
- No automatic timestamp updates (handled in application code)
- Compatible with existing election timestamp format
