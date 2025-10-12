-- Migration: Update candidate tables to support nomination form data
-- Date: 2025-01-23
-- Description: Add new fields to candidates and candidate_details tables

-- Step 1: Add new columns to candidates table
ALTER TABLE `candidates` 
ADD COLUMN `status` enum('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' AFTER `ward_id`,
ADD COLUMN `created_at` bigint DEFAULT NULL AFTER `status`,
ADD COLUMN `updated_at` bigint DEFAULT NULL AFTER `created_at`;

-- Step 2: Add indexes for new fields
ALTER TABLE `candidates`
ADD INDEX `idx_status` (`status`);

-- Step 2.1: Add party_secret_code to party_details table
ALTER TABLE `party_details`
ADD COLUMN `party_secret_code` varchar(100) NOT NULL AFTER `party_symbol`;

-- Step 3: Add new columns to candidate_details table
ALTER TABLE `candidate_details`
ADD COLUMN `email` varchar(255) NOT NULL AFTER `candidate_id`,
ADD COLUMN `phone_number` varchar(20) NOT NULL AFTER `email`,
ADD COLUMN `gender` varchar(20) NOT NULL AFTER `phone_number`,
ADD COLUMN `age` int NOT NULL AFTER `gender`,
ADD COLUMN `address` text NOT NULL AFTER `age`,
ADD COLUMN `aadhar_card_link` varchar(500) NOT NULL AFTER `address`,
ADD COLUMN `review_notes` text AFTER `manifesto_summary`,
ADD COLUMN `reviewed_by` varchar(100) AFTER `review_notes`,
ADD COLUMN `reviewed_at` bigint DEFAULT NULL AFTER `reviewed_by`;

-- Step 4: Update existing records to have default values
-- Set default values for existing candidates (they are already approved)
UPDATE `candidates` SET 
    `status` = 'APPROVED'
WHERE `status` IS NULL;

-- Update existing party_details records with default secret codes
UPDATE `party_details` SET 
    `party_secret_code` = 'DEFAULT_SECRET_CODE'
WHERE `party_secret_code` IS NULL OR `party_secret_code` = '';

-- Insert Independent Candidate party if it doesn't exist
INSERT IGNORE INTO `party_details` (`party_name`, `party_symbol`, `party_secret_code`) 
VALUES ('Independent Candidate', 'IND', 'INDEPENDENT_SECRET_2024');

-- Update existing candidate_details records with default values
UPDATE `candidate_details` SET 
    `email` = 'default@example.com',
    `phone_number` = '0000000000',
    `gender` = 'Not Specified',
    `age` = 25,
    `address` = 'Address not provided',
    `aadhar_card_link` = 'https://drive.google.com/not-provided'
WHERE `email` IS NULL OR `email` = '';

-- Step 5: Add constraints (optional - can be added later if needed)
-- ALTER TABLE `candidate_details` ADD CONSTRAINT `chk_age` CHECK (`age` >= 18 AND `age` <= 100);
-- ALTER TABLE `candidate_details` ADD CONSTRAINT `chk_email` CHECK (`email` REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$');

-- Step 6: Migration completed successfully
-- The existing tables now support candidate nominations with proper status management
-- Use the status field in candidates table to filter PENDING, APPROVED, or REJECTED candidates
