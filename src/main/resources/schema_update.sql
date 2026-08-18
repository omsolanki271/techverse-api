-- Run this script in your MySQL Workbench / MySQL CLI on database `blog_app_api`
-- to add the missing timestamp columns to the `users` table:

USE blog_app_api;

ALTER TABLE users ADD COLUMN created_at DATETIME NULL;
ALTER TABLE users ADD COLUMN updated_at DATETIME NULL;
