package com.example.loginpage.ui.theme

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.loginpage.data.LocalStorage

/** Holds and persists the user-selected dark mode preference. */
object ThemePreferenceState {
    private val _isDark: MutableState<Boolean> = mutableStateOf(false)
    val isDarkState: State<Boolean> get() = _isDark
    private var initialized = false

    fun init(context: Context) {
        if (!initialized) {
            _isDark.value = LocalStorage.loadDarkModeEnabled(context)
            initialized = true
        }
    }

    fun setDark(context: Context, dark: Boolean) {
        _isDark.value = dark
        LocalStorage.saveDarkModeEnabled(context, dark)
    }

    fun toggle(context: Context) = setDark(context, !_isDark.value)

    /** Sync with system dark mode (emulator/device setting). Always overrides stored value so UI matches system. */
    fun syncFromSystem(context: Context, dark: Boolean) {
        if (_isDark.value != dark) {
            _isDark.value = dark
            LocalStorage.saveDarkModeEnabled(context, dark)
        }
    }
}
