package com.aubynsamuel.netstatinfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.aubynsamuel.netstatinfo.ui.theme.NetStatInfoTheme
import com.aubynsamuel.netstatinfo.ui.theme.screens.DataUsageScreen

internal const val TAG = "NetStatInfo"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetStatInfoTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    DataUsageScreen()
                }
            }
        }
    }
}