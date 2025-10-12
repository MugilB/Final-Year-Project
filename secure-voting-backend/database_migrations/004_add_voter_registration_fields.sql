-- Migration: Add missing fields to user_details table for voter registration
-- Date: 2024-01-XX
-- Description: Add aadhar_card_link and profile_picture_link fields to support voter registration

-- Add missing fields to user_details table
ALTER TABLE `user_details` 
ADD COLUMN `aadhar_card_link` TEXT DEFAULT NULL COMMENT 'Google Drive link to Aadhar card document',
ADD COLUMN `profile_picture_link` TEXT DEFAULT NULL COMMENT 'Google Drive link to profile picture';

-- Add indexes for better performance (optional)
CREATE INDEX `idx_user_details_aadhar_link` ON `user_details` (`aadhar_card_link`(100));
CREATE INDEX `idx_user_details_profile_link` ON `user_details` (`profile_picture_link`(100));

-- Verify the changes
DESCRIBE `user_details`;





