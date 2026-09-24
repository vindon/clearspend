<!-- ─── AndroidManifest.xml ─────────────────────────────────────────────── -->
<!-- 
    Key permissions and components for ClearSpend.
    Minimal permission footprint — only what's needed.
-->

<!--
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.clearspend">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECEIVE_SMS" />
    <uses-permission android:name="android.permission.READ_SMS" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <uses-feature android:name="android.hardware.camera" android:required="true" />

    <application
        android:name=".ClearSpendApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="ClearSpend"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.ClearSpend">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize"
            android:theme="@style/Theme.ClearSpend.Splash">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- SMS BroadcastReceiver — triggers on bank SMS arrival -->
        <receiver
            android:name=".data.receiver.SmsReceiver"
            android:exported="false"
            android:permission="android.permission.BROADCAST_SMS">
            <intent-filter android:priority="999">
                <action android:name="android.provider.Telephony.SMS_RECEIVED" />
            </intent-filter>
        </receiver>

        <!-- WorkManager worker for weekly AI coach report -->
        <provider
            android:name="androidx.startup.InitializationProvider"
            android:authorities="${applicationId}.androidx-startup"
            android:exported="false">
            <meta-data
                android:name="androidx.work.WorkManagerInitializer"
                android:value="androidx.startup" />
        </provider>

    </application>
</manifest>
-->

// ─── ClearSpendApp.kt ─────────────────────────────────────────────────────────
// package com.clearspend
//
// import android.app.Application
// import androidx.hilt.work.HiltWorkerFactory
// import androidx.work.*
// import dagger.hilt.android.HiltAndroidApp
// import java.util.concurrent.TimeUnit
// import javax.inject.Inject
//
// @HiltAndroidApp
// class ClearSpendApp : Application(), Configuration.Provider {
//
//     @Inject lateinit var workerFactory: HiltWorkerFactory
//
//     override val workManagerConfiguration: Configuration
//         get() = Configuration.Builder()
//             .setWorkerFactory(workerFactory)
//             .build()
//
//     override fun onCreate() {
//         super.onCreate()
//         scheduleWeeklyCoachJob()
//     }
//
//     private fun scheduleWeeklyCoachJob() {
//         val constraints = Constraints.Builder()
//             .setRequiredNetworkType(NetworkType.CONNECTED)
//             .build()
//
//         val weeklyRequest = PeriodicWorkRequestBuilder<WeeklyCoachWorker>(7, TimeUnit.DAYS)
//             .setConstraints(constraints)
//             .setInitialDelay(calculateDelayToNextSunday(), TimeUnit.MILLISECONDS)
//             .build()
//
//         WorkManager.getInstance(this).enqueueUniquePeriodicWork(
//             "weekly_coach",
//             ExistingPeriodicWorkPolicy.KEEP,
//             weeklyRequest
//         )
//     }
//
//     private fun calculateDelayToNextSunday(): Long {
//         val now = java.time.LocalDateTime.now()
//         val nextSunday = now.with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.SUNDAY))
//             .withHour(8).withMinute(0).withSecond(0)
//         return java.time.Duration.between(now, nextSunday).toMillis().coerceAtLeast(0)
//     }
// }

// ─── di/AppModule.kt ──────────────────────────────────────────────────────────

// @Module
// @InstallIn(SingletonComponent::class)
// object AppModule {
//
//     @Provides
//     @Singleton
//     fun provideDatabase(@ApplicationContext context: Context): ClearSpendDatabase =
//         Room.databaseBuilder(context, ClearSpendDatabase::class.java, "clearspend.db")
//             .fallbackToDestructiveMigration()
//             .build()
//
//     @Provides fun provideTransactionDao(db: ClearSpendDatabase) = db.transactionDao()
//     @Provides fun provideBudgetDao(db: ClearSpendDatabase) = db.budgetDao()
//     @Provides fun provideCoachInsightDao(db: ClearSpendDatabase) = db.coachInsightDao()
//
//     @Provides
//     @Singleton
//     fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
//         PreferenceDataStoreFactory.create {
//             context.preferencesDataStoreFile("clearspend_prefs")
//         }
// }

// ─── data/worker/WeeklyCoachWorker.kt ────────────────────────────────────────

// @HiltWorker
// class WeeklyCoachWorker @AssistedInject constructor(
//     @Assisted appContext: Context,
//     @Assisted workerParams: WorkerParameters,
//     private val transactionRepo: TransactionRepository,
//     private val coachRepo: CoachRepository,
//     private val geminiParser: GeminiReceiptParser,
//     private val subscriptionRepo: SubscriptionRepository
// ) : CoroutineWorker(appContext, workerParams) {
//
//     override suspend fun doWork(): Result {
//         // Only run for Pro subscribers
//         if (!subscriptionRepo.isProActive()) return Result.success()
//
//         val today = LocalDate.now()
//         val weekStart = today.minusDays(6)
//         val lastWeekStart = today.minusDays(13)
//         val lastWeekEnd = today.minusDays(7)
//
//         val thisWeekTx = transactionRepo.getForDateRange(weekStart, today)
//         val lastWeekTx = transactionRepo.getForDateRange(lastWeekStart, lastWeekEnd)
//
//         if (thisWeekTx.isEmpty()) return Result.success()
//
//         val thisWeekSummary = buildWeekSummary(thisWeekTx)
//         val lastWeekSummary = buildWeekSummary(lastWeekTx)
//
//         val insight = geminiParser.generateWeeklyInsight(thisWeekSummary, lastWeekSummary)
//             ?: return Result.retry()
//
//         val topCat = thisWeekTx
//             .groupBy { it.category }
//             .maxByOrNull { it.value.sumOf { t -> t.amount } }
//             ?.key ?: Category.OTHER
//
//         val thisTotal = thisWeekTx.sumOf { it.amount }
//         val lastTotal = lastWeekTx.sumOf { it.amount }
//         val change = if (lastTotal > 0) ((thisTotal - lastTotal) / lastTotal * 100) else 0.0
//
//         coachRepo.saveInsight(CoachInsight(
//             weekStartDate = weekStart,
//             summary = insight,
//             topCategory = topCat,
//             changeVsLastWeek = change
//         ))
//
//         // Send notification
//         NotificationHelper.sendCoachNotification(applicationContext, insight.take(80))
//
//         return Result.success()
//     }
//
//     private fun buildWeekSummary(transactions: List<Transaction>): String {
//         val total = transactions.sumOf { it.amount }
//         val byCategory = transactions.groupBy { it.category }
//             .mapValues { it.value.sumOf { t -> t.amount } }
//             .entries.sortedByDescending { it.value }
//             .joinToString("\n") { "- ${it.key.displayName}: ₹${it.value.toInt()}" }
//         return "Total spend: ₹${total.toInt()}\n$byCategory"
//     }
// }

// ─── data/worker/SmsProcessingService.kt ─────────────────────────────────────

// object SmsProcessingService {
//     fun enqueue(context: Context, sender: String, body: String, timestamp: Long) {
//         val data = workDataOf(
//             "sender" to sender,
//             "body" to body,
//             "timestamp" to timestamp
//         )
//         val request = OneTimeWorkRequestBuilder<SmsWorker>()
//             .setInputData(data)
//             .setConstraints(Constraints.NONE)
//             .build()
//         WorkManager.getInstance(context).enqueue(request)
//     }
// }
//
// @HiltWorker
// class SmsWorker @AssistedInject constructor(
//     @Assisted context: Context,
//     @Assisted params: WorkerParameters,
//     private val smsParser: SmsBankParser,
//     private val transactionRepo: TransactionRepository
// ) : CoroutineWorker(context, params) {
//
//     override suspend fun doWork(): Result {
//         val sender = inputData.getString("sender") ?: return Result.failure()
//         val body = inputData.getString("body") ?: return Result.failure()
//         val timestamp = inputData.getLong("timestamp", System.currentTimeMillis())
//
//         if (!smsParser.isBankSms(sender)) return Result.success()
//
//         val parsed = smsParser.parseDebit(body, timestamp) ?: return Result.success()
//
//         // Dedup check: same merchant + amount within 5 minutes
//         val fiveMinMs = 5 * 60 * 1000L
//         val dups = transactionRepo.countDuplicates(
//             amount = parsed.amount,
//             merchant = parsed.merchant,
//             minTime = timestamp - fiveMinMs,
//             maxTime = timestamp + fiveMinMs
//         )
//         if (dups > 0) return Result.success()  // Already imported
//
//         transactionRepo.save(Transaction(
//             merchant = parsed.merchant,
//             amount = parsed.amount,
//             category = parsed.category,
//             date = LocalDate.ofEpochDay(timestamp / 86_400_000),
//             source = TransactionSource.SMS_IMPORT
//         ))
//
//         return Result.success()
//     }
// }
