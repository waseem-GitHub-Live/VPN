package com.xilli.stealthnet.Activities


import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.CountDownTimer
import android.os.IBinder
import android.preference.PreferenceManager


class CountDownService : Service() {
    private lateinit var timer: CountDownTimer
    private var remainingTime: Long = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        if (remainingTime == 0L) {
            remainingTime = sharedPreferences.getLong("remaining_time", 30 * 60 * 1000)
        }

        timer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Send the remaining time to the Fragment
                val intent = Intent("COUNTDOWN_TICK")
                intent.putExtra("time_left", millisUntilFinished)
                sendBroadcast(intent)
                remainingTime = millisUntilFinished
                sharedPreferences.edit().putLong("remaining_time", remainingTime).apply()
            }

            override fun onFinish() {
                // Countdown finished
            }
        }
        timer.start()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}