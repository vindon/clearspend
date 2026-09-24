package com.clearspend.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.clearspend.data.worker.SmsProcessingService

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        messages.forEach { smsMessage ->
            val sender = smsMessage.originatingAddress ?: return@forEach
            val body = smsMessage.messageBody ?: return@forEach
            SmsProcessingService.enqueue(context, sender, body, smsMessage.timestampMillis)
        }
    }
}
