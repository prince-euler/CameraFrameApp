package com.example.cameraframeapp

import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

object DoesHaveInternetAcess {
    const val port = 53
    const val timeout = 1500
    fun execute(): Boolean {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress("8.8.8.8", port), timeout)
            socket.close()
            true
        }catch (e : IOException){
            false
        }
    }
}