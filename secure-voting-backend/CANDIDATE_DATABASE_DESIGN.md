# Candidate Database Schema Redesign

## Current Issues
The existing `candidates` and `candidate_details` tables don't have enough fields to store all the information from the candidate nomination form.

## Candidate Nomination Form Fields
From the frontend form, we need to store:
- Candidate Name
- Gender
- Age
- Email
- Phone Number
- Address
- Party
- Party Secret Code
- Aadhar Card Link (Google Drive URL)

## Proposed New Database Structure

### Option 1: Extend Existing Tables (Recommended)

#### Updated `candidates` table:
```sql
CREATE TABLE `candidates` (
  `candidate_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `party_id` int NOT NULL,
  `election_id` int NOT NULL,
  `ward_id` int DEFAULT NULL,
  `status` enum('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`candidate_id`),
  KEY `election_id` (`election_id`),
  KEY `ward_id` (`ward_id`),
  KEY `party_id` (`party_id`),
  KEY `status` (`status`),
  CONSTRAINT `candidates_ibfk_1` FOREIGN KEY (`election_id`) REFERENCES `elections` (`election_id`) ON DELETE CASCADE,
  CONSTRAINT `candidates_ibfk_2` FOREIGN KEY (`ward_id`) REFERENCES `wards` (`ward_id`),
  CONSTRAINT `candidates_ibfk_3` FOREIGN KEY (`party_id`) REFERENCES `party_details` (`party_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

#### Updated `party_details` table:
```sql
CREATE TABLE `party_details` (
  `party_id` int NOT NULL AUTO_INCREMENT,
  `party_name` varchar(100) NOT NULL,
  `party_symbol` varchar(50) DEFAULT NULL,
  `party_secret_code` varchar(100) NOT NULL,
  PRIMARY KEY (`party_id`),
  UNIQUE KEY `party_name` (`party_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

#### Updated `candidate_details` table:
```sql
CREATE TABLE `candidate_details` (
  `candidate_id` int NOT NULL,
  `email` varchar(255) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `gender` varchar(20) NOT NULL,
  `age` int NOT NULL,
  `address` text NOT NULL,
  `aadhar_card_link` varchar(500) NOT NULL,
  `biography` text,
  `manifesto_summary` text,
  `review_notes` text,
  `reviewed_by` varchar(100),
  `reviewed_at` timestamp NULL,
  PRIMARY KEY (`candidate_id`),
  CONSTRAINT `candidate_details_ibfk_1` FOREIGN KEY (`candidate_id`) REFERENCES `candidates` (`candidate_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### Option 2: Create New Nomination Table (Alternative)

#### New `candidate_nominations` table:
```sql
CREATE TABLE `candidate_nominations` (
  `nomination_id` int NOT NULL AUTO_INCREMENT,
  `candidate_name` varchar(100) NOT NULL,
  `email` varchar(255) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `gender` varchar(20) NOT NULL,
  `age` int NOT NULL,
  `address` text NOT NULL,
  `aadhar_card_link` varchar(500) NOT NULL,
  `party` varchar(100) NOT NULL,
  `party_secret_code` varchar(100) NOT NULL,
  `election_id` int NOT NULL,
  `ward_id` int DEFAULT NULL,
  `status` enum('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`nomination_id`),
  KEY `election_id` (`election_id`),
  KEY `ward_id` (`ward_id`),
  KEY `status` (`status`),
  CONSTRAINT `candidate_nominations_ibfk_1` FOREIGN KEY (`election_id`) REFERENCES `elections` (`election_id`) ON DELETE CASCADE,
  CONSTRAINT `candidate_nominations_ibfk_2` FOREIGN KEY (`ward_id`) REFERENCES `wards` (`ward_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

## Migration Strategy

### For Option 1 (Recommended):
1. Add new columns to existing `candidates` table
2. Add new columns to existing `candidate_details` table
3. Update existing data to have default values
4. Update backend models and APIs

### For Option 2:
1. Create new `candidate_nominations` table
2. Keep existing `candidates` table for approved candidates
3. Create workflow to move approved nominations to candidates table
4. Update backend to handle both tables

## Recommended Approach: Option 1

**Benefits**:
- Simpler data model
- Single source of truth for candidate information
- Easier to maintain
- Better for reporting and analytics

**Migration Steps**:
1. Add new columns to `candidates` table
2. Add new columns to `candidate_details` table
3. Update backend models
4. Create candidate nomination API endpoint
5. Update existing candidate management functionality

## New Fields Explanation

### `candidates` table additions:
- `party_id`: Foreign key to party_details table
- `status`: Nomination status (PENDING/APPROVED/REJECTED)
- `created_at`: When nomination was submitted
- `updated_at`: Last modification timestamp

### `candidate_details` table additions:
- `email`: Candidate's email address
- `phone_number`: Contact phone number
- `gender`: Gender information
- `age`: Age of the candidate
- `address`: Complete address
- `aadhar_card_link`: Google Drive link to Aadhar card
- `review_notes`: Admin notes during review
- `reviewed_by`: Admin who reviewed the nomination
- `reviewed_at`: When the review was completed

### `party_details` table additions:
- `party_secret_code`: Party verification code for candidate nominations

## Backend Changes Needed

1. **Update Candidate Model**: Add new fields
2. **Create Nomination API**: Endpoint to submit nominations
3. **Create Review API**: Admin endpoint to approve/reject nominations
4. **Update Existing APIs**: Modify existing candidate endpoints
5. **Add Validation**: Server-side validation for nomination data
