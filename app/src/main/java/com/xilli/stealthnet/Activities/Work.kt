package com.xilli.stealthnet.Activities

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters

class Work(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        val initialTime = inputData.getLong("initial_time", 1800000 )
        var timeRemaining = initialTime

        while (timeRemaining > 0) {
            sendProgressBroadcast(timeRemaining)
            timeRemaining -= 1000
            Thread.sleep(1000)
        }

        sendOutputBroadcast()
        return Result.success()
    }

    private fun sendProgressBroadcast(timeRemainingMillis: Long) {
        val intent = Intent("countdown_progress")
        intent.putExtra("time_remaining", timeRemainingMillis)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun sendOutputBroadcast() {
        val intent = Intent("countdown_finished")
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

}