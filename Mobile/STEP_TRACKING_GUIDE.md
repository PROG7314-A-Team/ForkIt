# Step Tracking Implementation Guide

## Overview
Step tracking has been implemented in your ForkIt app using native Android capabilities. The implementation supports two methods:

1. **Health Connect** (Primary) - Modern Android health data platform (Android 14+)
2. **Step Counter Sensor** (Fallback) - Direct hardware sensor access for older devices

## How It Works

### StepTracker.kt
A new `StepTracker` class manages all step tracking functionality:
- Automatically detects available tracking methods
- Handles Health Connect API for newer devices
- Falls back to device sensors for older devices
- Manages permissions and data collection

### Integration Points

#### 1. Permissions Added
In `AndroidManifest.xml`:
- `ACTIVITY_RECOGNITION` - Required for step sensor access (Android 10+)
- `health.READ_STEPS` - Required for Health Connect

#### 2. Dependencies Added
In `build.gradle.kts`:
- `androidx.health.connect:connect-client:1.1.0-alpha07` - Health Connect client library

#### 3. Dashboard Integration
The Dashboard now:
- Displays real-time step count
- Shows progress toward daily step goal
- Automatically requests necessary permissions
- Updates steps when pulling to refresh

## User Experience Flow

### First Time Setup
1. User opens the Dashboard
2. App detects available step tracking method
3. Permission dialog appears (Health Connect or Activity Recognition)
4. User grants permission
5. Steps are automatically fetched and displayed

### Ongoing Usage
- Steps update automatically while the app is open (sensor mode)
- Pull-to-refresh updates step count from Health Connect
- Steps persist across app sessions when using Health Connect
- Progress bar shows percentage of daily goal achieved

## Device Compatibility

### Modern Devices (Android 13+)
- Uses Health Connect
- Reads steps from Google Fit, Samsung Health, or other connected apps
- Most accurate, as it aggregates from multiple sources
- Data persists even if app is closed
- **Note**: This app requires minSdk 33 (Android 13+)

### Devices With Step Sensors
- Uses Step Counter hardware sensor
- Counts steps while app is running
- May reset if app is force-closed
- Works as fallback if Health Connect is unavailable

### Devices Without Sensors
- Shows "Not available" in the Steps card
- No error messages, graceful degradation

## Testing

### To Test Health Connect:
1. Ensure you have Android 14+ device/emulator
2. Install Google Fit or Samsung Health
3. Enable step tracking in that app
4. Grant Health Connect permissions in ForkIt
5. Walk around and verify steps appear

### To Test Sensor Fallback:
1. Use Android 10-13 device/emulator
2. Grant Activity Recognition permission
3. Walk or shake device to generate steps
4. Verify steps increment in real-time

## Data Flow

```
User walks → Device records steps → Health Connect OR Sensor
                                              ↓
                           StepTracker fetches data
                                              ↓
                           Dashboard displays count
                                              ↓
                      Progress bar updates automatically
```

## Key Features

✅ **No API Required** - All data comes from device sensors/health app
✅ **Automatic Fallback** - Works on devices without Health Connect
✅ **Permission Handling** - Automatically requests needed permissions
✅ **Real-time Updates** - Steps update as you walk (sensor mode)
✅ **Goal Tracking** - Shows progress toward daily step goal
✅ **Privacy First** - All data stays on device, never sent to server

## Potential Improvements

For future enhancements, consider:
1. **Local Storage** - Save sensor steps to SharedPreferences to persist across sessions
2. **Background Service** - Track steps even when app is closed
3. **Step History** - Store and display step trends over time
4. **Notifications** - Remind users to reach their step goals
5. **Rewards** - Celebrate when users hit step milestones

## Troubleshooting

### Steps showing 0:
- Check if device has step sensors
- Verify permissions are granted
- Try pulling to refresh
- Check if Health Connect is installed (Android 14+)

### Permission Denied:
- Go to Settings → Apps → ForkIt → Permissions
- Enable "Physical activity" or "Activity recognition"
- Restart the app

### Steps not updating:
- Pull to refresh on the Dashboard
- Check if Health Connect app is running
- Verify Google Fit or Samsung Health is tracking steps

## Code Structure

```
StepTracker.kt
├── Health Connect Integration
│   ├── isHealthConnectAvailable()
│   ├── hasHealthConnectPermissions()
│   ├── fetchTodayStepsFromHealthConnect()
│   └── getHealthConnectPermissions()
├── Sensor Integration
│   ├── isSensorAvailable()
│   ├── startSensorTracking()
│   ├── stopSensorTracking()
│   └── stepSensorListener
└── Unified Interface
    ├── fetchTodaySteps() - Smart method that picks best source
    └── stepCount StateFlow - Observable step count
```

## Notes

- Health Connect is installed separately on Android 14+ devices
- Some emulators don't support step sensors - test on real device
- Step data is only read, never written by this app
- Users control which apps can access Health Connect data

