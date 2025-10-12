# Admin Dashboard - Show Only Approved Candidates Fix

## Problem
The admin dashboard was showing ALL candidates (including PENDING and REJECTED) in both the candidate list and bar chart, instead of only showing APPROVED candidates.

## Root Cause
The existing API endpoints were returning all candidates regardless of their status:
- `/api/candidates` - Returns all candidates
- `/api/candidates/election/{electionId}` - Returns all candidates for an election
- `/api/elections/with-candidates` - Returns all elections with all candidates
- `/api/elections/{electionId}/with-candidates` - Returns specific election with all candidates

## Solution
Created new API endpoints that filter only APPROVED candidates:

### New API Endpoints

#### Candidate Endpoints
1. **`GET /api/candidates/approved`** - Returns only approved candidates
2. **`GET /api/candidates/election/{electionId}/approved`** - Returns only approved candidates for a specific election

#### Election Endpoints
3. **`GET /api/elections/with-approved-candidates`** - Returns all elections with only approved candidates
4. **`GET /api/elections/{electionId}/with-approved-candidates`** - Returns specific election with only approved candidates

## Backend Changes

### 1. CandidateController.java
```java
// Get only approved candidates
@GetMapping("/approved")
public List<Candidate> getApprovedCandidates() {
    return candidateService.getApprovedCandidates();
}

// Get only approved candidates for a specific election
@GetMapping("/election/{electionId}/approved")
public List<Candidate> getApprovedCandidatesByElectionId(@PathVariable int electionId) {
    return candidateService.getApprovedCandidatesByElectionId(electionId);
}
```

### 2. ElectionController.java
```java
@GetMapping("/with-approved-candidates")
public List<Election> getAllElectionsWithApprovedCandidates() {
    return electionService.getAllElectionsWithApprovedCandidates();
}

@GetMapping("/{electionId}/with-approved-candidates")
public ResponseEntity<Election> getElectionWithApprovedCandidates(@PathVariable int electionId) {
    Election election = electionService.getElectionWithApprovedCandidates(electionId);
    return election != null ? ResponseEntity.ok(election) : ResponseEntity.notFound().build();
}
```

### 3. CandidateService.java
```java
// Get only approved candidates
public List<Candidate> getApprovedCandidates() {
    List<Candidate> candidates = candidateRepository.findByStatus(CandidateStatus.APPROVED);
    // Ensure candidateDetails is properly initialized for each candidate
    for (Candidate candidate : candidates) {
        if (candidate.getCandidateDetails() == null) {
            // Create an empty CandidateDetails object if none exists
            CandidateDetails details = new CandidateDetails();
            details.setCandidateId(candidate.getCandidateId());
            details.setBiography(null);
            details.setManifestoSummary(null);
            candidate.setCandidateDetails(details);
        }
    }
    return candidates;
}

// Get only approved candidates for a specific election
public List<Candidate> getApprovedCandidatesByElectionId(int electionId) {
    List<Candidate> candidates = candidateRepository.findByElectionIdAndStatus(electionId, CandidateStatus.APPROVED);
    // Ensure candidateDetails is properly initialized for each candidate
    for (Candidate candidate : candidates) {
        if (candidate.getCandidateDetails() == null) {
            // Create an empty CandidateDetails object if none exists
            CandidateDetails details = new CandidateDetails();
            details.setCandidateId(candidate.getCandidateId());
            details.setBiography(null);
            details.setManifestoSummary(null);
            candidate.setCandidateDetails(details);
        }
    }
    return candidates;
}
```

### 4. ElectionService.java
```java
// Get all elections with approved candidates only
public List<Election> getAllElectionsWithApprovedCandidates() {
    List<Election> elections = electionRepository.findAll();
    for (Election election : elections) {
        List<Candidate> approvedCandidates = candidateRepository.findByElectionIdAndStatus(election.getElectionId(), CandidateStatus.APPROVED);
        election.setCandidates(approvedCandidates);
    }
    return elections;
}

// Get election with approved candidates only
public Election getElectionWithApprovedCandidates(int electionId) {
    Election election = electionRepository.findById(electionId).orElse(null);
    if (election != null) {
        List<Candidate> approvedCandidates = candidateRepository.findByElectionIdAndStatus(electionId, CandidateStatus.APPROVED);
        election.setCandidates(approvedCandidates);
    }
    return election;
}
```

### 5. WebSecurityConfig.java
Added public access to new endpoints:
```java
.antMatchers("/api/candidates/approved").permitAll()
.antMatchers("/api/candidates/election/*/approved").permitAll()
.antMatchers("/api/elections/with-approved-candidates").permitAll()
.antMatchers("/api/elections/*/with-approved-candidates").permitAll()
```

## Database Queries Used

### Repository Methods
```java
// Find candidates by status
List<Candidate> findByStatus(CandidateStatus status);

// Find candidates by election and status
List<Candidate> findByElectionIdAndStatus(int electionId, CandidateStatus status);
```

### SQL Queries Generated
```sql
-- Get all approved candidates
SELECT * FROM candidates WHERE status = 'APPROVED';

-- Get approved candidates for specific election
SELECT * FROM candidates WHERE election_id = ? AND status = 'APPROVED';
```

## Frontend Integration

### For Admin Dashboard
Update the frontend to use the new approved candidate endpoints:

#### Before (showing all candidates):
```javascript
// Old endpoints
fetch('/api/candidates')
fetch('/api/elections/with-candidates')
fetch('/api/elections/1/with-candidates')
```

#### After (showing only approved candidates):
```javascript
// New endpoints
fetch('/api/candidates/approved')
fetch('/api/elections/with-approved-candidates')
fetch('/api/elections/1/with-approved-candidates')
```

## API Response Examples

### Get Approved Candidates
```json
GET /api/candidates/approved

[
  {
    "candidateId": 1,
    "name": "John Doe",
    "electionId": 1,
    "partyId": 1,
    "status": "APPROVED",
    "createdAt": 1735689600000,
    "updatedAt": 1735689600000,
    "candidateDetails": {
      "email": "john@example.com",
      "phoneNumber": "1234567890",
      "gender": "Male",
      "age": 30,
      "address": "123 Main St"
    }
  }
]
```

### Get Elections with Approved Candidates
```json
GET /api/elections/with-approved-candidates

[
  {
    "electionId": 1,
    "name": "General Election 2024",
    "startDate": 1735689600000,
    "endDate": 1735776000000,
    "status": "SCHEDULED",
    "candidates": [
      {
        "candidateId": 1,
        "name": "John Doe",
        "status": "APPROVED"
      },
      {
        "candidateId": 2,
        "name": "Jane Smith",
        "status": "APPROVED"
      }
    ]
  }
]
```

## Testing

### Test the New Endpoints
```bash
# Test approved candidates endpoint
curl -X GET http://localhost:8081/api/candidates/approved

# Test approved candidates for specific election
curl -X GET http://localhost:8081/api/candidates/election/1/approved

# Test elections with approved candidates
curl -X GET http://localhost:8081/api/elections/with-approved-candidates

# Test specific election with approved candidates
curl -X GET http://localhost:8081/api/elections/1/with-approved-candidates
```

### Verify Results
- Only candidates with `status: "APPROVED"` should be returned
- PENDING and REJECTED candidates should be excluded
- Bar chart should only show approved candidates
- Candidate list should only show approved candidates

## Migration Steps

1. **Restart Backend Server** (when Maven is available)
2. **Update Frontend** to use new approved candidate endpoints
3. **Test Admin Dashboard** to verify only approved candidates are shown
4. **Verify Bar Chart** shows only approved candidates

## Benefits

- ✅ **Clean Admin Dashboard**: Only shows approved candidates
- ✅ **Accurate Bar Charts**: Vote counts only for approved candidates
- ✅ **Better User Experience**: Admins see only relevant candidates
- ✅ **Data Integrity**: Separates approved candidates from pending/rejected ones
- ✅ **Backward Compatibility**: Original endpoints still work for other purposes

## Files Modified

### Backend
- `CandidateController.java` - Added approved candidate endpoints
- `ElectionController.java` - Added approved candidate election endpoints
- `CandidateService.java` - Added approved candidate service methods
- `ElectionService.java` - Added approved candidate election service methods
- `WebSecurityConfig.java` - Added security permissions for new endpoints

The admin dashboard will now show only approved candidates in both the candidate list and bar chart!
