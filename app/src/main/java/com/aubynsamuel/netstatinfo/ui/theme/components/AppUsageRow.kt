package com.aubynsamuel.netstatinfo.ui.theme.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.aubynsamuel.netstatinfo.AppDataUsage
import com.aubynsamuel.netstatinfo.formatDataUsage

@Composable
fun AppUsageRow(appDataUsage: AppDataUsage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Safely handle app icon
        if (appDataUsage.appIcon != null) {
            Image(
                bitmap = appDataUsage.appIcon.toBitmap(width = 48, height = 48).asImageBitmap(),
                contentDescription = "${appDataUsage.appName} icon",
                modifier = Modifier.size(48.dp)
            )
        } else {
            Box(Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                appDataUsage.appName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(appDataUsage.packageName, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "Mobile: ${formatDataUsage(appDataUsage.mobileDataUsageBytes)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "WiFi: ${formatDataUsage(appDataUsage.wifiDataUsageBytes)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Total: ${formatDataUsage(appDataUsage.mobileDataUsageBytes + appDataUsage.wifiDataUsageBytes)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}