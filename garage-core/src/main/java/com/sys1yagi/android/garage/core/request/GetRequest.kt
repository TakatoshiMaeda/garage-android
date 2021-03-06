package com.sys1yagi.android.garage.core.request

import android.util.Log
import com.sys1yagi.android.garage.core.GarageClient
import com.sys1yagi.android.garage.core.config.RequestConfiguration
import okhttp3.Call
import okhttp3.Request

open class GetRequest(private val path: Path, private val config: RequestConfiguration, val requestPreparing: (Request.Builder) -> Request.Builder = { it }) : GarageRequest() {

    var parameter: Parameter? = null

    override fun url(): String {
        with(config) {
            return ("${scheme.value}://${endpoint}:${customPort ?: scheme.port}/${path.to()}" + (parameter?.let { "?${it.build()}" } ?: "")).apply {
                if (config.isDebugMode) {
                    Log.d(GarageClient.TAG, "GET:$this")
                }
            }
        }
    }

    override fun newCall(requestProcessing: (Request.Builder) -> Request.Builder): Call {
        return config.client.newCall(
                requestProcessing(Request.Builder())
                        .url(url())
                        .build())
    }

    override fun execute(success: (GarageResponse) -> Unit, failed: (GarageError) -> Unit) {
        val call = newCall(requestPreparing)
        try {
            val response = call.execute()
            success.invoke(GarageResponse(call, response))
        } catch(e: Exception) {
            failed.invoke(
                    GarageError(e).apply {
                        this.call = call
                        if (config.isDebugMode) {
                            e.printStackTrace()
                        }
                    }
            )
        }
    }

    override fun requestTime() = System.currentTimeMillis()
}
