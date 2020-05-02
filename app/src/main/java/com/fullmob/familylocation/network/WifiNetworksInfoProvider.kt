package com.fullmob.familylocation.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class WifiNetworks(val networks: MutableList<WifiNetwork> = ArrayList()) {
    fun toJson(): JSONArray {
        val jArray = JSONArray()
        networks.forEach { jArray.put(it.toJson()) }
        return jArray
    }
}

class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val signal: Int,
    val frequency: Int,
    val channel: Int
) {
    fun toJson(): JSONObject {
        val jObj = JSONObject()
        jObj.put("ssid", ssid)
        jObj.put("bssid", bssid)
        jObj.put("signal", signal)
        jObj.put("frequency", frequency)
        jObj.put("channel", channel)
        return jObj
    }
}

class WifiNetworksInfoProvider(private val ctx: Context) {

    private lateinit var receiver: BroadcastReceiver

    suspend fun getNetworks(): WifiNetworks = suspendCoroutine { cont ->
        val wifiManager = ctx.applicationContext.getSystemService(WIFI_SERVICE) as? WifiManager

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                ctx.unregisterReceiver(receiver)
                if (intent?.action.equals(SCAN_RESULTS_AVAILABLE_ACTION)) {
                    cont.resume(processResults(wifiManager!!))
                }
            }
        }
        ctx.registerReceiver(receiver, IntentFilter(SCAN_RESULTS_AVAILABLE_ACTION))
        if (wifiManager?.startScan() == false) {
            cont.resume(processResults(wifiManager!!))
        }
    }

    private fun processResults(wifiManager: WifiManager): WifiNetworks {
        val response = WifiNetworks()
        if (wifiManager?.scanResults?.size ?: 0 > 0) {
            wifiManager?.scanResults?.forEach { result ->
                response.networks.add(
                    WifiNetwork(
                        ssid = result.SSID,
                        bssid = result.BSSID,
                        signal = result.level,
                        frequency = result.frequency,
                        channel = convertFrequencyToChannel(result.frequency)
                    )
                )
            }
        }
        return response
    }

    fun convertFrequencyToChannel(freq: Int): Int {
        return if (freq >= 2412 && freq <= 2484) {
            (freq - 2412) / 5 + 1
        } else if (freq >= 5170 && freq <= 5825) {
            (freq - 5170) / 5 + 34
        } else {
            -1
        }
    }

}