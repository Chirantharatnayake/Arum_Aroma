package com.example.loginpage.API

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Global battery alert state used by the in-app banner
object BatteryState {
    var isEnabled by mutableStateOf(false)
    var lastTier: Int? = null // 2 = <20, 1 = <50, 0 = >=50
    var message by mutableStateOf<String?>(null)
}

/**
 * Global Battery API that updates BatteryState based on ACTION_BATTERY_CHANGED.
 * No system notifications are used; UI shows messages via GlobalBatteryAlert()
 */
object BatteryApi {
    @Volatile private var isRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Intent.ACTION_BATTERY_CHANGED != intent.action) return
            val level = intent.getIntExtra("level", -1)
            val scale = intent.getIntExtra("scale", -1)
            if (level <= 0 || scale <= 0) return
            val percent = (level * 100) / scale
            updateTier(percent)
        }
    }

    private fun updateTier(percent: Int) {
        // Determine tier and decide whether to show a new message
        val newTier = when {
            percent < 20 -> 2
            percent < 50 -> 1
            else -> 0
        }
        val oldTier = BatteryState.lastTier
        BatteryState.lastTier = newTier

        if (!BatteryState.isEnabled) return

        // Only show when crossing into a lower tier (e.g., from 0->1 or 1->2)
        if (newTier > (oldTier ?: 0)) {
            BatteryState.message = when (newTier) {
                2 -> "Battery low: below 20%"
                1 -> "Battery warning: below 50%"
                else -> null
            }
        }
        // When recovering to 0, we don't show a message; banner will auto-hide.
    }

    fun start(context: Context) {
        if (isRegistered) return
        val app = context.applicationContext
        BatteryState.isEnabled = true
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val sticky = app.registerReceiver(receiver, filter)
        isRegistered = true
        // Evaluate immediately from sticky intent
        sticky?.let {
            val level = it.getIntExtra("level", -1)
            val scale = it.getIntExtra("scale", -1)
            if (level > 0 && scale > 0) {
                val percent = (level * 100) / scale
                updateTier(percent)
            }
        }
    }

    fun stop(context: Context) {
        if (!isRegistered) return
        runCatching { context.applicationContext.unregisterReceiver(receiver) }
        isRegistered = false
        BatteryState.isEnabled = false
        BatteryState.message = null
        BatteryState.lastTier = null
    }
}

@Composable
fun GlobalBatteryAlert(
    modifier: Modifier = Modifier,
    autoHideMillis: Long = 5_000L
) {
    val ctx = LocalContext.current
    var visibleMessage by remember { mutableStateOf<String?>(null) }

    // Reflect message from BatteryState when enabled
    LaunchedEffect(BatteryState.isEnabled) {
        if (!BatteryState.isEnabled) {
            visibleMessage = null
        }
    }

    LaunchedEffect(BatteryState.message) {
        visibleMessage = BatteryState.message
    }

    // Auto-hide after delay
    LaunchedEffect(visibleMessage) {
        if (visibleMessage != null) {
            delay(autoHideMillis)
            // Only clear if no new message arrived
            if (visibleMessage == BatteryState.message) {
                visibleMessage = null
                BatteryState.message = null
            }
        }
    }

    AnimatedVisibility(visible = visibleMessage != null, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D47A1), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = visibleMessage ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
