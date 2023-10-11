package com.xilli.stealthnet.helper

interface CountdownCallback {
    fun onTimerTick(remainingTime: Long)
}