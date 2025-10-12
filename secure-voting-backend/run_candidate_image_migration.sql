-- Simple script to add candidate image field
-- Copy and paste these commands into your MySQL client

USE secure_voting;

-- Add candidate_image_link column to candidate_details table
ALTER TABLE `candidate_details`
ADD COLUMN `candidate_image_link` varchar(500) DEFAULT NULL AFTER `aadhar_card_link`;

-- Verify the changes
DESCRIBE candidate_details;

-- Show sample data
SELECT 
    candidate_id,
    email,
    aadhar_card_link,
    candidate_image_link
FROM candidate_details 
LIMIT 3;
