package com.aubynsamuel.netstatinfo.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aubynsamuel.netstatinfo.data.model.AppDataUsage
import com.aubynsamuel.netstatinfo.ui.theme.MobileColor
import com.aubynsamuel.netstatinfo.ui.theme.WifiColor

@Composable
fun AppUsageRow(appDataUsage: AppDataUsage, modifier: Modifier = Modifier) {
    val totalBytes = appDataUsage.mobileDataUsageBytes + appDataUsage.wifiDataUsageBytes

    // Animate usage bar fractions on first composition
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(appDataUsage.packageName) { triggered = true }

    val mobileRatio =
        if (totalBytes > 0) appDataUsage.mobileDataUsageBytes.toFloat() / totalBytes else 0f
    val animatedMobileRatio by animateFloatAsState(
        targetValue = if (triggered) mobileRatio else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f),
        label = "mobileRatioAnim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (appDataUsage.appIcon != null) {
                    Image(
                        bitmap = appDataUsage.appIcon,
                        contentDescription = "${appDataUsage.appName} icon",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = appDataUsage.appName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // App name + package + bar
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appDataUsage.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = appDataUsage.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Dual usage bar (Mobile | WiFi)
                UsageBar(
                    mobileRatio = animatedMobileRatio,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Mobile / WiFi labels beneath bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MobileColor)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = appDataUsage.formattedMobileUsage,
                            style = MaterialTheme.typography.labelSmall,
                            color = MobileColor
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(WifiColor)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = appDataUsage.formattedWifiUsage,
                            style = MaterialTheme.typography.labelSmall,
                            color = WifiColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Total usage badge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = appDataUsage.formattedTotalUsage,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UsageBar(mobileRatio: Float, modifier: Modifier = Modifier) {
    val wifiRatio = 1f - mobileRatio
    Row(
        modifier = modifier
            .height(6.dp)
            .clip(RoundedCornerShape(50)),
    ) {
        if (mobileRatio > 0f) {
            Box(
                modifier = Modifier
                    .weight(mobileRatio.coerceAtLeast(0.01f))
                    .height(6.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(MobileColor.copy(alpha = 0.7f), MobileColor)
                        )
                    )
            )
        }
        if (wifiRatio > 0f) {
            Box(
                modifier = Modifier
                    .weight(wifiRatio.coerceAtLeast(0.01f))
                    .height(6.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(WifiColor, WifiColor.copy(alpha = 0.7f))
                        )
                    )
            )
        }
    }
}
