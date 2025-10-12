-- Create test elections with future timestamps (bigint values)
-- These will show up in the election dropdown

-- Delete any existing test elections
DELETE FROM elections WHERE name LIKE '%Test%' OR name LIKE '%General%' OR name LIKE '%Local%';

-- Create test elections with future timestamps
-- Current timestamp is around 1704067200000 (January 2024)
-- We'll create elections with timestamps in the future

INSERT INTO elections (name, start_date, end_date, status) VALUES
('General Election 2024', 1735689600000, 1735776000000, 'SCHEDULED'),
('Local Council Election 2024', 1736294400000, 1736380800000, 'SCHEDULED'),
('Student Union Election 2024', 1736899200000, 1736985600000, 'SCHEDULED'),
('Municipal Election 2024', 1737504000000, 1737590400000, 'SCHEDULED');

-- Verify the elections were created
SELECT 
    election_id,
    name,
    start_date,
    end_date,
    status
FROM elections 
ORDER BY start_date;
