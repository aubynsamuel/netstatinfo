package com.aubynsamuel.netstatinfo

import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Process
import android.util.Log
import java.util.Locale
import kotlin.use

fun hasUsageStatsPermission(context: Context): Boolean {
    val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager?
    // Check if appOpsManager is null
    if (appOpsManager == null) {
        Log.e(TAG, "AppOpsManager is null, cannot check permission.")
        return false
    }

    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    } else {
        @Suppress("DEPRECATION")
        appOpsManager.checkOp(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

fun getAppsDataUsage(
    context: Context,
    startTimeMillis: Long,
    endTimeMillis: Long,
): List<AppDataUsage> {
    if (!hasUsageStatsPermission(context)) {
        Log.e(TAG, "Usage stats permission not granted. Cannot fetch app data usage.")
        return emptyList()
    }

    val networkStatsManager =
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager?
    val packageManager = context.packageManager

    if (networkStatsManager == null) {
        Log.e(TAG, "NetworkStatsManager is null. Cannot fetch network stats.")
        return emptyList()
    }

    val appDataUsageMap = mutableMapOf<String, AppDataUsage>()

    try {
        val installedApps: List<ApplicationInfo> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledApplications(0)
            }

        for (appInfo in installedApps) {
            try {
                val uid = appInfo.uid
                var mobileBytes: Long = 0
                var wifiBytes: Long = 0

                // --- Mobile Data ---
                val mobileNetworkType = ConnectivityManager.TYPE_MOBILE
                try {
                    val networkStatsMobile = networkStatsManager.queryDetailsForUid(
                        mobileNetworkType,
                        null, // subscriberId - null for all mobile networks
                        startTimeMillis,
                        endTimeMillis,
                        uid
                    )

                    networkStatsMobile.use { stats ->
                        val bucket = NetworkStats.Bucket()
                        while (stats.hasNextBucket()) {
                            stats.getNextBucket(bucket)
                            mobileBytes += bucket.rxBytes + bucket.txBytes
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error getting mobile data for ${appInfo.packageName}: ${e.message}")
                }

                // --- WiFi Data ---
                val wifiNetworkType = ConnectivityManager.TYPE_WIFI
                try {
                    val networkStatsWifi = networkStatsManager.queryDetailsForUid(
                        wifiNetworkType,
                        null, // No subscriberId for WiFi
                        startTimeMillis,
                        endTimeMillis,
                        uid
                    )

                    networkStatsWifi.use { stats ->
                        val bucket = NetworkStats.Bucket()
                        while (stats.hasNextBucket()) {
                            stats.getNextBucket(bucket)
                            wifiBytes += bucket.rxBytes + bucket.txBytes
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error getting wifi data for ${appInfo.packageName}: ${e.message}")
                }

                if (mobileBytes > 0 || wifiBytes > 0) {
                    val appName = try {
                        appInfo.loadLabel(packageManager).toString()
                    } catch (_: Exception) {
                        appInfo.packageName
                    }

                    val packageName = appInfo.packageName
                    val appIcon = try {
                        appInfo.loadIcon(packageManager)
                    } catch (_: Exception) {
                        null
                    }

                    val existingEntry = appDataUsageMap[packageName]
                    if (existingEntry != null) {
                        appDataUsageMap[packageName] = existingEntry.copy(
                            mobileDataUsageBytes = existingEntry.mobileDataUsageBytes + mobileBytes,
                            wifiDataUsageBytes = existingEntry.wifiDataUsageBytes + wifiBytes
                        )
                    } else {
                        appDataUsageMap[packageName] = AppDataUsage(
                            appName = appName,
                            packageName = packageName,
                            uid = uid,
                            mobileDataUsageBytes = mobileBytes,
                            wifiDataUsageBytes = wifiBytes,
                            appIcon = appIcon
                        )
                    }
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "SecurityException for ${appInfo.packageName}: ${e.message}")
            } catch (e: Exception) {
                Log.w(TAG, "Exception for ${appInfo.packageName}: ${e.message}")
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error getting installed applications", e)
    }

    return appDataUsageMap.values.toList()
}

fun formatDataUsage(bytes: Long): String {
    val locale = Locale.getDefault()
    return when {
        bytes == 0L -> "0 B"
        bytes < 1024 -> String.format(locale, "%d B", bytes)
        bytes < 1024 * 1024 -> String.format(locale, "%.2f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format(locale, "%.2f MB", bytes / (1024.0 * 1024.0))
        else -> String.format(locale, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}