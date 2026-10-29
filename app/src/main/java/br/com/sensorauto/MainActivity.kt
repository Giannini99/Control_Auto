package br.com.sensorauto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import br.com.sensorauto.ui.theme.LocalThemePreferences
import br.com.sensorauto.ui.theme.ThemePreferences
import br.com.sensorauto.navigation.SensorAutoNavHost
import br.com.sensorauto.ui.theme.SensorAutoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appearance = remember { ThemePreferences(applicationContext) }
            val darkTheme = if (appearance.followSystem) isSystemInDarkTheme()
                            else appearance.darkTheme
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            CompositionLocalProvider(LocalThemePreferences provides appearance) {
                SensorAutoTheme(darkTheme = darkTheme) {
                    SensorAutoNavHost()
                }
            }
        }
    }
}


