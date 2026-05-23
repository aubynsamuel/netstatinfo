package com.aubynsamuel.netstatinfo.data.model

import java.time.Instant
import java.time.temporal.ChronoUnit

enum class TimePeriod(val displayName: String, val getStartTimeMillis: () -> Long) {
    LAST_HOUR(
        "Last Hour",
        { Instant.now().minus(1, ChronoUnit.HOURS).toEpochMilli() }
    ),
    LAST_6_HOURS(
        "Last 6 Hours",
        { Instant.now().minus(6, ChronoUnit.HOURS).toEpochMilli() }
    ),
    LAST_24_HOURS(
        "Last 24 Hours",
        { Instant.now().minus(24, ChronoUnit.HOURS).toEpochMilli() }
    ),
    LAST_7_DAYS(
        "Last 7 Days",
        { Instant.now().minus(7, ChronoUnit.DAYS).toEpochMilli() }
    ),
    ALL_TIME(
        "All Time",
        { 0L }
    );
}
