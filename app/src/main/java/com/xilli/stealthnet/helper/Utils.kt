package com.xilli.stealthnet.helper

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.facebook.ads.NativeAdLayout
import com.xilli.stealthnet.R
import com.xilli.stealthnet.Utils.Constants
import com.xilli.stealthnet.model.Countries
import com.xilli.stealthnet.speed.Speed
import com.xilli.stealthnet.ui.toolside
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface

object Utils: toolside() {
    private var mLastRxBytes: Long = 0
    private var mLastTxBytes: Long = 0
    private var mLastTime: Long = 0
    private var mSpeed: Speed? = null
    private var appContext: Context? = null
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
    var connectBtnTextView: ImageView? = null
    var connectionStateTextView: TextView? = null
    var rcvFree: RecyclerView? = null
    var footer: RelativeLayout? = null
    lateinit var sharedPreferences: SharedPreferences
    var countryName: String? = null
    var flagUrl: String? = null
    @JvmField
    var imgFlag: ImageView? = null
    var isConnected = false

    @JvmField
    var flagName: TextView? = null



    var STATUS: String? = "DISCONNECTED"
    fun showIP(context: Context, textView: TextView) {

            val queue = Volley.newRequestQueue(context)
            val urlip = "https://checkip.amazonaws.com/"

            val stringRequest = StringRequest(
                Request.Method.GET, urlip,
                Response.Listener<String> { response ->
                    textView.text = response
                },
                Response.ErrorListener { _ ->
                    textView.text = getIpv4HostAddress()
                }
            )
            queue.add(stringRequest)

    }
     fun loadServers(): List<Countries> {
        val servers = ArrayList<Countries>()
        try {
            val jsonArray = JSONArray(Constants.FREE_SERVERS)
            for (i in 0 until jsonArray.length()) {
                val `object` = jsonArray[i] as JSONObject
                servers.add(
                    Countries(
                        `object`.getString("serverName"),
                        `object`.getString("flag_url"),
                        `object`.getString("ovpnConfiguration"),
                        `object`.getString("vpnUserName"),
                        `object`.getString("vpnPassword")
                    )
                )
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return servers
    }
     fun loadServersvip(): List<Countries> {
        val servers = ArrayList<Countries>()
        try {
            val jsonArray = JSONArray(Constants.PREMIUM_SERVERS)
            for (i in 0 until jsonArray.length()) {
                val `object` = jsonArray[i] as JSONObject
                val country = Countries( // Create a new Countries object
                    `object`.getString("serverName"),
                    `object`.getString("flag_url"),
                    `object`.getString("ovpnConfiguration"),
                    `object`.getString("vpnUserName"),
                    `object`.getString("vpnPassword")
                )
                country.isPremium = true // Set the isPremium property for premium servers
                servers.add(country)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return servers
    }
    fun initialize(context: Context) {
        appContext = context
    }
    private fun getIpv4HostAddress(): CharSequence? {
        NetworkInterface.getNetworkInterfaces()?.toList()?.map { networkInterface ->
            networkInterface.inetAddresses?.toList()?.find {
                !it.isLoopbackAddress && it is Inet4Address
            }?.let { return it.hostAddress }
        }
        return ""
    }

    fun updateUI(status:String) {
        when (status) {
            "CONNECTED" -> {
                STATUS = "CONNECTED"
                textDownloading?.visibility = View.VISIBLE
                textUploading?.visibility = View.VISIBLE
                connectBtnTextView?.isEnabled = true
                connectionStateTextView?.setText(R.string.connected)
                timerTextView?.visibility = View.GONE
                hideConnectProgress()
                tvIpAddress?.let { appContext?.let { it1 -> showIP(it1,it) } }
                connectBtnTextView?.visibility = View.VISIBLE
                tvConnectionStatus?.text = "Selected"
                lottieAnimationView?.visibility = View.GONE
                isConnected=true
            }
            "AUTH" -> {
                STATUS = "AUTHENTICATION"
                connectBtnTextView?.visibility = View.VISIBLE
                lottieAnimationView?.visibility = View.VISIBLE


                connectionStateTextView?.setText(R.string.auth)
                connectBtnTextView?.isEnabled = true
                timerTextView?.visibility = View.GONE
            }
            "WAIT" -> {
                STATUS = "WAITING"
                connectBtnTextView?.visibility = View.VISIBLE
                lottieAnimationView?.visibility = View.VISIBLE


                connectionStateTextView?.setText(R.string.wait)
                connectBtnTextView?.isEnabled = true
                timerTextView?.visibility = View.GONE
            }
            "RECONNECTING" -> {
                STATUS = "RECONNECTING"
                connectBtnTextView?.visibility = View.VISIBLE
                lottieAnimationView?.visibility = View.VISIBLE


                connectionStateTextView?.setText(R.string.recon)
                connectBtnTextView?.isEnabled = true
                timerTextView?.visibility = View.GONE
            }
            "LOAD" -> {
                STATUS = "LOAD"
                connectBtnTextView?.visibility = View.VISIBLE
                lottieAnimationView?.visibility = View.VISIBLE


                connectionStateTextView?.setText(R.string.connecting)
                connectBtnTextView?.isEnabled = true
                timerTextView?.visibility = View.GONE
            }
            "ASSIGN_IP" -> {
                STATUS = "LOAD"
                connectBtnTextView?.visibility = View.VISIBLE
                lottieAnimationView?.visibility = View.VISIBLE


                connectionStateTextView?.setText(R.string.assign_ip)
                connectBtnTextView?.isEnabled = true
                timerTextView?.visibility = View.GONE
            }
            "GET_CONFIG" -> {
                STATUS = "LOAD"
                connectBtnTextView?.visibility = View.VISIBLE
                lottieAnimationView?.visibility = View.VISIBLE


                connectionStateTextView?.setText(R.string.config)
                connectBtnTextView?.isEnabled = true
                timerTextView?.visibility = View.GONE
            }
            "USERPAUSE" -> {
                STATUS = "DISCONNECTED"
                tvConnectionStatus?.text = "Not Selected"
                ivConnectionStatusImage?.setImageResource(R.drawable.ic_dot)


                tvConnectionStatus?.text = "Not Selected"
                connectionStateTextView?.setText(R.string.paused)
                ivConnectionStatusImage?.setImageResource(R.drawable.ic_dot)
            }
            "NONETWORK" -> {
                STATUS = "DISCONNECTED"
                tvConnectionStatus?.text = "Not Selected"
                ivConnectionStatusImage?.setImageResource(R.drawable.ic_dot)
                tvIpAddress?.let { appContext?.let { it1 -> showIP(it1,it) } }


                tvConnectionStatus?.text = "Not Selected"
                connectionStateTextView?.setText(R.string.nonetwork)
                ivConnectionStatusImage?.setImageResource(R.drawable.ic_dot)
            }
            "DISCONNECTED" -> {
                STATUS = "DISCONNECTED"
                tvConnectionStatus?.text = "Not Selected"
                ivConnectionStatusImage?.setImageResource(R.drawable.ic_dot)
                timerTextView?.visibility = View.INVISIBLE
                hideConnectProgress()
                tvIpAddress?.let { appContext?.let { it1 -> showIP(it1,it) } }


                tvConnectionStatus?.text = "Not Selected"
                connectionStateTextView?.setText(R.string.disconnected)
                ivConnectionStatusImage?.setImageResource(R.drawable.ic_dot)
            }
        }
    }
    fun hideConnectProgress() {
        connectionStateTextView?.visibility = View.VISIBLE
    }
    override val layoutRes: Int
        get() = TODO("Not yet implemented")
    fun isVpnConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        return networkInfo != null && networkInfo.isConnected
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
    val isVpnActiveFlow = callbackFlow {
        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager == null) {
            channel.close(IllegalStateException("connectivity manager is null"))
            return@callbackFlow
        } else {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    channel.trySend(true)
                }

                override fun onLost(network: Network) {
                    channel.trySend(false)
                }
            }
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                    .build(),
                callback
            )
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }
    }
}