package com.example.spinnshot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.spinnshot.ui.navigation.SpinnShotNavHost
import com.example.spinnshot.ui.theme.SpinnShotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpinnShotTheme {
                SpinnShotNavHost()
            }
        }
    }
}
