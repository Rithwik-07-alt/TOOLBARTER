-- ==============================================================================
-- TOOLBARTER Database Initialization Script
-- DBMS: MySQL 8.0+
-- Run this script as a privileged MySQL user (e.g. root) to create the database.
-- ==============================================================================

-- 1. Create database with full Unicode (utf8mb4) support
CREATE DATABASE IF NOT EXISTS toolbarter
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 2. Select the database
USE toolbarter;

-- 3. Confirm creation and character set
SELECT 
    SCHEMA_NAME AS database_name, 
    DEFAULT_CHARACTER_SET_NAME AS charset, 
    DEFAULT_COLLATION_NAME AS collation 
FROM information_schema.SCHEMATA 
WHERE SCHEMA_NAME = 'toolbarter';
