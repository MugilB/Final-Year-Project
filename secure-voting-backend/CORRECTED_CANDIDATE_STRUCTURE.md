# Corrected Candidate Database Structure

## Overview
Fixed the database structure to properly separate concerns between tables. The `party_secret_code` has been moved to the `party_details` table where it belongs, and personal information fields are correctly placed in the `candidate_details` table.

## Final Database Structure

### 1. `candidates` table
**Purpose**: Core candidate information and election relationships
```sql
CREATE TABLE `candidates` (
  `candidate_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `party_id` int NOT NULL,
  `election_id` int NOT NULL,
  `ward_id` int DEFAULT NULL,
  `status` enum('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`candidate_id`),
  KEY `election_id` (`election_id`),
  KEY `ward_id` (`ward_id`),
  KEY `party_id` (`party_id`),
  KEY `status` (`status`),
  CONSTRAINT `candidates_ibfk_1` FOREIGN KEY (`election_id`) REFERENCES `elections` (`election_id`) ON DELETE CASCADE,
  CONSTRAINT `candidates_ibfk_2` FOREIGN KEY (`ward_id`) REFERENCES `wards` (`ward_id`),
  CONSTRAINT `candidates_ibfk_3` FOREIGN KEY (`party_id`) REFERENCES `party_details` (`party_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

**Fields**:
- `candidate_id`: Primary key
- `name`: Candidate's name
- `party_id`: Foreign key to party_details
- `election_id`: Foreign key to elections
- `ward_id`: Foreign key to wards (optional)
- `status`: Nomination status (PENDING/APPROVED/REJECTED)
- `created_at`: When nomination was submitted
- `updated_at`: Last modification timestamp

### 2. `candidate_details` table
**Purpose**: Personal information and additional candidate details
```sql
CREATE TABLE `candidate_details` (
  `candidate_id` int NOT NULL,
  `email` varchar(255) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `gender` varchar(20) NOT NULL,
  `age` int NOT NULL,
  `address` text NOT NULL,
  `aadhar_card_link` varchar(500) NOT NULL,
  `biography` text,
  `manifesto_summary` text,
  `review_notes` text,
  `reviewed_by` varchar(100),
  `reviewed_at` timestamp NULL,
  PRIMARY KEY (`candidate_id`),
  CONSTRAINT `candidate_details_ibfk_1` FOREIGN KEY (`candidate_id`) REFERENCES `candidates` (`candidate_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

**Fields**:
- `candidate_id`: Primary key (same as candidates.candidate_id)
- `email`: Candidate's email address
- `phone_number`: Contact phone number
- `gender`: Gender information
- `age`: Age of the candidate
- `address`: Complete address
- `aadhar_card_link`: Google Drive link to Aadhar card
- `biography`: Candidate's biography
- `manifesto_summary`: Summary of candidate's manifesto
- `review_notes`: Admin notes during review
- `reviewed_by`: Admin who reviewed the nomination
- `reviewed_at`: When the review was completed

### 3. `party_details` table
**Purpose**: Political party information and secret codes
```sql
CREATE TABLE `party_details` (
  `party_id` int NOT NULL AUTO_INCREMENT,
  `party_name` varchar(100) NOT NULL,
  `party_symbol` varchar(50) DEFAULT NULL,
  `party_secret_code` varchar(100) NOT NULL,
  PRIMARY KEY (`party_id`),
  UNIQUE KEY `party_name` (`party_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

**Fields**:
- `party_id`: Primary key
- `party_name`: Name of the political party
- `party_symbol`: Party symbol/logo
- `party_secret_code`: Secret code for party verification

## Key Changes Made

### 1. Moved `party_secret_code` to `party_details`
- **Before**: `party_secret_code` was in `candidates` table
- **After**: `party_secret_code` is in `party_details` table
- **Reason**: Secret codes belong to parties, not individual candidates

### 2. Changed `candidates.party` to `candidates.party_id`
- **Before**: `candidates` had `party` (string) and `party_secret_code`
- **After**: `candidates` has `party_id` (foreign key to `party_details`)
- **Reason**: Proper normalization and foreign key relationships

### 3. Personal information in `candidate_details`
- **Before**: Personal info was mixed in `candidates` table
- **After**: Personal info is properly separated in `candidate_details` table
- **Reason**: Better separation of concerns and data organization

## Backend Model Updates

### 1. Candidate Model
```java
@Entity
@Table(name = "candidates")
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "candidate_id")
    private int candidateId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "party_id", nullable = false)
    private int partyId;

    @Column(name = "election_id", nullable = false)
    private int electionId;

    @Column(name = "ward_id")
    private Integer wardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CandidateStatus status = CandidateStatus.PENDING;

    @Column(name = "created_at")
    private java.sql.Timestamp createdAt;

    @Column(name = "updated_at")
    private java.sql.Timestamp updatedAt;

    // Relationships
    @OneToOne(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private CandidateDetails candidateDetails;
}
```

### 2. CandidateDetails Model
```java
@Entity
@Table(name = "candidate_details")
public class CandidateDetails {
    @Id
    @Column(name = "candidate_id")
    private int candidateId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "age", nullable = false)
    private int age;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "aadhar_card_link", nullable = false)
    private String aadharCardLink;

    // ... other fields
}
```

### 3. PartyDetails Model
```java
@Entity
@Table(name = "party_details")
public class PartyDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "party_id")
    private int partyId;

    @Column(name = "party_name", nullable = false, unique = true)
    private String partyName;

    @Column(name = "party_symbol")
    private String partySymbol;

    @Column(name = "party_secret_code", nullable = false)
    private String partySecretCode;

    @OneToMany(mappedBy = "partyId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Candidate> candidates;
}
```

## Service Layer Updates

### CandidateNominationService
- Validates party exists by name
- Validates party secret code matches
- Creates candidate with proper party_id reference
- Creates candidate_details with personal information

### Key Validation Logic
```java
// Validate party exists and secret code matches
Optional<PartyDetails> partyOpt = partyDetailsRepository.findByPartyName(request.getParty());
if (partyOpt.isEmpty()) {
    throw new IllegalArgumentException("Party not found: " + request.getParty());
}

PartyDetails party = partyOpt.get();
if (!party.getPartySecretCode().equals(request.getPartySecretCode())) {
    throw new IllegalArgumentException("Invalid party secret code");
}
```

## Benefits of This Structure

1. **Proper Normalization**: No data duplication
2. **Foreign Key Integrity**: Proper relationships between tables
3. **Separation of Concerns**: Each table has a clear purpose
4. **Scalability**: Easy to add new party or candidate fields
5. **Data Integrity**: Constraints ensure data consistency
6. **Security**: Party secret codes are centralized and managed properly

## Migration Path

1. Run the migration script to add new columns
2. Update existing data with default values
3. Deploy updated backend models and services
4. Test candidate nomination flow
5. Verify party validation works correctly

The structure is now properly normalized and follows database design best practices!
