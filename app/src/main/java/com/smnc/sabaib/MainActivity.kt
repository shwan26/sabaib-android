package com.smnc.sabaib

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.smnc.sabaib.navigation.AppNavHost
import com.smnc.sabaib.ui.theme.SabaiBTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SabaiBTheme {
                AppNavHost()
            }
        }
    }
}
