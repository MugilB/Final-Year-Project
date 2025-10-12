# Testing cURL Functionality

## Issue
The cURL request for creating tickets in the external system (Desk365.io) is not being triggered during voter registration.

## Debugging Steps

### 1. Test the External API Directly
Use the test cURL command to verify the external API is working:

```bash
curl -X 'POST' \
  'https://dev007test.desk365.io/apis/v3/tickets/create' \
  -H 'accept: application/json' \
  -H 'Authorization: 575a2602b282a056af8f7c4af4be8cdf9d6a4c91a759f4209ff3b187d5659703' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "test@example.com",
    "subject": "Test Voter Registration Request - John Doe",
    "description": "Test Voter Registration Request - Please review and approve the voter registration application for Voter ID: VOTER_TEST_123",
    "status": "open",
    "priority": 1,
    "type": "Request",
    "group": "Voter Registration",
    "category": "New Registration",
    "sub_category": "Voter Application",
    "custom_fields": {
      "cf_First Name": "John",
      "cf_Last Name": "Doe",
      "cf_Email": "test@example.com",
      "cf_Phone Number": "+1-555-123-4567",
      "cf_Date Of Birth": "1990-05-15",
      "cf_Gender": "Male",
      "cf_Address": "123 Main Street, City, State 12345",
      "cf_Ward": 1,
      "cf_Blood Group": "O+",
      "cf_Proof": "https://drive.google.com/file/d/example-aadhar-link",
      "cf_Profile Picture": "https://drive.google.com/file/d/example-profile-link",
      "cf_Username": "johndoe123",
      "cf_Action": "New Voter Approval"
    },
    "watchers": [],
    "share_to": []
  }'
```

### 2. Test the Backend cURL Functionality
Use the test endpoint to verify the backend cURL implementation:

```bash
curl -X POST http://localhost:8081/api/voters/test-curl
```

### 3. Check Backend Logs
Look for these debug messages in the backend console:
- "Starting ticket creation for voter: [VOTER_ID]"
- "API URL: https://dev007test.desk365.io/apis/v3/tickets/create"
- "Headers prepared"
- "HTTP entity created with ticket data"
- "Making API call to external service..."
- "API call completed. Response status: [STATUS]"
- "Ticket created successfully: [RESPONSE]"

### 4. Check Voter Registration Flow
When testing voter registration, look for:
- "Attempting to create ticket for voter: [VOTER_ID]"
- "Ticket creation completed successfully for voter: [VOTER_ID]"
- OR "Failed to create ticket for voter [VOTER_ID]: [ERROR]"

## Possible Issues

### 1. Voter Registration Failing Before cURL
- **Cause**: Foreign key constraint violation (ward_id issue)
- **Solution**: Run `create_test_wards.sql` to populate ward data

### 2. RestTemplate Not Configured
- **Cause**: RestTemplate bean not available
- **Solution**: Check `RestTemplateConfig.java` is loaded

### 3. External API Issues
- **Cause**: Desk365.io API is down or credentials are invalid
- **Solution**: Test external API directly with cURL

### 4. Network/Firewall Issues
- **Cause**: Backend cannot reach external API
- **Solution**: Check network connectivity

### 5. JSON Serialization Issues
- **Cause**: Data format not matching external API expectations
- **Solution**: Check the JSON structure matches API requirements

## Files Created for Testing

1. **test_curl_request.json** - Test data for external API
2. **test_curl_command.txt** - Direct cURL command for testing
3. **Test endpoint** - `/api/voters/test-curl` for backend testing
4. **Enhanced logging** - Debug messages throughout the flow

## Next Steps

1. **Run the ward data script** to fix foreign key constraint
2. **Test the external API** directly with cURL
3. **Test the backend endpoint** with `/api/voters/test-curl`
4. **Test voter registration** and check logs for cURL execution
5. **Verify ticket creation** in the external system




