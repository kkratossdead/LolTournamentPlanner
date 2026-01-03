-- Migration script to add email column to users table
-- Run this script to update your existing database

-- Add email column to users table
ALTER TABLE `users` ADD COLUMN `email` VARCHAR(255) NULL AFTER `username`;

-- Add unique index on email
ALTER TABLE `users` ADD UNIQUE INDEX `idx_users_email` (`email`);

-- Update existing users with placeholder emails (optional - you can customize this)
-- UPDATE `users` SET `email` = CONCAT(username, '@placeholder.local') WHERE `email` IS NULL;
