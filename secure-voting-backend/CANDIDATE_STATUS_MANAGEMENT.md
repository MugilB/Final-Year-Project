# Candidate Status Management

## Overview
The candidate nomination system uses a simple `status` field in the `candidates` table to manage the lifecycle of candidate nominations. No separate tables are needed for pending/approved candidates.

## Status Values

### `PENDING`
- **When**: New candidate nomination submitted
- **Description**: Candidate has submitted nomination but not yet reviewed by admin
- **Actions**: Admin can approve or reject

### `APPROVED`
- **When**: Admin approves the nomination
- **Description**: Candidate is officially approved and can participate in elections
- **Actions**: Candidate appears in voting ballots

### `REJECTED`
- **When**: Admin rejects the nomination
- **Description**: Candidate nomination was rejected and cannot participate
- **Actions**: Candidate cannot vote or be voted for

## Database Queries

### Get Pending Nominations
```sql
SELECT c.*, cd.*, p.party_name 
FROM candidates c
LEFT JOIN candidate_details cd ON c.candidate_id = cd.candidate_id
LEFT JOIN party_details p ON c.party_id = p.party_id
WHERE c.status = 'PENDING';
```

### Get Approved Candidates
```sql
SELECT c.*, cd.*, p.party_name 
FROM candidates c
LEFT JOIN candidate_details cd ON c.candidate_id = cd.candidate_id
LEFT JOIN party_details p ON c.party_id = p.party_id
WHERE c.status = 'APPROVED';
```

### Get Rejected Candidates
```sql
SELECT c.*, cd.*, p.party_name 
FROM candidates c
LEFT JOIN candidate_details cd ON c.candidate_id = cd.candidate_id
LEFT JOIN party_details p ON c.party_id = p.party_id
WHERE c.status = 'REJECTED';
```

### Get All Candidates by Election
```sql
SELECT c.*, cd.*, p.party_name 
FROM candidates c
LEFT JOIN candidate_details cd ON c.candidate_id = cd.candidate_id
LEFT JOIN party_details p ON c.party_id = p.party_id
WHERE c.election_id = ? AND c.status = 'APPROVED';
```

## Backend Service Methods

### CandidateNominationService
```java
// Get pending nominations
public List<Candidate> getPendingNominations() {
    return candidateRepository.findByStatus(CandidateStatus.PENDING);
}

// Get approved candidates
public List<Candidate> getApprovedCandidates() {
    return candidateRepository.findByStatus(CandidateStatus.APPROVED);
}

// Get candidates by election and status
public List<Candidate> getCandidatesByElectionAndStatus(int electionId, CandidateStatus status) {
    return candidateRepository.findByElectionIdAndStatus(electionId, status);
}

// Approve nomination
public Candidate approveNomination(int candidateId, String reviewedBy, String reviewNotes) {
    // Update status to APPROVED
    // Add review information to candidate_details
}

// Reject nomination
public Candidate rejectNomination(int candidateId, String reviewedBy, String reviewNotes, String reason) {
    // Update status to REJECTED
    // Add review information to candidate_details
}
```

## API Endpoints

### Get Pending Nominations
```
GET /api/candidate-nominations/pending
```

### Get Approved Candidates
```
GET /api/candidates/approved
```

### Approve Nomination
```
PUT /api/candidate-nominations/{candidateId}/approve
Body: { "reviewNotes": "..." }
```

### Reject Nomination
```
PUT /api/candidate-nominations/{candidateId}/reject
Body: { "reason": "...", "reviewNotes": "..." }
```

## Benefits of Single Table Approach

1. **Simplicity**: One table manages all candidate states
2. **Consistency**: No data duplication or synchronization issues
3. **Flexibility**: Easy to add new statuses if needed
4. **Performance**: Simple queries with indexed status field
5. **Maintainability**: Less complex database structure

## Status Transition Flow

```
SUBMIT NOMINATION → PENDING → APPROVED/REJECTED
```

1. **Submit**: Candidate submits nomination → Status = PENDING
2. **Review**: Admin reviews nomination
3. **Decision**: Admin approves or rejects → Status = APPROVED/REJECTED

## Indexes for Performance

```sql
-- Index on status for fast filtering
ALTER TABLE candidates ADD INDEX idx_status (status);

-- Composite index for election + status queries
ALTER TABLE candidates ADD INDEX idx_election_status (election_id, status);
```

This approach is much simpler and more maintainable than having separate tables for different candidate states!
