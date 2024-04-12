package com.example.cameraframeapp

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

const val TAG = "C-Manager"

class NetworkUtil(context: Context) : LiveData<NetworkState>() {

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableSet<Network> = HashSet()
    var downSpeed = 0
    object Variables {
        var isNetworkConnected: Boolean by Delegates.observable(false) { property, oldValue, newValue ->
            Log.i("Network connectivity", "$newValue")
        }
    }

    private fun checkValidNetworks() {
        if(downSpeed == 0) {
            postValue(NetworkState.NOINTERNET)
        }
        else if(downSpeed < 20){
            postValue(NetworkState.LOWINTERNET)
        }
        else{
            postValue(NetworkState.OKINTERNET)
        }
    }

    override fun onActive() {
        networkCallback = createNetworkCallback()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onInactive() {
        cm.unregisterNetworkCallback(networkCallback)
    }

    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val networkCapabilities = cm.getNetworkCapabilities(network)
            val hasInternetCapability = networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET)
            CheckInternet.Variables.isNetworkConnected = true
            if (hasInternetCapability == true) {
                CoroutineScope(Dispatchers.IO).launch {
                    val hasInternet = DoesHaveInternetAcess.execute()
                    if(hasInternet){
                        withContext(Dispatchers.Main){
                            Log.d(TAG, "onAvailable: adding network. $network")
                            downSpeed = networkCapabilities.linkDownstreamBandwidthKbps;
                            Log.v("downspeed",downSpeed.toString())
                            validNetworks.add(network)
                            checkValidNetworks()
                        }
                    }
                }
            }
        }

        override fun onLost(network: Network) {
            Log.d(TAG, "onLost: $network")
            CheckInternet.Variables.isNetworkConnected = false
            validNetworks.remove(network)
            downSpeed=0
            checkValidNetworks()
        }
    }
}


enum class NetworkState{
    NOINTERNET,
    LOWINTERNET,
    OKINTERNET
}
