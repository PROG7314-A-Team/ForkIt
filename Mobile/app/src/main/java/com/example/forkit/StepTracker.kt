package com.example.forkit

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime


class StepTracker(private val context: Context) {
    
    private val TAG = "StepTracker"
    
    // Step count state
    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()
    
    // Health Connect client
    private var healthConnectClient: HealthConnectClient? = null
    
    // Sensor manager for fallback
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    
    // Store initial step count for sensor-based tracking
    private var initialStepCount = 0
    private var currentSessionSteps = 0
    private var isFirstReading = true
    
    init {
        // Try to initialize Health Connect
        try {
            healthConnectClient = HealthConnectClient.getOrCreate(context)
            Log.d(TAG, "Health Connect initialized")
        } catch (e: Exception) {
            Log.d(TAG, "Health Connect not available, will use sensor fallback: ${e.message}")
            healthConnectClient = null
        }
        
        // Initialize sensor fallback
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        
        if (stepCounterSensor != null) {
            Log.d(TAG, "Step Counter sensor available")
        } else if (stepDetectorSensor != null) {
            Log.d(TAG, "Step Detector sensor available")
        } else {
            Log.w(TAG, "No step sensors available on device")
        }
    }
    

    fun isHealthConnectAvailable(): Boolean {
        return healthConnectClient != null
    }
    

    fun isSensorAvailable(): Boolean {
        return stepCounterSensor != null || stepDetectorSensor != null
    }
    

    fun getHealthConnectPermissions(): Set<String> {
        return setOf(
            HealthPermission.getReadPermission(StepsRecord::class)
        )
    }
    

    suspend fun hasHealthConnectPermissions(): Boolean {
        return try {
            val client = healthConnectClient ?: return false
            val granted = client.permissionController.getGrantedPermissions()
            granted.containsAll(getHealthConnectPermissions())
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions: ${e.message}")
            false
        }
    }
    

    suspend fun fetchTodayStepsFromHealthConnect(): Int {
        try {
            val client = healthConnectClient ?: run {
                Log.w(TAG, "Health Connect client not available")
                return 0
            }
            
            // Check permissions first
            if (!hasHealthConnectPermissions()) {
                Log.w(TAG, "Health Connect permissions not granted")
                return 0
            }
            
            // Get start and end of today
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val startOfDay = now.toLocalDate().atStartOfDay(ZoneId.systemDefault())
            val endOfDay = now
            
            // Create time range filter for today
            val timeRangeFilter = TimeRangeFilter.between(
                startOfDay.toInstant(),
                endOfDay.toInstant()
            )
            
            // Read steps records
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = timeRangeFilter
            )
            
            val response = client.readRecords(request)
            val totalSteps = response.records.sumOf { it.count }
            
            Log.d(TAG, "Fetched $totalSteps steps from Health Connect for today")
            _stepCount.value = totalSteps.toInt()
            
            return totalSteps.toInt()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching steps from Health Connect: ${e.message}", e)
            return 0
        }
    }
    

    fun startSensorTracking() {
        val sensor = stepCounterSensor
        if (sensor != null) {
            sensorManager?.registerListener(
                stepSensorListener,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
            Log.d(TAG, "Started step counter sensor tracking")
        } else {
            Log.w(TAG, "Step counter sensor not available")
        }
    }
    

    fun stopSensorTracking() {
        sensorManager?.unregisterListener(stepSensorListener)
        Log.d(TAG, "Stopped step counter sensor tracking")
    }

    private val stepSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                    val totalStepsSinceBoot = it.values[0].toInt()
                    
                    // On first reading, store as baseline
                    if (isFirstReading) {
                        initialStepCount = totalStepsSinceBoot
                        isFirstReading = false
                        Log.d(TAG, "Initial step count: $initialStepCount")
                    }
                    
                    // Calculate today's steps (this is a simple approach)
                    // Note: This will reset if app is killed, ideally store in SharedPreferences
                    currentSessionSteps = totalStepsSinceBoot - initialStepCount
                    _stepCount.value = currentSessionSteps
                    
                    Log.d(TAG, "Step count updated: $currentSessionSteps")
                }
            }
        }
        
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            Log.d(TAG, "Sensor accuracy changed: $accuracy")
        }
    }
    

    suspend fun fetchTodaySteps(): Int {
        return try {
            // Try Health Connect first
            if (isHealthConnectAvailable() && hasHealthConnectPermissions()) {
                val steps = fetchTodayStepsFromHealthConnect()
                if (steps > 0) {
                    return steps
                }
            }
            
            // Fallback to sensor data
            if (isSensorAvailable()) {
                Log.d(TAG, "Using sensor fallback: ${_stepCount.value} steps")
                return _stepCount.value
            }
            
            Log.w(TAG, "No step tracking method available")
            0
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching steps: ${e.message}", e)
            0
        }
    }
    

    fun saveTodayBaseline(baselineSteps: Int) {
        val prefs = context.getSharedPreferences("step_tracker", Context.MODE_PRIVATE)
        val today = java.time.LocalDate.now().toString()
        prefs.edit().apply {
            putInt("baseline_$today", baselineSteps)
            putString("last_update_date", today)
            apply()
        }
    }
    

    fun loadTodayBaseline(): Int {
        val prefs = context.getSharedPreferences("step_tracker", Context.MODE_PRIVATE)
        val today = java.time.LocalDate.now().toString()
        return prefs.getInt("baseline_$today", 0)
    }
    

    fun cleanup() {
        stopSensorTracking()
    }
}

