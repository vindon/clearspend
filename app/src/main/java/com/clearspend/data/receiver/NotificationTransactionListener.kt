package com.clearspend.data.receiver

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.clearspend.data.worker.SmsProcessingService

/**
 * Resilient Fallback Ingestion: Captures transaction notifications from Google Pay,
 * PhonePe, Paytm, and bank apps if SMS permission is not granted.
 */
class NotificationTransactionListener : NotificationListenerService() {

    private val targetPackages = setOf(
        "com.google.android.apps.nbu.paisa.user", // Google Pay
        "com.phonepe.app",                        // PhonePe
        "net.one97.paytm",                        // Paytm
        "com.hdfcbank.payzapp",                   // PayZapp
        "com.icicibank.imobile"                   // iMobile
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val sbnNotNull = sbn ?: return
        val packageName = sbnNotNull.packageName ?: return

        if (!targetPackages.contains(packageName)) return

        val extras = sbnNotNull.notification.extras ?: return
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val combined = "$title: $text"

        if (combined.contains("debited", ignoreCase = true) || combined.contains("paid", ignoreCase = true)) {
            SmsProcessingService.enqueue(
                context = applicationContext,
                sender = packageName,
                body = combined,
                timestamp = sbnNotNull.postTime
            )
        }
    }
}
