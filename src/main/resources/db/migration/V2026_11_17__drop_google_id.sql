-- Safe schema migration for TechVerse: remove legacy Google auth column
-- This migration preserves existing user data and only removes the unused column.

ALTER TABLE users DROP COLUMN IF EXISTS google_id;
