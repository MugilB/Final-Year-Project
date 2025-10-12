-- Test script to check and create elections for testing

-- First, let's see what elections exist
SELECT * FROM elections;

-- Check current timestamp for reference
SELECT NOW() as current_time, UNIX_TIMESTAMP() * 1000 as current_timestamp_ms;

-- Create some test elections with future dates if none exist
-- Election 1: Starts in 1 week
INSERT IGNORE INTO elections (name, start_date, end_date, status) 
VALUES (
    'General Election 2024', 
    UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 7 DAY)) * 1000,
    UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 8 DAY)) * 1000,
    'SCHEDULED'
);

-- Election 2: Starts in 2 weeks  
INSERT IGNORE INTO elections (name, start_date, end_date, status) 
VALUES (
    'Local Council Election 2024', 
    UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 14 DAY)) * 1000,
    UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 15 DAY)) * 1000,
    'SCHEDULED'
);

-- Election 3: Starts in 1 month
INSERT IGNORE INTO elections (name, start_date, end_date, status) 
VALUES (
    'Student Union Election 2024', 
    UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 30 DAY)) * 1000,
    UNIX_TIMESTAMP(DATE_ADD(NOW(), INTERVAL 31 DAY)) * 1000,
    'SCHEDULED'
);

-- Check elections again after insertion
SELECT 
    election_id,
    name,
    FROM_UNIXTIME(start_date/1000) as start_date_readable,
    FROM_UNIXTIME(end_date/1000) as end_date_readable,
    status,
    start_date,
    end_date
FROM elections 
ORDER BY start_date;
