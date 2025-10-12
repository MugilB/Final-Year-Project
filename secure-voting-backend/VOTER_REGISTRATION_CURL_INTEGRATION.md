# Voter Registration cURL Integration

## Overview
When a user fills out the voter registration form and clicks "Register as Voter", the system automatically creates a ticket in the external ticketing system (Desk365.io) with all the form data.

## Dynamic Data Flow

### 1. Form Submission
- User fills out the voter registration form
- Clicks "Register as Voter" button
- Form data is sent to `/api/voters/register` endpoint

### 2. Backend Processing
- Creates user account in database with `approvalStatus = 2` (pending)
- Creates user details record
- **Automatically triggers cURL request** to create ticket

### 3. cURL Request Structure
The cURL request is built dynamically using the form data:

```json
{
  "email": "{{DYNAMIC_FROM_FORM}}",
  "subject": "New Voter Registration Request - {{DYNAMIC_FIRST_NAME}} {{DYNAMIC_LAST_NAME}}",
  "description": "New Voter Registration Request - Please review and approve the voter registration application for Voter ID: {{DYNAMIC_VOTER_ID}}",
  "status": "open",
  "priority": 1,
  "type": "Request",
  "group": "Voter Registration",
  "category": "New Registration",
  "sub_category": "Voter Application",
  "custom_fields": {
    "cf_First Name": "{{DYNAMIC_FROM_FORM}}",
    "cf_Last Name": "{{DYNAMIC_FROM_FORM}}",
    "cf_Email": "{{DYNAMIC_FROM_FORM}}",
    "cf_Phone Number": "{{DYNAMIC_FROM_FORM}}",
    "cf_Date Of Birth": "{{DYNAMIC_FROM_FORM}}",
    "cf_Gender": "{{DYNAMIC_FROM_FORM}}",
    "cf_Address": "{{DYNAMIC_FROM_FORM}}",
    "cf_Ward": "{{DYNAMIC_FROM_FORM}}",
    "cf_Blood Group": "{{DYNAMIC_FROM_FORM}}",
    "cf_Proof": "{{DYNAMIC_FROM_FORM}}",
    "cf_Profile Picture": "{{DYNAMIC_FROM_FORM}}",
    "cf_Username": "{{DYNAMIC_FROM_FORM}}",
    "cf_Action": "New Voter Approval"
  }
}
```

## Field Mapping

| Form Field | Custom Field | Source |
|------------|--------------|---------|
| Email | cf_Email | Dynamic from form |
| First Name | cf_First Name | Dynamic from form |
| Last Name | cf_Last Name | Dynamic from form |
| Phone Number | cf_Phone Number | Dynamic from form |
| Date of Birth | cf_Date Of Birth | Dynamic from form |
| Gender | cf_Gender | Dynamic from form |
| Address | cf_Address | Dynamic from form |
| Ward ID | cf_Ward | Dynamic from form |
| Blood Group | cf_Blood Group | Dynamic from form |
| Aadhar Card Link | cf_Proof | Dynamic from form |
| Profile Picture Link | cf_Profile Picture | Dynamic from form |
| Username | cf_Username | Dynamic from form |
| **Action Status** | **cf_Action** | **Static: "New Voter Approval"** |

## Static vs Dynamic Fields

### Static Fields (Always the same)
- `cf_Action`: Always set to "New Voter Approval" for new registrations
- `status`: Always "open"
- `priority`: Always 1
- `type`: Always "Request"
- `group`: Always "Voter Registration"
- `category`: Always "New Registration"
- `sub_category`: Always "Voter Application"

### Dynamic Fields (From form data)
- All `cf_*` fields except `cf_Action`
- `email`: User's email from form
- `subject`: Generated using first name and last name
- `description`: Generated using voter ID

## Implementation Details

### Backend Code Location
- **Controller**: `VoterRegistrationController.java`
- **Method**: `createVoterRegistrationTicket()`
- **Trigger**: Called automatically after successful user registration

### Error Handling
- If ticket creation fails, voter registration still succeeds
- Error is logged but doesn't block the registration process
- User gets success message regardless of ticket creation status

### API Endpoint
- **URL**: `https://dev007test.desk365.io/apis/v3/tickets/create`
- **Method**: POST
- **Authentication**: Bearer token in Authorization header

## Example Flow

1. **User submits form** with:
   - First Name: "John"
   - Last Name: "Doe"
   - Email: "john.doe@example.com"
   - Phone: "+1-555-123-4567"
   - etc.

2. **Backend creates ticket** with:
   - Subject: "New Voter Registration Request - John Doe"
   - cf_First Name: "John"
   - cf_Last Name: "Doe"
   - cf_Email: "john.doe@example.com"
   - cf_Phone Number: "+1-555-123-4567"
   - cf_Action: "New Voter Approval" (static)

3. **Admin receives ticket** for review and approval

## Benefits
- **Automated workflow**: No manual ticket creation needed
- **Complete data capture**: All form fields are preserved in the ticket
- **Consistent process**: Every registration gets a ticket
- **Audit trail**: Full history of registration requests
- **Admin efficiency**: Centralized approval process
