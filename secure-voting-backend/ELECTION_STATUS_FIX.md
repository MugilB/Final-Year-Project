# Election Status Update Fix

## Problem
When updating election times (start date/end date), the election status was not being automatically recalculated based on the new dates. The status remained the same as before the update, causing incorrect status display in the frontend.

## Root Cause
The `updateElection` method in `ElectionService` was setting the status from the request (`request.getStatus()`) instead of automatically determining the correct status based on the current time and the new start/end dates.

## Solution Applied

### 1. Fixed Election Update Logic
Modified `ElectionService.updateElection()` to automatically determine status based on current time and new dates:

```java
// Automatically determine status based on current time and new dates
long currentTime = System.currentTimeMillis();
String newStatus;

if (currentTime < election.getStartDate()) {
    newStatus = "SCHEDULED";
} else if (currentTime >= election.getStartDate() && currentTime <= election.getEndDate()) {
    newStatus = "OPENED";
} else {
    newStatus = "CLOSED";
}

election.setStatus(newStatus);
```

### 2. Added Manual Status Update Methods
- `updateElectionStatus(int electionId)`: Update status for a specific election
- `updateElectionStatuses()`: Update status for all elections
- New controller endpoint: `POST /api/elections/{electionId}/update-status`

### 3. Status Logic
- **SCHEDULED**: Current time < start date
- **OPENED**: Current time >= start date AND <= end date  
- **CLOSED**: Current time > end date

## Benefits
✅ **Automatic Status Updates**: Status is recalculated whenever election times are updated
✅ **Real-time Accuracy**: Status reflects the actual current state based on dates
✅ **Manual Override**: Admins can manually trigger status updates if needed
✅ **Consistent Logic**: Same status determination logic used everywhere

## Testing
1. Update an election's start/end dates
2. Check that the status automatically updates to reflect the new dates
3. Verify the frontend displays the correct status
4. Test manual status update endpoint if needed

## API Endpoints
- `PUT /api/elections/{electionId}`: Update election (now includes automatic status update)
- `POST /api/elections/{electionId}/update-status`: Manually update specific election status
- `POST /api/elections/update-statuses`: Manually update all election statuses

The fix ensures that election statuses are always accurate and reflect the current time relative to the election's start and end dates.

