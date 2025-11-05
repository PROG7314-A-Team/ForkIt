package com.example.forkit.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

class SyncManager(private val context: Context) {
    
    private val TAG = "SyncManager"
    private val workManager = WorkManager.getInstance(context)
    
    companion object {
        private const val SYNC_WORK_NAME = "forkit_data_sync"
        private const val PERIODIC_SYNC_WORK_NAME = "forkit_periodic_sync"
    }
    
    /**
     * Schedule a one-time sync when network is available
     */
    fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10000L, // 10 seconds
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
        
        Log.d(TAG, "Sync work scheduled")
    }
    
    /**
     * Schedule periodic sync (every 15 minutes when connected)
     */
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val periodicSyncRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10000L, // 10 seconds
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicSyncRequest
        )
        
        Log.d(TAG, "Periodic sync scheduled (every 15 minutes)")
    }
    
    /**
     * Trigger immediate sync
     */
    fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
            .setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        
        workManager.enqueue(syncRequest)
        
        Log.d(TAG, "Immediate sync triggered")
    }
    
    /**
     * Cancel all sync work
     */
    fun cancelSync() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
        workManager.cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
        Log.d(TAG, "All sync work cancelled")
    }
    
    /**
     * Check if sync is in progress
     */
    fun isSyncing(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(SYNC_WORK_NAME).get()
        return workInfos.any { it.state == WorkInfo.State.RUNNING }
    }
}

