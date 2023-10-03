package com.xilli.stealthnet.Activities

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender.SendIntentException
import android.net.VpnService
import android.os.Bundle
import android.os.PersistableBundle
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.SkuType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.bumptech.glide.Glide
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.OnSuccessListener
import com.xilli.stealthnet.Config
import com.xilli.stealthnet.R
import com.xilli.stealthnet.Utils.ActiveServer
import com.xilli.stealthnet.model.Countries
import top.oneconnectapi.app.OpenVpnApi
import top.oneconnectapi.app.core.OpenVPNThread
import java.util.Arrays
import java.util.Locale
import java.util.Objects

class MainActivity : ContentsActivity(), PurchasesUpdatedListener, BillingClientStateListener {
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

    private fun connectToBillingService() {
        if (!billingClient!!.isReady) {
            billingClient!!.startConnection(this)
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
        val result = billingClient!!.queryPurchases(SkuType.SUBS)
        val purchases = result.purchasesList
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
        billingClient!!.querySkuDetailsAsync(
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
//        MobileAds.initialize(this) { }
        connectToBillingService()
        val intent = intent
        if (getIntent().extras != null) {
            selectedCountry = getIntent().extras!!.getParcelable("c")
            updateUI("LOAD")
            if (!Utility.isOnline(applicationContext)) {
                showMessage("No Internet Connection", "error")
            } else {
                showInterstitialAndConnect()
            }
        } else {
            if (selectedCountry != null) {
                updateUI("CONNECTED")
                Glide.with(this)
                    .load(selectedCountry!!.flagUrl)
                    .into(imgFlag!!)
                flagName!!.text = selectedCountry!!.country
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
//        if (type == "ad") {
//            val requestBuilder = RequestConfiguration.Builder()
//            MobileAds.setRequestConfiguration(requestBuilder.build())
//        } else {
//            AdSettings.setIntegrationErrorMode(AdSettings.IntegrationErrorMode.INTEGRATION_ERROR_CALLBACK_MODE)
//            AudienceNetworkAds.initialize(this)
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        inAppUpdate()
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
    }

    override fun disconnectFromVpn() {
        try {
            OpenVPNThread.stop()
            updateUI("DISCONNECTED")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun checkRemainingTraffic() {}
    override val layoutRes: Int
        protected get() = R.layout.activity_main

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

    public override fun prepareVpn() {
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

    protected fun startVpn() {
        try {
            ActiveServer.saveServer(selectedCountry, this@MainActivity)
            OpenVpnApi.startVpn(
                this,
                selectedCountry?.ovpn,
                selectedCountry?.country,
                selectedCountry?.ovpnUserName,
                selectedCountry?.ovpnUserPassword
            )
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                Objects.requireNonNull(getIntent().getStringExtra("state"))?.let { updateUI(it) }
                Objects.requireNonNull(intent.getStringExtra("state"))
                    ?.let { Log.v("CHECKSTATE", it) }
                if (isFirst) {
                    if (ActiveServer.getSavedServer(this@MainActivity).country != null) {
                        selectedCountry = ActiveServer.getSavedServer(this@MainActivity)
                        Glide.with(this@MainActivity)
                            .load(selectedCountry?.flagUrl)
                            .into(imgFlag!!)
                        flagName!!.text = selectedCountry?.country
                    }
                    isFirst = false
                }
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

    public override fun checkSelectedCountry() {
        if (selectedCountry == null) {
            updateUI("DISCONNECT")
            showMessage("Please select a server first", "")
        } else {
            showInterstitialAndConnect()
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
}