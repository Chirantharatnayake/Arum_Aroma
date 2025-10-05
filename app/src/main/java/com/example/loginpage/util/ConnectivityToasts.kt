package com.example.loginpage.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun ConnectivityToasts() {
    val ctx = LocalContext.current.applicationContext

    DisposableEffect(Unit) {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var lastOnline: Boolean? = null

        fun isCurrentlyOnline(): Boolean {
            val active = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(active) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }

        fun showOnlineOnce() {
            if (lastOnline != true) {
                lastOnline = true
                Toast.makeText(ctx, "You're online", Toast.LENGTH_SHORT).show()
            }
        }

        fun showOfflineOnce() {
            if (lastOnline != false) {
                lastOnline = false
                Toast.makeText(ctx, "You're offline", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize with current status
        if (isCurrentlyOnline()) showOnlineOnce() else showOfflineOnce()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // A network became available; verify it's actually usable
                if (isCurrentlyOnline()) showOnlineOnce()
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                if (cm.activeNetwork == network &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                ) {
                    showOnlineOnce()
                }
            }

            override fun onLost(network: Network) {
                // One network is gone; check if we still have any working network
                if (!isCurrentlyOnline()) showOfflineOnce()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(callback)
        } else {
            val req = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(req, callback)
        }

        onDispose {
            runCatching { cm.unregisterNetworkCallback(callback) }
        }
    }
}
