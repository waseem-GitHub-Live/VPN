package com.xilli.stealthnet.Activities

import okhttp3.OkHttpClient
import java.net.ProxySelector
import java.net.URI
//////////////////////////////////////////////////// For Split Tunnnling /////////////////////////////////////
class NetworkInitializer {
    fun configureNetworkWithPACFile() {
        // Replace the URL with the GitHub URL to your proxy.pac file
        val pacUrl = URI("https://raw.githubusercontent.com/waseem-terafort/Proxy/main/proxy.pac")

        // Create a ProxySelector with the PAC file
        val proxySelector = ProxySelector.getDefault()
//        if (proxySelector is android.net.ProxySelector) {
//            proxySelector.setPacFile(pacUrl)
//        }

        // Use the ProxySelector for your HTTP client (e.g., OkHttpClient)
        val httpClient = OkHttpClient.Builder()
            .proxySelector(proxySelector)
            .build()

        // Use httpClient for making HTTP requests, and it will honor the PAC file rules
    }
}