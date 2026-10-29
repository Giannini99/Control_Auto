package br.com.sensorauto.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/** Appearance is independent of collection settings and is saved immediately. */
class ThemePreferences(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("appearance", Context.MODE_PRIVATE)

    var followSystem by mutableStateOf(preferences.getBoolean("follow_system", true))
        private set
    var darkTheme by mutableStateOf(preferences.getBoolean("dark_theme", false))
        private set

    fun updateFollowSystem(value: Boolean) {
        preferences.edit().putBoolean("follow_system", value).apply()
        followSystem = value
    }

    fun updateDarkTheme(value: Boolean) {
        preferences.edit().putBoolean("dark_theme", value).apply()
        darkTheme = value
    }
}

val LocalThemePreferences = staticCompositionLocalOf<ThemePreferences> {
    error("ThemePreferences must be provided by MainActivity")
}

