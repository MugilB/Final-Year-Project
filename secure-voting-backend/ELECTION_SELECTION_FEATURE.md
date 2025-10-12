# Election Selection Feature Implementation

## Overview
Added election selection functionality to both candidate nomination and voter registration forms. Users can now choose which election they want to participate in.

## Features Implemented

### 1. Backend API Endpoint
- **New Endpoint**: `GET /api/elections/for-nominations`
- **Purpose**: Returns elections that are open for nominations (not yet started)
- **Filter**: Only returns elections where `start_date > current_time`

### 2. Frontend Updates

#### Candidate Nomination Form
- ✅ Added election selection dropdown at the top of the form
- ✅ Shows election name and start date
- ✅ Required field validation
- ✅ Loading state while fetching elections
- ✅ Error handling for no available elections

#### Voter Registration Form
- ✅ Added election selection dropdown in a new section
- ✅ Shows election name and start date
- ✅ Required field validation
- ✅ Loading state while fetching elections
- ✅ Error handling for no available elections

### 3. Backend Validation
- ✅ Validates election exists
- ✅ Validates election hasn't started yet
- ✅ Validates election status is "SCHEDULED"
- ✅ Stores election ID in database

## Database Schema

### Elections Table
```sql
CREATE TABLE `elections` (
  `election_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `start_date` bigint NOT NULL,
  `end_date` bigint NOT NULL,
  `status` varchar(20) NOT NULL,
  PRIMARY KEY (`election_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
```

### Candidates Table
```sql
CREATE TABLE `candidates` (
  `candidate_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `election_id` int NOT NULL,
  `party_id` int DEFAULT NULL,
  `ward_id` int DEFAULT NULL,
  `status` enum('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`candidate_id`),
  KEY `election_id` (`election_id`),
  KEY `party_id` (`party_id`),
  KEY `ward_id` (`ward_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `candidates_ibfk_1` FOREIGN KEY (`election_id`) REFERENCES `elections` (`election_id`) ON DELETE CASCADE,
  CONSTRAINT `candidates_ibfk_2` FOREIGN KEY (`party_id`) REFERENCES `party_details` (`party_id`),
  CONSTRAINT `candidates_ibfk_3` FOREIGN KEY (`ward_id`) REFERENCES `wards` (`ward_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
```

## API Endpoints

### Get Elections for Nominations
```
GET /api/elections/for-nominations
```

**Response:**
```json
[
  {
    "electionId": 1,
    "name": "General Election 2024",
    "startDate": 1704067200000,
    "endDate": 1704153600000,
    "status": "SCHEDULED"
  }
]
```

### Submit Candidate Nomination
```
POST /api/candidate-nominations
```

**Request Body:**
```json
{
  "candidateName": "John Doe",
  "gender": "Male",
  "age": 30,
  "email": "john@example.com",
  "phoneNumber": "1234567890",
  "address": "123 Main St, City",
  "party": "Independent Candidate",
  "partySecretCode": "INDEPENDENT_SECRET_2024",
  "aadharCardLink": "https://drive.google.com/file/d/...",
  "electionId": 1
}
```

## Security Configuration
Updated `WebSecurityConfig.java` to allow public access to:
- `/api/elections/for-nominations` - For fetching available elections
- `/api/candidate-nominations/**` - For candidate nominations
- `/api/voter-registration/**` - For voter registrations

## User Experience

### Election Selection Dropdown
- **Position**: At the top of both forms
- **Format**: "Election Name (Starts: Date Time)"
- **Validation**: Required field
- **Loading State**: Shows spinner while loading
- **Empty State**: Shows message when no elections available

### Form Flow
1. User opens nomination/registration form
2. Elections are automatically loaded
3. User selects an election from dropdown
4. User fills out remaining form fields
5. Form validates election selection
6. Data is submitted with selected election ID

## Error Handling

### Frontend
- ✅ Loading state while fetching elections
- ✅ Error message if elections fail to load
- ✅ Empty state message if no elections available
- ✅ Form validation for required election selection

### Backend
- ✅ Election not found error
- ✅ Election already started error
- ✅ Election not in SCHEDULED status error
- ✅ Proper HTTP status codes and error messages

## Testing

### Test Scenarios
1. **Normal Flow**: Select election, fill form, submit successfully
2. **No Elections**: Form shows "No elections available" message
3. **API Error**: Form shows error message if elections fail to load
4. **Invalid Election**: Backend returns appropriate error message
5. **Election Started**: Backend rejects nomination for started elections

### Manual Testing Steps
1. Start backend server
2. Open candidate nomination form
3. Verify elections dropdown loads
4. Select an election
5. Fill out form completely
6. Submit form
7. Verify data is stored with correct election ID

## Future Enhancements
- Add election description in dropdown
- Show election end date
- Add election type/category
- Filter elections by user eligibility
- Add election preview/details modal

## Files Modified

### Backend
- `ElectionController.java` - Added `/for-nominations` endpoint
- `ElectionService.java` - Added `getElectionsForNominations()` method
- `CandidateNominationService.java` - Enhanced election validation
- `WebSecurityConfig.java` - Added public access for new endpoints

### Frontend
- `candidate-nomination.component.ts` - Added election loading and selection
- `candidate-nomination.component.html` - Added election dropdown
- `voter-registration.component.ts` - Added election loading and selection
- `voter-registration.component.html` - Added election dropdown

## Deployment Notes
1. Restart backend server after security config changes
2. Ensure database has elections with future start dates
3. Test election selection functionality
4. Verify form submissions work with selected elections
