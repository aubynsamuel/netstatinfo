package com.aubynsamuel.netstatinfo.ui.screens

import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aubynsamuel.netstatinfo.data.model.AppDataUsage
import com.aubynsamuel.netstatinfo.data.model.TimePeriod
import com.aubynsamuel.netstatinfo.ui.components.AppUsageRow
import com.aubynsamuel.netstatinfo.ui.components.TimeFilterChips
import com.aubynsamuel.netstatinfo.ui.theme.MobileColor
import com.aubynsamuel.netstatinfo.ui.theme.WifiColor
import com.aubynsamuel.netstatinfo.ui.viewmodel.DataUsageUiState
import com.aubynsamuel.netstatinfo.ui.viewmodel.DataUsageViewModel

private const val TAG = "DataUsageScreen"

@Composable
fun DataUsageScreen() {
    val context = LocalContext.current

    val viewModel: DataUsageViewModel = viewModel(
        factory = DataUsageViewModel.provideFactory(context)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTimePeriod by viewModel.selectedTimePeriod.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissionAndFetch()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DataUsageScreen(
        uiState = uiState,
        selectedTimePeriod = selectedTimePeriod,
        onSelectTimePeriod = viewModel::selectTimePeriod,
        onFetchData = viewModel::fetchData
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DataUsageScreen(
    uiState: DataUsageUiState,
    selectedTimePeriod: TimePeriod,
    onSelectTimePeriod: (TimePeriod) -> Unit,
    onFetchData: () -> Unit
) {
    val context = LocalContext.current

    var settingsErrorMessage by remember { mutableStateOf<String?>(null) }

    AnimatedContent(
        targetState = uiState is DataUsageUiState.PermissionRequired,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
        label = "permissionTransition"
    ) { needsPermission ->
        if (needsPermission) {
            PermissionRequestUI(
                errorMessage = settingsErrorMessage,
                onRequestPermission = {
                    settingsErrorMessage = null
                    try {
                        context.startActivity(
                            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Could not open settings", e)
                        settingsErrorMessage =
                            "Could not open settings. Please go to Android Settings > Apps > Special app access > Usage access."
                    }
                }
            )
        } else {
            DataUsageContent(
                uiState = uiState,
                selectedTimePeriod = selectedTimePeriod.displayName,
                onPeriodSelected = { onSelectTimePeriod(it) },
                onRefresh = { onFetchData() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DataUsageContent(
    uiState: DataUsageUiState,
    selectedTimePeriod: String,
    onPeriodSelected: (TimePeriod) -> Unit,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 48.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Data Usage",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = selectedTimePeriod,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onRefresh,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Time filter chips
        TimeFilterChips(
            selectedPeriod = TimePeriod.entries
                .first { it.displayName == selectedTimePeriod },
            onPeriodSelected = onPeriodSelected,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Body: animate between loading, error, empty, and success
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
            label = "contentTransition"
        ) { state ->
            when (state) {
                is DataUsageUiState.Loading -> ShimmerList()
                is DataUsageUiState.Error -> ErrorState(message = state.message)
                is DataUsageUiState.Success -> {
                    if (state.apps.isEmpty()) {
                        EmptyState()
                    } else {
                        SuccessContent(apps = state.apps)
                    }
                }

                DataUsageUiState.PermissionRequired -> Unit // handled by parent
            }
        }
    }
}

@Composable
private fun SuccessContent(apps: List<AppDataUsage>) {
    val totalMobile = apps.sumOf { it.mobileDataUsageBytes }
    val totalWifi = apps.sumOf { it.wifiDataUsageBytes }
    val totalAll = totalMobile + totalWifi

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Summary Card
        item {
            SummaryCard(
                totalMobile = totalMobile,
                totalWifi = totalWifi,
                totalAll = totalAll,
                appCount = apps.size,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(apps, key = { it.packageName }) { app ->
            AppUsageRow(appDataUsage = app)
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun SummaryCard(
    totalMobile: Long,
    totalWifi: Long,
    totalAll: Long,
    appCount: Int,
    modifier: Modifier = Modifier,
) {
    fun formatBytes(bytes: Long): String {
        return when {
            bytes == 0L -> "0 B"
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            bytes < 1024L * 1024 * 1024 -> "%.2f MB".format(bytes / (1024.0 * 1024))
            else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Network Usage",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "$appCount apps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = formatBytes(totalAll),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatChip(
                    label = "Mobile",
                    value = formatBytes(totalMobile),
                    color = MobileColor,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "WiFi",
                    value = formatBytes(totalWifi),
                    color = WifiColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun ShimmerList() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Restart),
        label = "shimmerX"
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ),
        start = Offset(shimmerX, 0f),
        end = Offset(shimmerX + 600f, 400f)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        repeat(7) {
            ShimmerCard(brush = shimmerBrush)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ShimmerCard(brush: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(brush)
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("⚠️", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("📭", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "No data usage found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Try selecting a wider time range or wait for apps to use data.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
