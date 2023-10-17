package com.xilli.stealthnet.Activities

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

class Work(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        try {
            var timeLeftInSeconds = inputData.getInt("initial_time", 30 * 60)

            while (timeLeftInSeconds > 0) {
                val progress = workDataOf("timeLeft" to timeLeftInSeconds)
                setProgressAsync(progress)
                Thread.sleep(1000)
                timeLeftInSeconds--
            }
        } catch (e: InterruptedException) {
            return Result.failure()
        }

        return Result.success()
    }
}