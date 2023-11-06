package com.example.smartpos.util

import android.content.Context
import com.zoop.pos.Zoop
import com.zoop.pos.plugin.DashboardConfirmationResponse
import com.zoop.pos.type.Environment
import com.zoop.pos.type.LogLevel
import com.zoop.pos.plugin.smartpos.SmartPOSPlugin

class SmartPOSPluginManager(private val credentials: DashboardConfirmationResponse.Credentials? = null) {

    fun initialize(context: Context) {
        Zoop.initialize(context) {
            if (credentials != null) {
                credentials {
                    marketplace = credentials.marketplace
                    seller = credentials.seller
                    accessKey = credentials.accessKey
                }
            }
        }
        Zoop.setEnvironment(Environment.Production)
        Zoop.setLogLevel(LogLevel.Trace)
        Zoop.setStrict(false)
        Zoop.setTimeout(15 * 1000L)
        Zoop.findPlugin<SmartPOSPlugin>() ?: Zoop.make<SmartPOSPlugin>().run(Zoop::plug)
    }

    fun terminate() {
        Zoop.findPlugin<SmartPOSPlugin>()?.run(Zoop::unplug)
    }
}