package de.readeckapp.domain.model

import java.util.concurrent.TimeUnit


enum class AutoSyncTimeframe(repeatInterval: Long, repeatIntervalTimeUnit: TimeUnit) {
    MANUAL(0L, TimeUnit.MILLISECONDS),
    HOURS_01(1L, TimeUnit.HOURS),
    HOURS_06(6L, TimeUnit.HOURS),
    HOURS_12(12L, TimeUnit.HOURS),
    DAYS_01(1L, TimeUnit.DAYS),
    DAYS_07(7L, TimeUnit.DAYS),
    DAYS_14(14L, TimeUnit.DAYS),
    DAYS_30(30L, TimeUnit.DAYS)
}