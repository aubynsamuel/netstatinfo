package com.aubynsamuel.netstatinfo.data.model

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.ImageBitmap

@Stable
data class AppDataUsage(
    val appName: String,
    val packageName: String,
    val uid: Int,
    val mobileDataUsageBytes: Long,
    val wifiDataUsageBytes: Long,
    val formattedMobileUsage: String,
    val formattedWifiUsage: String,
    val formattedTotalUsage: String,
    val appIcon: ImageBitmap?,
)
