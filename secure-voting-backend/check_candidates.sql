-- Quick script to check candidates in database
USE secure_voting;

-- Check all candidates
SELECT 
    'ALL CANDIDATES' as type,
    COUNT(*) as count
FROM candidates

UNION ALL

-- Check approved candidates
SELECT 
    'APPROVED CANDIDATES' as type,
    COUNT(*) as count
FROM candidates 
WHERE status = 'APPROVED'

UNION ALL

-- Check pending candidates
SELECT 
    'PENDING CANDIDATES' as type,
    COUNT(*) as count
FROM candidates 
WHERE status = 'PENDING'

UNION ALL

-- Check rejected candidates
SELECT 
    'REJECTED CANDIDATES' as type,
    COUNT(*) as count
FROM candidates 
WHERE status = 'REJECTED';

-- Show detailed candidate list
SELECT 
    candidate_id,
    name,
    election_id,
    party_id,
    status,
    created_at
FROM candidates 
ORDER BY candidate_id;

-- Show elections
SELECT 
    election_id,
    name,
    status
FROM elections 
ORDER BY election_id;
