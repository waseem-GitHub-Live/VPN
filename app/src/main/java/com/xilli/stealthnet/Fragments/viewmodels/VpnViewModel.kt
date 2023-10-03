package com.xilli.stealthnet.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VpnViewModel: ViewModel() {
    private val _isVpnConnecting = MutableLiveData(false)
    val isVpnConnecting: LiveData<Boolean> = _isVpnConnecting
    private val _isVpnDisconnecting = MutableLiveData(false)
    val isVpnDisconnecting: LiveData<Boolean> = _isVpnDisconnecting

    // Method to start VPN connection
    fun startVpn() {
        _isVpnConnecting.value = true
    }
    fun stopVpn() {

        _isVpnDisconnecting.value = false
    }
    // Method to reset VPN connection state
    fun resetVpnConnectionState() {
        _isVpnConnecting.value = false
    }
}