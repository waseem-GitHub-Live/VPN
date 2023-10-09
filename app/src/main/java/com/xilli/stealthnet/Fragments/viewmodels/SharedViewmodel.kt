package com.xilli.stealthnet.Fragments.viewmodels

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.xilli.stealthnet.R
import com.xilli.stealthnet.helper.Utils.getSystemService
import com.xilli.stealthnet.helper.Utils.isVpnActiveFlow
import com.xilli.stealthnet.model.Countries
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
    var isSwitchChecked = MutableLiveData<Boolean>()
    var isSwitchCheckedauto = MutableLiveData<Boolean>()
    var isSwitchCheckedimprove = MutableLiveData<Boolean>()
    var isSwitchCheckedsavedata = MutableLiveData<Boolean>()
    fun <T> Flow<T>.asLiveData(): LiveData<T> {
        val liveData = MutableLiveData<T>()
        this.onEach { value -> liveData.value = value }
        return liveData
    }
    val selectedItem: MutableLiveData<Countries> = MutableLiveData()
  val isVpnActiveLiveData: LiveData<Boolean> = isVpnActiveFlow.asLiveData()
}