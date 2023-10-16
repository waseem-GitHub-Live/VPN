package com.xilli.stealthnet.Fragments.viewmodels

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xilli.stealthnet.model.Countries

class SharedViewmodel : ViewModel() {

    var remainingTimeMillis = 30 * 60 * 1000L
    var timeRemainingMillis: Long = 30L * 60 * 1000
    private var startTimeMillis = 0L
    private var isTimerRunning = false
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

    val lastPacketReceivedLiveData = MutableLiveData<String>()
    val durationLiveData = MutableLiveData<String>()
    val byteInLiveData = MutableLiveData<String>()
    val byteOutLiveData = MutableLiveData<String>()

}