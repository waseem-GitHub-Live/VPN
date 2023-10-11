package com.xilli.stealthnet.Fragments.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xilli.stealthnet.model.Countries

class SharedViewmodel : ViewModel() {
    private val averageRxSpeedLiveData = MutableLiveData<String>()
    private val averageTxSpeedLiveData = MutableLiveData<String>()
    var remainingTimeMillis = 30 * 60 * 1000L

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
        // Calculate and store the remaining time when stopping the timer
        remainingTimeMillis -= System.currentTimeMillis() - startTimeMillis
    }

    fun getRemainingTime(): Long {
        return if (isTimerRunning) {
            remainingTimeMillis - (System.currentTimeMillis() - startTimeMillis)
        } else {
            remainingTimeMillis
        }
    }
    var isSwitchChecked = MutableLiveData<Boolean>()
    var isSwitchCheckedauto = MutableLiveData<Boolean>()
    var isSwitchCheckedimprove = MutableLiveData<Boolean>()
    var isSwitchCheckedsavedata = MutableLiveData<Boolean>()

    val selectedItem: MutableLiveData<Countries> = MutableLiveData()

}