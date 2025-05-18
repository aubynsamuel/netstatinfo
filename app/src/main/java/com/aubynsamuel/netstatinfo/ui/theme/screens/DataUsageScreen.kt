package com.aubynsamuel.netstatinfo.ui.theme.screens

import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aubynsamuel.netstatinfo.AppDataUsage
import com.aubynsamuel.netstatinfo.TAG
import com.aubynsamuel.netstatinfo.TimePeriod
import com.aubynsamuel.netstatinfo.getAppsDataUsage
import com.aubynsamuel.netstatinfo.hasUsageStatsPermission
import com.aubynsamuel.netstatinfo.ui.theme.components.AppUsageRow
import com.aubynsamuel.netstatinfo.ui.theme.components.TimeFilterChips
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataUsageScreen() {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    var appUsageList by remember { mutableStateOf<List<AppDataUsage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTimePeriod by remember { mutableStateOf(TimePeriod.LAST_6_HOURS) }
    var settingsErrorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        hasPermission = hasUsageStatsPermission(context)
    }

    val fetchData: (TimePeriod) -> Unit = { timePeriod ->
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            settingsErrorMessage = null // Clear settings error on new fetch

            try {
                val endTime = System.currentTimeMillis()
                val startTime = timePeriod.getStartTimeMillis()

                val result = withContext(Dispatchers.IO) {
                    getAppsDataUsage(context, startTime, endTime)
                }

                appUsageList = result
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching data", e)
                errorMessage = "Error fetching data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(hasPermission, selectedTimePeriod) {
        if (hasPermission) {
            fetchData(selectedTimePeriod)
        } else {
            // Clear list if permission is revoked
            appUsageList = emptyList()
        }
    }

    if (!hasPermission) {
        PermissionRequestUI(
            errorMessage = settingsErrorMessage,
            onRequestPermission = {
                try {
                    settingsErrorMessage = null
                    context.startActivity(
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS) // add package name)
                            .apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            })
                } catch (e: Exception) {
                    Log.e(TAG, "Could not open settings", e)
                    settingsErrorMessage =
                        "Could not open settings. Please grant permission manually via Android Settings > Apps > Special app access > Usage access."
                }
            }
        )
    } else {
        Column(modifier = Modifier.padding(vertical = 30.dp, horizontal = 10.dp).fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Data Usage",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable(onClick = { fetchData(selectedTimePeriod) })
                )
            }
            Text(text = " ${selectedTimePeriod.displayName}")

            TimeFilterChips(
                selectedPeriod = selectedTimePeriod,
                onPeriodSelected = { period ->
                    selectedTimePeriod = period
                    fetchData(period)
                }
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
            } else if (appUsageList.isEmpty()) {
                Text("No data usage information available for the selected period or no apps found. Ensure the app has 'Usage access' permission and that apps have used data in this period.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(appUsageList.sortedByDescending { it.mobileDataUsageBytes + it.wifiDataUsageBytes }) { usageInfo ->
                        AppUsageRow(usageInfo)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}