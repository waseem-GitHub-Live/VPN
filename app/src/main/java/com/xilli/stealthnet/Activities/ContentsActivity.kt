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


    private var mLastRxBytes: Long = 0
    private var mLastTxBytes: Long = 0
    private var mLastTime: Long = 0
    private var mSpeed: Speed? = null

    var lottieAnimationView: LottieAnimationView? = null
    var vpnToastCheck = true
    var handlerTraffic: Handler? = null
    private val adCount = 0

    private var loadingAd: Boolean? = false
    var frameLayout: RelativeLayout? = null
    var nativeAdLayout: NativeAdLayout? = null

    @JvmField


    var progressBarValue = 0
    var handler = Handler(Looper.getMainLooper())
    private val customHandler = Handler(Looper.getMainLooper())
    private var startTime = 0L
    var timeInMilliseconds = 0L
    var timeSwapBuff = 0L
    var updatedTime = 0L


    var tvIpAddress: TextView? = null
    var textDownloading: TextView? = null
    var textUploading: TextView? = null
    var tvConnectionStatus: TextView? = null
    var ivConnectionStatusImage: ImageView? = null
    var ivVpnDetail: ImageView? = null
    var timerTextView: TextView? = null

    var connectionStateTextView: TextView? = null
    var rcvFree: RecyclerView? = null
    var footer: RelativeLayout? = null
    var gifImageView1: GifImageView? = null
    var gifImageView2: GifImageView? = null
    lateinit var sharedPreferences :SharedPreferences

    @JvmField
    var imgFlag: ImageView? = null

    @JvmField
    var flagName: TextView? = null


    var facebookAdView: AdView? = null
    private var nativeAd: NativeAd? = null


    @JvmField
    var facebookInterstitialAd: InterstitialAd? = null

    private var STATUS: String? = "DISCONNECTED"

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

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()

    }

    override fun onDestroy() {

        super.onDestroy()
    }

    private fun showOrHideAppendLayout() {
        if (footer?.visibility == View.VISIBLE) {
            ivVpnDetail?.setImageResource(R.drawable.ic_drop_down)
            footer?.visibility = View.GONE
        } else {
            ivVpnDetail?.setImageResource(R.drawable.ic_up)
            footer?.visibility = View.VISIBLE
        }
    }
    fun loadLottieAnimation() {
        val lottieAnimationView: LottieAnimationView = findViewById(R.id.lottieAnimationView)
        lottieAnimationView.setAnimation(R.raw.loading_animation)

        val lottieAnimationView2: LottieAnimationView = findViewById(R.id.lottieAnimationView2)
        lottieAnimationView2.setAnimation(R.raw.backview)
        lottieAnimationView2.repeatCount = LottieDrawable.INFINITE

        lottieAnimationView.addAnimatorUpdateListener {
            // Update listener logic here
        }

        lottieAnimationView2.addAnimatorUpdateListener {
            // Update listener logic here
        }

        lottieAnimationView.playAnimation()
        lottieAnimationView2.playAnimation()
    }
    private val mUIHandler = Handler(Looper.getMainLooper())
    val mUIUpdateRunnable: Runnable = object : Runnable {
        override fun run() {

            checkRemainingTraffic()
            mUIHandler.postDelayed(this, 10000)
        }
    }

    fun btnConnectDisconnect() {
        if (STATUS != "DISCONNECTED") {
            disconnectAlert()
        } else {
            if (!Utility.isOnline(applicationContext)) {
                showMessage("No Internet Connection", "error")
            } else {
                checkSelectedCountry()
            }
        }
    }

    protected abstract fun checkRemainingTraffic()



    protected fun hideConnectProgress() {
        connectionStateTextView?.visibility = View.VISIBLE
    }

    protected fun disconnectAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Do you want to disconnect?")
        builder.setPositiveButton(
                "Disconnect"
        ) { _, _ ->
            disconnectFromVpn()
            STATUS = "DISCONNECTED"

            textDownloading?.text = "0.0 kB/s"
            textUploading?.text = "0.0 kB/s"

            showMessage("Server Disconnected", "success")
        }
        builder.setNegativeButton(
                "Cancel"
        ) { _, _ ->
            showMessage("VPN Remains Connected", "success")
        }
        builder.show()
    }

    companion object {
        protected val TAG = MainActivity::class.java.simpleName
    }

    protected fun showMessage(msg: String?, type:String) {

        if(type == "success") {
            Toasty.success(
                    this@ContentsActivity,
                    msg + "",
                    Toast.LENGTH_SHORT
            ).show()
        } else if (type == "error") {
            Toasty.error(
                    this@ContentsActivity,
                    msg + "",
                    Toast.LENGTH_SHORT
            ).show()
        } else {
            Toasty.normal(
                    this@ContentsActivity,
                    msg + "",
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    open fun updateConnectionStatus(
            duration: String?,
            lastPacketReceive: String?,
            byteIn: String,
            byteOut: String
    ) {
        val byteinKb = byteIn.split("-").toTypedArray()[1]
        val byteoutKb = byteOut.split("-").toTypedArray()[1]

        textDownloading?.text = byteinKb
        textUploading?.text = byteoutKb
        timerTextView?.text = duration
    }

    fun showInterstitialAndConnect() {
            prepareVpn()
    }

    protected abstract fun checkSelectedCountry()
    protected abstract fun prepareVpn()
    protected abstract fun disconnectFromVpn()
}