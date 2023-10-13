package com.xilli.stealthnet.Activities

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper (context: Context) {
    private val PREFS_NAME = "MyPrefs"
    private val DOWNLOAD_KEY = "download_average"
    private val UPLOAD_KEY = "upload_average"
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun saveAverages(downloadAverage: Float, uploadAverage: Float) {
        editor.putFloat(DOWNLOAD_KEY, downloadAverage)
        editor.putFloat(UPLOAD_KEY, uploadAverage)
        editor.apply()
    }

    fun getDownloadAverage(): Float {
        return sharedPreferences.getFloat(DOWNLOAD_KEY, 0.0f) // 0.0f is the default value
    }

    fun getUploadAverage(): Float {
        return sharedPreferences.getFloat(UPLOAD_KEY, 0.0f) // 0.0f is the default value
    }
}