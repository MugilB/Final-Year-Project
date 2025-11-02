-- Migration: Add approval_status field to users table
-- Date: 2024-01-XX
-- Description: Add approval_status field to track voter registration approval status

-- Add approval_status column to users table
ALTER TABLE `users` 
ADD COLUMN `approval_status` INT DEFAULT 1 COMMENT '0=rejected, 1=approved, 2=pending';

-- Update existing users to have approved status (1)
UPDATE `users` SET `approval_status` = 1 WHERE `approval_status` IS NULL;

-- Add index for better performance
CREATE INDEX `idx_users_approval_status` ON `users` (`approval_status`);

-- Verify the changes
DESCRIBE `users`;








