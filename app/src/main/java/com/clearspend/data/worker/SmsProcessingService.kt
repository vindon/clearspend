package com.clearspend.data.worker

import android.content.Context
import androidx.work.*

object SmsProcessingService {
    fun enqueue(context: Context, sender: String, body: String, timestamp: Long) {
        val data = workDataOf(
            "sender" to sender,
            "body" to body,
            "timestamp" to timestamp
        )
        val request = OneTimeWorkRequestBuilder<SmsWorker>()
            .setInputData(data)
            .setConstraints(Constraints.NONE)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
