package com.clearspend

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.clearspend.data.worker.WeeklyCoachWorker
import dagger.hilt.android.HiltAndroidApp
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ClearSpendApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleWeeklyCoachJob()
    }

    private fun scheduleWeeklyCoachJob() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val weeklyRequest = PeriodicWorkRequestBuilder<WeeklyCoachWorker>(7, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(calculateDelayToNextSunday(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "weekly_coach",
            ExistingPeriodicWorkPolicy.KEEP,
            weeklyRequest
        )
    }

    private fun calculateDelayToNextSunday(): Long {
        val now = LocalDateTime.now()
        val nextSunday = now.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
            .withHour(9).withMinute(0).withSecond(0)
        return Duration.between(now, nextSunday).toMillis().coerceAtLeast(0)
    }
}
