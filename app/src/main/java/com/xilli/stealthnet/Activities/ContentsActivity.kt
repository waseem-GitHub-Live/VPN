package com.xilli.stealthnet.Activities

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.facebook.ads.*
import com.facebook.ads.AdView
import com.google.android.gms.ads.*
import com.xilli.stealthnet.R
import com.xilli.stealthnet.speed.Speed
import com.xilli.stealthnet.ui.toolside
import com.onesignal.OneSignal
import com.xilli.stealthnet.Fragments.HomeFragmentDirections
import es.dmoral.toasty.Toasty
import pl.droidsonroids.gif.GifImageView


abstract class ContentsActivity : toolside() {

    var tvIpAddress: TextView? = null

    var connectionStateTextView: TextView? = null

    @JvmField
    var imgFlag: ImageView? = null

    @JvmField
    var flagName: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        connectionStateTextView = findViewById(R.id.connect)
        imgFlag = findViewById(R.id.flagimageView)

        flagName = findViewById(R.id.flag_name)


        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()
        tvIpAddress = findViewById(R.id.vpn_ip)
        showIP()


    }

    fun showIP() {
        val queue = Volley.newRequestQueue(this)
        val urlip = "https://checkip.amazonaws.com/"

        val stringRequest =
                StringRequest(Request.Method.GET, urlip, { response -> tvIpAddress?.setText(response) })
                { e ->
                    run {
                        tvIpAddress?.setText(getString(R.string.app_name))
                    }
                }
        queue.add(stringRequest)
    }
}