package com.kzeta.presionmtb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kzeta.presionmtb.ui.AppNav
import com.kzeta.presionmtb.ui.theme.PresionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as PresionApp
        setContent {
            PresionTheme {
                AppNav(app)
            }
        }
    }
}
