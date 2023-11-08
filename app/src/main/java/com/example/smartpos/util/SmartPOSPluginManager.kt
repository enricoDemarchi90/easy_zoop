package com.example.smartpos.util

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import com.zoop.pos.Zoop
import com.zoop.pos.plugin.DashboardConfirmationResponse
import com.zoop.pos.type.Environment
import com.zoop.pos.type.LogLevel
import com.zoop.pos.plugin.smartpos.SmartPOSPlugin

class SmartPOSPluginManager(private val credentials: DashboardConfirmationResponse.Credentials? = null) {
    var spMarketplace: String? = ""
    var spSeller: String? = ""
    var spAcessKey: String? = ""
    fun initialize(context: Context) {
        Zoop.initialize(context) {
            val preferences = context.getSharedPreferences("user_preferences", ComponentActivity.MODE_PRIVATE)
            spMarketplace = preferences.getString("marketplace","")
            spSeller = preferences.getString("seller","")
            spAcessKey = preferences.getString("accessKey","")
            credentials{
                marketplace = spMarketplace.toString()
                seller = spSeller.toString()
                accessKey = spAcessKey.toString()
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