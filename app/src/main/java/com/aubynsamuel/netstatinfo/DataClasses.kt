package com.aubynsamuel.netstatinfo

import android.graphics.drawable.Drawable
import java.util.Calendar

data class AppDataUsage(
    val appName: String,
    val packageName: String,
    val uid: Int,
    val mobileDataUsageBytes: Long,
    val wifiDataUsageBytes: Long,
    val appIcon: Drawable?,
)

enum class TimePeriod(val displayName: String, val getStartTimeMillis: () -> Long) {
    LAST_HOUR(
        "Last Hour",
        { Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, -1) }.timeInMillis }),
    LAST_6_HOURS(
        "Last 6 Hours",
        { Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, -6) }.timeInMillis }),
    LAST_24_HOURS(
        "Last 24 Hours",
        { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis }),
    LAST_7_DAYS(
        "Last 7 Days",
        { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis }),
    ALL_TIME("All Time", { 0L }); // Using 0L for start time (Unix epoch)
}