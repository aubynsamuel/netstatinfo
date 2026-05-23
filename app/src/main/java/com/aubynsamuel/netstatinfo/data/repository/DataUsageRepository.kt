package com.aubynsamuel.netstatinfo.data.repository

import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.aubynsamuel.netstatinfo.data.model.AppDataUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

private const val TAG = "NetStatInfoRepo"

interface DataUsageRepository {
    fun hasUsageStatsPermission(): Boolean
    suspend fun getAppsDataUsage(startTimeMillis: Long, endTimeMillis: Long): List<AppDataUsage>
}

class DataUsageRepositoryImpl(private val context: Context) : DataUsageRepository {

    override fun hasUsageStatsPermission(): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager?
            ?: return false

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

    override suspend fun getAppsDataUsage(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): List<AppDataUsage> = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission()) {
            Log.e(TAG, "Usage stats permission not granted. Cannot fetch data usage.")
            return@withContext emptyList()
        }

        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager?
                ?: return@withContext emptyList()

        val packageManager = context.packageManager
        val uidDataMap = mutableMapOf<Int, Pair<Long, Long>>() // uid -> (mobileBytes, wifiBytes)

        // 1. Query Mobile Stats (Cumulative for all UIDs)
        try {
            val stats = networkStatsManager.querySummary(
                ConnectivityManager.TYPE_MOBILE,
                null,
                startTimeMillis,
                endTimeMillis
            )
            val bucket = NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                val uid = bucket.uid
                val bytes = bucket.rxBytes + bucket.txBytes
                if (bytes > 0) {
                    val current = uidDataMap[uid] ?: Pair(0L, 0L)
                    uidDataMap[uid] = Pair(current.first + bytes, current.second)
                }
            }
            stats.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error querying mobile stats: ${e.message}")
        }

        // 2. Query WiFi Stats (Cumulative for all UIDs)
        try {
            val stats = networkStatsManager.querySummary(
                ConnectivityManager.TYPE_WIFI,
                null,
                startTimeMillis,
                endTimeMillis
            )
            val bucket = NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                val uid = bucket.uid
                val bytes = bucket.rxBytes + bucket.txBytes
                if (bytes > 0) {
                    val current = uidDataMap[uid] ?: Pair(0L, 0L)
                    uidDataMap[uid] = Pair(current.first, current.second + bytes)
                }
            }
            stats.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error querying wifi stats: ${e.message}")
        }

        if (uidDataMap.isEmpty()) {
            return@withContext emptyList()
        }

        // 3. Load all installed applications once (Fast - doesn't load label/icon)
        val installedApps = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledApplications(0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching installed applications", e)
            emptyList()
        }

        val uidToAppInfos = installedApps.groupBy { it.uid }
        val resultList = mutableListOf<AppDataUsage>()

        // 4. Resolve details only for active UIDs
        for ((uid, bytesPair) in uidDataMap) {
            val (mobileBytes, wifiBytes) = bytesPair
            if (mobileBytes == 0L && wifiBytes == 0L) continue

            val appInfos = uidToAppInfos[uid] ?: continue
            for (appInfo in appInfos) {
                val appName = try {
                    appInfo.loadLabel(packageManager).toString()
                } catch (_: Exception) {
                    appInfo.packageName
                }

                val appIcon: ImageBitmap? = try {
                    val drawable = appInfo.loadIcon(packageManager)
                    // Convert Drawable to Bitmap (48dp size) on background thread
                    val bitmap = drawable.toBitmap(
                        width = 96,
                        height = 96
                    ) // 96px is perfect for sharp icons
                    bitmap.asImageBitmap()
                } catch (_: Exception) {
                    null
                }

                resultList.add(
                    AppDataUsage(
                        appName = appName,
                        packageName = appInfo.packageName,
                        uid = uid,
                        mobileDataUsageBytes = mobileBytes,
                        wifiDataUsageBytes = wifiBytes,
                        formattedMobileUsage = formatDataUsage(mobileBytes),
                        formattedWifiUsage = formatDataUsage(wifiBytes),
                        formattedTotalUsage = formatDataUsage(mobileBytes + wifiBytes),
                        appIcon = appIcon
                    )
                )
            }
        }

        // Sort by total usage descending
        resultList.sortByDescending { it.mobileDataUsageBytes + it.wifiDataUsageBytes }
        resultList
    }

    private fun formatDataUsage(bytes: Long): String {
        val locale = Locale.getDefault()
        return when {
            bytes == 0L -> "0 B"
            bytes < 1024 -> String.format(locale, "%d B", bytes)
            bytes < 1024 * 1024 -> String.format(locale, "%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format(
                locale,
                "%.2f MB",
                bytes / (1024.0 * 1024.0)
            )

            else -> String.format(locale, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
