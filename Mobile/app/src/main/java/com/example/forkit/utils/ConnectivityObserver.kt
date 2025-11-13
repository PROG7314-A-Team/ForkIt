package com.example.forkit.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Application-wide connectivity observer that exposes the current online/offline
 * state as a hot [StateFlow]. Must be initialised with an application context
 * before use.
 */
object ConnectivityObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val initialized = AtomicBoolean(false)
    private val _isOnline = MutableStateFlow(false)

    /**
     * Call once with an application context to start observing connectivity.
     */
    fun initialize(context: Context) {
        if (initialized.compareAndSet(false, true)) {
            val appContext = context.applicationContext
            val networkManager = NetworkConnectivityManager(appContext)
            _isOnline.value = networkManager.isOnline()
            scope.launch {
                networkManager.observeConnectivity().collect { status ->
                    _isOnline.value = status
                }
            }
        }
    }

    /**
     * A hot [StateFlow] reporting whether the device currently has validated internet access.
     */
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
}


