package com.xilli.stealthnet.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewmodel : ViewModel() {
    private val averageRxSpeedLiveData = MutableLiveData<String>()
    private val averageTxSpeedLiveData = MutableLiveData<String>()

    fun setAverageRxSpeed(speed: String) {
        averageRxSpeedLiveData.value = speed
    }

    fun setAverageTxSpeed(speed: String) {
        averageTxSpeedLiveData.value = speed
    }

    fun getAverageRxSpeed(): LiveData<String> {
        return averageRxSpeedLiveData
    }

    fun getAverageTxSpeed(): LiveData<String> {
        return averageTxSpeedLiveData
    }
    var totalDataUsage1: String = ""
    private var startTimeMillis = 0L
    private var isTimerRunning = false

    fun startTimer() {
        if (!isTimerRunning) {
            startTimeMillis = System.currentTimeMillis()
            isTimerRunning = true
        }
    }

    fun stopTimer() {
        isTimerRunning = false
    }

    fun getElapsedTime(): Long {
        return if (isTimerRunning) {
            System.currentTimeMillis() - startTimeMillis
        } else {
            0L
        }
    }
}