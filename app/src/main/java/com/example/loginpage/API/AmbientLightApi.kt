package com.example.loginpage.API

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay

// Simple global ambient light state used by ProfileScreen
object AmbientLightState {
    var isEnabled by mutableStateOf(false)
    var currentLux by mutableStateOf<Float?>(null)
    var lastZoneShown: Int? = null
}

@Composable
fun GlobalAmbientAlert(
    modifier: Modifier = Modifier,
    autoHideMillis: Long = 5_000L // Auto-hide banner after a set time
) {
    val context = LocalContext.current
    var message by remember { mutableStateOf<String?>(null) }
    var currentZone by remember { mutableStateOf<Int?>(null) }

    // Initialize ambient light sensor subscription on composable entry
    DisposableEffect(AmbientLightState.isEnabled) {
        // If ambient light is enabled, start listening to the sensor
        if (AmbientLightState.isEnabled) {
            val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val sensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT)
            if (sensor == null) {
                message = "No ambient light sensor on this device."
                return@DisposableEffect onDispose { }
            }

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val lux = event.values.getOrNull(0) ?: return
                    AmbientLightState.currentLux = lux
                    val zone = when {
                        lux <= 20f -> 1
                        lux <= 100f -> 2
                        lux <= 1000f -> 3
                        else -> 4
                    }

                    if (zone != currentZone) {
                        currentZone = zone
                        AmbientLightState.lastZoneShown = zone
                        message = when (zone) {
                            1 -> "Low light detected. Dark mode is recommended."
                            2 -> "Dim light: lower brightness for comfort."
                            3 -> "Bright light detected. Increase screen brightness."
                            4 -> "Very bright light! Increase screen brightness."
                            else -> null
                        }
                        Log.d("AmbientLight", "Zone=$zone lux=$lux")
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            onDispose { sm.unregisterListener(listener) }
        }

        // Handle ambient light state cleanup on disposal
        if (!AmbientLightState.isEnabled) {
            AmbientLightState.currentLux = null
            message = null
        }

        onDispose {}
    }

    // Auto-hide banner after `autoHideMillis` milliseconds
    LaunchedEffect(message) {
        if (message != null) {
            delay(autoHideMillis)
            message = null
        }
    }

    // Display the message when it's not null
    AnimatedVisibility(visible = message != null, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF212121), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = message ?: "",
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

