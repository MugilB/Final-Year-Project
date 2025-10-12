-- Migration: Add candidate image upload field
-- Date: 2025-01-23
-- Description: Add candidate_image_link column to candidate_details table

-- Step 1: Add candidate_image_link column to candidate_details table
ALTER TABLE `candidate_details`
ADD COLUMN `candidate_image_link` varchar(500) DEFAULT NULL AFTER `aadhar_card_link`;

-- Step 2: Migration completed successfully
-- The candidate_details table now supports candidate image uploads via Google Drive links
