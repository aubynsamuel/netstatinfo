package com.aubynsamuel.netstatinfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.aubynsamuel.netstatinfo.ui.screens.DataUsageScreen
import com.aubynsamuel.netstatinfo.ui.theme.NetStatInfoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetStatInfoTheme {
                Scaffold { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        DataUsageScreen()
                    }
                }
            }
        }
    }
}