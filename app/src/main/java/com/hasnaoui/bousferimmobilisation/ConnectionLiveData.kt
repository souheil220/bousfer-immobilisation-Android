package com.hasnaoui.bousferimmobilisation

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData

class ConnectionLiveData(context:Context) : LiveData<Boolean>() {
    private val TAG = "C-Manager"
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks:MutableSet<Network> = HashSet()

    private fun checkValidNetworks(){
        postValue(validNetworks.size>0)
    }

    override fun onActive() {
        networkCallback = createNetworkCallback()
        val networkRequest = NetworkRequest.Builder().addCapability(NET_CAPABILITY_INTERNET).build()
        cm.registerNetworkCallback(networkRequest,networkCallback)
    }

    override fun onInactive() {
        cm.unregisterNetworkCallback(networkCallback)
    }

    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: Network) {
            Log.e(TAG,"onAvailable: $network")
            val networkCapabilities = cm.getNetworkCapabilities(network)
            val isInternet = networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET)
            Log.d(TAG,"onAvailable $network , $isInternet")
            if(isInternet == true){
                validNetworks.add(network)
            }
            checkValidNetworks()
        }

        override fun onLost(network: Network) {
           Log.e(TAG,"onLost: $network")
            validNetworks.remove(network)
            checkValidNetworks()
        }
    }
}