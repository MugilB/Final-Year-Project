# Candidate Image Upload Feature

## Overview
Added candidate image upload functionality to the candidate nomination form, allowing candidates to upload their professional photos via Google Drive links.

## Features Implemented

### 1. Database Schema
- **New Column**: `candidate_image_link` in `candidate_details` table
- **Type**: `varchar(500) DEFAULT NULL`
- **Position**: After `aadhar_card_link` column
- **Optional**: Not required (unlike Aadhar Card)

### 2. Backend Changes

#### Database Migration
```sql
ALTER TABLE `candidate_details`
ADD COLUMN `candidate_image_link` varchar(500) DEFAULT NULL AFTER `aadhar_card_link`;
```

#### Model Updates
**CandidateDetails.java**:
```java
@Column(name = "candidate_image_link")
private String candidateImageLink;

// Getters and setters
public String getCandidateImageLink() {
    return candidateImageLink;
}

public void setCandidateImageLink(String candidateImageLink) {
    this.candidateImageLink = candidateImageLink;
}
```

#### DTO Updates
**CandidateNominationRequest.java**:
```java
@Pattern(regexp = "^https://drive\\.google\\.com/.*$", message = "Candidate image link must be a valid Google Drive link")
private String candidateImageLink;
```

#### Service Updates
**CandidateNominationService.java**:
```java
candidateDetails.setCandidateImageLink(request.getCandidateImageLink());
```

### 3. Frontend Changes

#### Form Field
- **Label**: "Upload Candidate Image (Google Drive Link)"
- **Type**: URL input with Google Drive validation
- **Required**: No (Optional field)
- **Validation**: Must be a valid Google Drive link
- **Placeholder**: "https://drive.google.com/file/d/..."

#### Form Validation
```typescript
candidateImageLink: ['', [Validators.pattern(/^https:\/\/drive\.google\.com\/.*$/)]]
```

#### HTML Template
```html
<div class="form-group">
  <label for="candidateImageLink">Upload Candidate Image (Google Drive Link)</label>
  <input
    type="url"
    id="candidateImageLink"
    formControlName="candidateImageLink"
    class="form-control"
    [class.error]="getFieldError('candidateImageLink')"
    placeholder="https://drive.google.com/file/d/..."
  />
  <div class="field-help">
    <i class="fas fa-info-circle"></i>
    Please upload your professional photo to Google Drive and share the public link here (Optional)
  </div>
  <div class="error-message" *ngIf="getFieldError('candidateImageLink')">
    {{ getFieldError('candidateImageLink') }}
  </div>
</div>
```

## Database Schema

### Before
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
  -- other fields...
);
```

### After
```sql
CREATE TABLE `candidate_details` (
  `candidate_id` int NOT NULL,
  `email` varchar(255) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `gender` varchar(20) NOT NULL,
  `age` int NOT NULL,
  `address` text NOT NULL,
  `aadhar_card_link` varchar(500) NOT NULL,
  `candidate_image_link` varchar(500) DEFAULT NULL,  -- NEW FIELD
  `biography` text,
  `manifesto_summary` text,
  -- other fields...
);
```

## API Request/Response

### Request Body
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
  "aadharCardLink": "https://drive.google.com/file/d/abc123/view",
  "candidateImageLink": "https://drive.google.com/file/d/xyz789/view",  // NEW FIELD
  "electionId": 1
}
```

### Database Storage
```sql
INSERT INTO candidate_details (
  candidate_id, email, phone_number, gender, age, address, 
  aadhar_card_link, candidate_image_link, biography, manifesto_summary
) VALUES (
  1, 'john@example.com', '1234567890', 'Male', 30, '123 Main St, City',
  'https://drive.google.com/file/d/abc123/view',
  'https://drive.google.com/file/d/xyz789/view',  -- NEW FIELD
  'Biography not provided', 'Manifesto not provided'
);
```

## Validation Rules

### Frontend Validation
- **Pattern**: Must match `^https://drive\.google\.com/.*$`
- **Required**: No (Optional field)
- **Error Message**: "Candidate image link must be a valid Google Drive link"

### Backend Validation
- **Pattern**: Same as frontend
- **Nullable**: Yes (can be null)
- **Length**: Maximum 500 characters

## User Experience

### Form Layout
1. **Election Selection** (Required)
2. **Candidate Name** (Required)
3. **Gender** (Required)
4. **Age** (Required)
5. **Email** (Required)
6. **Phone Number** (Required)
7. **Address** (Required)
8. **Political Party** (Required)
9. **Party Secret Code** (Required)
10. **Aadhar Card Link** (Required)
11. **Candidate Image Link** (Optional) ← NEW FIELD
12. **Submit Button**

### Help Text
- **Label**: "Upload Candidate Image (Google Drive Link)"
- **Help Text**: "Please upload your professional photo to Google Drive and share the public link here (Optional)"
- **Placeholder**: "https://drive.google.com/file/d/..."

## Migration Steps

### 1. Run Database Migration
```sql
-- Copy and paste the contents of run_candidate_image_migration.sql
USE secure_voting;

ALTER TABLE `candidate_details`
ADD COLUMN `candidate_image_link` varchar(500) DEFAULT NULL AFTER `aadhar_card_link`;
```

### 2. Restart Backend Server
```bash
cd secure-voting-backend
mvn spring-boot:run
```

### 3. Test the Feature
1. Open candidate nomination form
2. Fill out all required fields
3. Optionally add candidate image link
4. Submit the form
5. Verify data is stored in database

## Verification

### Check Database Schema
```sql
DESCRIBE candidate_details;
```

Expected output should include:
```
+----------------------+--------------+------+-----+---------+-------+
| Field                | Type         | Null | Key | Default | Extra |
+----------------------+--------------+------+-----+---------+-------+
| candidate_image_link | varchar(500) | YES  |     | NULL    |       |
+----------------------+--------------+------+-----+---------+-------+
```

### Check Sample Data
```sql
SELECT 
    candidate_id,
    email,
    aadhar_card_link,
    candidate_image_link
FROM candidate_details 
LIMIT 3;
```

## Files Modified

### Backend
- `database_migrations/003_add_candidate_image.sql` - Database migration
- `model/CandidateDetails.java` - Added candidateImageLink field
- `dto/CandidateNominationRequest.java` - Added candidateImageLink field
- `service/CandidateNominationService.java` - Handle image field in service

### Frontend
- `candidate-nomination.component.ts` - Added form field and validation
- `candidate-nomination.component.html` - Added HTML input field

## Future Enhancements
- Image preview functionality
- Image size validation
- Multiple image uploads
- Image compression
- CDN integration for better performance
- Image metadata storage (dimensions, file size, etc.)

## Notes
- Candidate image is optional (unlike Aadhar Card which is required)
- Uses same Google Drive link validation as Aadhar Card
- Stored as varchar(500) to accommodate long Google Drive URLs
- Can be null in database (optional field)
- Frontend validation matches backend validation pattern
