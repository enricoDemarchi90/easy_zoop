package com.example.EasyMobilePDV

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import com.example.EasyMobilePDV.util.SmartPOSPluginManager


class MainActivity : ComponentActivity() {
    lateinit var SuperContext: Context
    private lateinit var paymentManager: PaymentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SuperContext = this
        tsStatic.webView = findViewById(R.id.webView)
        tsStatic.webView!!.settings.javaScriptEnabled = true
        tsStatic.webView!!.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        tsStatic.webView!!.settings.allowFileAccess = true
        tsStatic.webView!!.settings.allowFileAccessFromFileURLs = true
        tsStatic.webView!!.settings.allowUniversalAccessFromFileURLs = true
        tsStatic.webView!!.settings.builtInZoomControls = true
        tsStatic.webView!!.settings.userAgentString = "tsAndroid"
        tsStatic.webView!!.webViewClient = object: WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean{
                if (!url.orEmpty().toLowerCase().contains(".easyanalytics.com.br") &&
                    !url.orEmpty().toLowerCase().contains("easyanalytics.com.br") || url.orEmpty().toLowerCase().contains("hrefopenoutside88")
                ){
                    view?.context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    return true
                } else{
                    if (url != null) {
                        view?.loadUrl(url)
                    }
                    return false
            }
        }
        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler, error: SslError) {
            handler.proceed() // Ignore SSL certificate errors
        }
        override fun onPageFinished(view: WebView?, url: String?) {
            val cookieManager = CookieManager.getInstance()
            val tsL = CookieManager.getInstance().getCookie(url)
                if (tsL != null) {
                    val temp = tsL.split(";")
                    for (ar1 in temp) {
                        if (ar1.contains("caTK")) {
                            val temp1 = ar1.split("=")
                            //tsStatic.AuthCookie = temp1[1]
                        }
                    }
                }
            cookieManager.setAcceptCookie(true)
            cookieManager.flush()
        }
            @Suppress("deprecation")
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                if ((errorCode in 400 until 600) || errorCode == -2) {
                    //tsStatic.webView.stopLoading()
                    //tsStatic.webView.loadUrl("file:///android_asset/conexao.html")
                }
                super.onReceivedError(view, errorCode, description, failingUrl)
                Toast.makeText(
                    SuperContext,
                    "Your Internet Connection May not be active Or $description",
                    Toast.LENGTH_LONG
                ).show()
            }

            @androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.M)
            override fun onReceivedError(
                view: WebView?,
                req: WebResourceRequest?,
                rerr: WebResourceError?
            ) {
                val errorCode = rerr?.errorCode ?: 0
                if ((errorCode in 400 until 600) || errorCode == -2) {
                    //tsStatic.webView.stopLoading()
                    //tsStatic.webView.loadUrl("file:///android_asset/conexao.html")
                }
                // super.onReceivedError(view, errorCode, req?.method, rerr?.description ?: "")
                Toast.makeText(
                SuperContext,
                 "Your Internet Connection May not be active Or ${rerr?.description}",
                  Toast.LENGTH_LONG
                 ).show()
            }
        }
        val preferences = getSharedPreferences("user_preferences", MODE_PRIVATE)
        val editor = preferences.edit()
        //editor.putString("MarketplaceId","0bc5d980777d43fd9aee0f8d215d8735")
        //editor.putString("SellerId","43f859c8ac5949b5ba5e25c934f5019a")
        //editor.putString("Terminal","1493572076")
        //editor.putString("AccessKey","cfb1ccad-80fd-45e9-9b31-716be7913df7")
       /* editor.putString("MarketplaceId","")
        editor.putString("SellerId","")
        editor.putString("Terminal","")
        editor.putString("AccessKey","")*/
        editor.commit()
        SmartPOSPluginManager().initialize(SuperContext)
        tsStatic.webView!!.addJavascriptInterface(WebAppInterface(SuperContext), "Android")
        tsStatic.webView!!.loadUrl("https://easyanalytics.com.br/easymobile/V0/login/?Fonte=paytime")
    }

    fun processActivity(){
        val intent = Intent(this, PaymentActivity::class.java)
        this?.startActivityForResult(intent,99)
        paymentManager = PaymentManager()
        //paymentManager.startUpdatingText()
        //this.updatePayment(PaymentStatus.START,"Iniciando Pagamento")
    }

    fun updatePayment(type: PaymentStatus, message: String){
        runOnUiThread(Runnable {
            paymentManager.updatePayment(type,message)
        })
    }

    fun closeActivity(){
        paymentManager.closeActivity()
    }

}
