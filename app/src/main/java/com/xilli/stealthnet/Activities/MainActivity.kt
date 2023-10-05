package com.xilli.stealthnet.Activities

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender.SendIntentException
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.SkuType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.OnSuccessListener
import com.onesignal.OneSignal
import com.xilli.stealthnet.Config
import com.xilli.stealthnet.Fragments.HomeFragment
import com.xilli.stealthnet.Fragments.HomeFragmentDirections
import com.xilli.stealthnet.R
import com.xilli.stealthnet.Utils.ActiveServer
import com.xilli.stealthnet.helper.Utils
import com.xilli.stealthnet.helper.Utils.connectBtnTextView
import com.xilli.stealthnet.helper.Utils.connectionStateTextView
import com.xilli.stealthnet.helper.Utils.flagName
import com.xilli.stealthnet.helper.Utils.imgFlag
import com.xilli.stealthnet.helper.Utils.isConnected
import com.xilli.stealthnet.helper.Utils.ivConnectionStatusImage
import com.xilli.stealthnet.helper.Utils.lottieAnimationView
import com.xilli.stealthnet.helper.Utils.textDownloading
import com.xilli.stealthnet.helper.Utils.textUploading
import com.xilli.stealthnet.helper.Utils.timerTextView
import com.xilli.stealthnet.helper.Utils.tvConnectionStatus
import com.xilli.stealthnet.helper.Utils.updateUI
import com.xilli.stealthnet.model.Countries
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_home.imageView4
import top.oneconnectapi.app.OpenVpnApi
import top.oneconnectapi.app.core.OpenVPNThread
import java.net.NetworkInterface
import java.util.Arrays
import java.util.Locale
import java.util.Objects

class MainActivity : AppCompatActivity(), PurchasesUpdatedListener, BillingClientStateListener {
    private val locale: Locale? = null
    private var isFirst = true
    private val vpnThread = OpenVPNThread()
    private var billingClient: BillingClient? = null
    private val skusWithSkuDetails: MutableMap<String, SkuDetails> = HashMap()
    private val allSubs: List<String> = ArrayList(
        Arrays.asList(
            Config.all_month_id,
            Config.all_threemonths_id,
            Config.all_sixmonths_id,
            Config.all_yearly_id
        )
    )
    private var STATUS: String? = "DISCONNECTED"

    private fun connectToBillingService() {
        if (!billingClient?.isReady!!) {
            billingClient?.startConnection(this)
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            querySkuDetailsAsync(
                SkuType.SUBS,
                allSubs
            )
            queryPurchases()
        }
    }

    override fun onBillingServiceDisconnected() {
        connectToBillingService()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {}
    private fun queryPurchases() {
        val result = billingClient?.queryPurchases(SkuType.SUBS)
        val purchases = result?.purchasesList
        val skus: MutableList<String> = ArrayList()
        if (purchases != null) {
            var i = 0
            for (purchase in purchases) {
                skus.add(purchase.skus[i])
                Log.v("CHECKBILLING", purchase.skus[i])
                i++
            }
            if (skus.contains(Config.all_month_id) ||
                skus.contains(Config.all_threemonths_id) ||
                skus.contains(Config.all_sixmonths_id) ||
                skus.contains(Config.all_yearly_id)
            ) {
                Config.all_subscription = true
            }
        }
    }

    private fun querySkuDetailsAsync(@SkuType skuType: String, skuList: List<String>) {
        val params = SkuDetailsParams
            .newBuilder()
            .setSkusList(skuList)
            .setType(skuType)
            .build()
        billingClient?.querySkuDetailsAsync(
            params
        ) { billingResult: BillingResult, skuDetailsList: List<SkuDetails>? ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                for (details in skuDetailsList) {
                    skusWithSkuDetails[details.sku] = details
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, IntentFilter("connectionState"))
        billingClient = BillingClient
            .newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        connectToBillingService()
        val intent = intent
        if (getIntent().extras != null) {
            selectedCountry = getIntent().extras?.getParcelable("c")
            updateUI("LOAD")
            if (!Utility.isOnline(applicationContext)) {
                showMessage("No Internet Connection", "error")
            } else {
                showMessage("Vpn is preparing", "success")
            }
        } else {
            if (selectedCountry != null) {
                updateUI("CONNECTED")
                imgFlag?.let {
                    Glide.with(this)
                        .load(selectedCountry?.flagUrl)
                        .into(it)
                }
                flagName?.text = selectedCountry?.country

            }
        }
        if (intent.getStringExtra("type") != null) {
            type = intent.getStringExtra("type")
            indratech_fast_27640849_ad_banner_id =
                intent.getStringExtra("indratech_fast_27640849_ad_banner")
            admob_interstitial_id = intent.getStringExtra("admob_interstitial")
            indratech_fast_27640849_fb_native_id =
                intent.getStringExtra("indratech_fast_27640849_fb_native")
            indratech_fast_27640849_fb_interstitial_id =
                intent.getStringExtra("indratech_fast_27640849_fb_interstitial")
        }
        if (TextUtils.isEmpty(type)) {
            type = ""
            Log.v("AD_TYPE", " null")
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    //    inAppUpdate()
        val homeFragment = supportFragmentManager.findFragmentByTag("homeFragmentTag") as HomeFragment?
//        homeFragment?.setConnectBtnClickListener()
        connectBtnTextView?.setOnClickListener {
        }
//        OneSignal.initWithContext(this)
//        OneSignal.setAppId("a2be7720-a32b-415a-9db1-d50fdc54f069")
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Check if onboarding is completed
        val sharedPreferences = getSharedPreferences("onboarding", MODE_PRIVATE)
        val onboardingCompleted = sharedPreferences.getBoolean("completed", false)

        if (!onboardingCompleted) {
            // Navigate to the OnboardingFragment
            navController.navigate(R.id.onboardingScreenFragment)
        } else {
            // Navigate to the HomeFragment
            navController.navigate(R.id.homeFragment)
        }
        val countryName: String? = intent.getStringExtra("countryName")
        val flagUrl: String? = intent.getStringExtra("flagUrl")
        Utils.countryName = countryName
        Utils.flagUrl = flagUrl
    }

   fun disconnectFromVpn() {
        try {
            OpenVPNThread.stop()
            updateUI("DISCONNECTED")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun inAppUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this@MainActivity)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener(object : OnSuccessListener<AppUpdateInfo?> {


            override fun onSuccess(result: AppUpdateInfo?) {
                if (result?.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            result,
                            AppUpdateType.IMMEDIATE,
                            this@MainActivity,
                            11
                        )
                    } catch (e: SendIntentException) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 11) {
            Toast.makeText(this, "Start Downloand", Toast.LENGTH_SHORT).show()
            if (resultCode != RESULT_OK) {
                Log.d("Update", "Update failed$resultCode")
            }
        }
        if (resultCode == RESULT_OK) {
            startVpn()
        } else {
            showMessage("Permission Denied", "error")
        }
    }

    fun prepareVpn() {
        imgFlag?.let {
            Glide.with(this)
                .load(selectedCountry?.flagUrl)
                .into(it)
        }
        flagName?.text = selectedCountry?.country
        if (Utility.isOnline(applicationContext)) {
            if (selectedCountry != null) {
                val intent = VpnService.prepare(this)
                Log.v("CHECKSTATE", "start")
                if (intent != null) {
                    startActivityForResult(intent, 1)
                } else startVpn()
            } else {
                showMessage("Please select a server first", "")
            }
        } else {
            showMessage("No Internet Connection", "error")
        }
    }

    fun startVpn() {
        try {
            if (selectedCountry != null) {
                ActiveServer.saveServer(selectedCountry, this@MainActivity)
                OpenVpnApi.startVpn(
                    this,
                    selectedCountry?.ovpn,
                    selectedCountry?.country,
                    selectedCountry?.ovpnUserName,
                    selectedCountry?.ovpnUserPassword
                )

            } else {
                Toast.makeText(this, "No country selected", Toast.LENGTH_SHORT).show()
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }


    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                Objects.requireNonNull(getIntent().getStringExtra("state")).let {
                    if (it != null) {
                        updateUI(it)
                    }
                }
                Objects.requireNonNull(intent.getStringExtra("state"))
                    .let {
                        if (it != null) {
                            Log.v("CHECKSTATE", it)
                        }
                    }
                if (isFirst) {
                    if (ActiveServer.getSavedServer(this@MainActivity).country != null) {
                        selectedCountry = ActiveServer.getSavedServer(this@MainActivity)
                        imgFlag?.let {
                            Glide.with(this@MainActivity)
                                .load(selectedCountry?.flagUrl)
                                .into(it)
                        }
                        flagName?.text = selectedCountry?.country
                    }
                    isFirst = false
                }
                isConnected = true

            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                var duration = intent.getStringExtra("duration")
                var lastPacketReceive = intent.getStringExtra("lastPacketReceive")
                var byteIn = intent.getStringExtra("byteIn")
                var byteOut = intent.getStringExtra("byteOut")
                if (duration == null) duration = "00:00:00"
                if (lastPacketReceive == null) lastPacketReceive = "0"
                if (byteIn == null) byteIn = " "
                if (byteOut == null) byteOut = " "
                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
//        disconnectFromVpn()
    }

   fun checkSelectedCountry() {
        if (selectedCountry == null) {
            updateUI("DISCONNECT")
            showMessage("Please select a server first", "")
        } else {
            prepareVpn()
            updateUI("LOAD")
        }
    }

    companion object {


        var indratech_toto_27640849_fb_reward_id: String? = null
        var indratech_toto_27640849_admob_reward: String? = null
        @JvmField
        var copyright_indratech_official_dont_change_the_value: String? = null
        var selectedCountry: Countries? = null
        @JvmField
        var type: String? = ""
        @JvmField
        var indratech_fast_27640849_admob_id = ""
        @JvmField
        var indratech_fast_27640849_ad_banner_id: String? = ""
        @JvmField
        var admob_interstitial_id: String? = ""
        @JvmField
        var indratech_fast_27640849_aad_native_id = ""
        @JvmField
        var indratech_fast_27640849_fb_native_id: String? = ""
        @JvmField
        var indratech_fast_27640849_fb_interstitial_id: String? = ""
        @JvmField
        var indratech_fast_27640849_all_ads_on_off = false
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
        if (Utils.STATUS != "DISCONNECTED") {
            showMessage("Wait","success")
        } else {
            if (!Utility.isOnline(applicationContext)) {
                showMessage("No Internet Connection", "error")
            } else {
                checkSelectedCountry()
            }
        }
    }

    fun checkRemainingTraffic() {}



    fun disconnectAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Do you want to disconnect?")
        builder.setPositiveButton(
            "Disconnect"
        ) { _, _ ->
            disconnectFromVpn()
            Utils.STATUS = "DISCONNECTED"

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

    fun showMessage(msg: String?, type: String) {

        if (type == "success") {
            Toasty.success(
                this,
                msg + "",
                Toast.LENGTH_SHORT
            ).show()
        } else if (type == "error") {
            Toasty.error(
                this,
                msg + "",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toasty.normal(
                this,
                msg + "",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun updateConnectionStatus(
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

}