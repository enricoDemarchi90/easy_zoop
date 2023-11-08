package com.example.smartpos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartpos.ui.theme.SmartPOSTheme
import com.example.smartpos.util.SmartPOSPluginManager
import com.example.smartpos.util.rememberQrBitmapPainter

class MainActivity : ComponentActivity() {
    lateinit var SuperContext: Context
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
        // Add a JavaScript interface
        tsStatic.webView!!.addJavascriptInterface(MainViewModel(),"Android")
        //webView.addJavascriptInterface(WebAppInterface(this, this), "Android")

        // Load the URL
        tsStatic.webView!!.loadUrl("https://easyanalytics.com.br/easymobile/V0/login/?Fonte=rede")

        val preferences = getSharedPreferences("user_preferences", MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("marketplace","0bc5d980777d43fd9aee0f8d215d8735")
        editor.putString("seller","afc4a20ebe09433fac674ad0856ac33c")
        editor.putString("accessKey","57c813b3-2330-4ed9-9ad7-14534ff595cd")
        editor.commit()

        SmartPOSPluginManager().initialize(SuperContext)

    /*  val viewModel = MainViewModel()
        setContent {
            SmartPOSTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }*/
    }

@Composable
fun MainScreen(viewModel: MainViewModel) {
    SmartPOSPluginManager().initialize(LocalContext.current)

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Text(text = "App smartpos teste")
        }
        Row(modifier = Modifier.padding(15.dp)) {
            Button(
                onClick = { viewModel.handle(MainEvent.OnStartLogin) },
                modifier = Modifier.padding(5.dp)
            ) {
                Text(
                    text = "Login",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }


            Button(
                onClick = { viewModel.handle(MainEvent.OnStartCheckKey) },
                modifier = Modifier.padding(5.dp)
            ) {
                Text(
                    text = "Chave",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Row(modifier = Modifier.padding(15.dp)) {
            Button(
                onClick = { viewModel.handle(MainEvent.OnStartPayment) },
                modifier = Modifier.padding(5.dp)
            ) {
                Text(
                    text = "Venda",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Button(
                onClick = { viewModel.handle(MainEvent.OnStartPix) },
                modifier = Modifier.padding(5.dp)
            ) {
                Text(
                    text = "Pix",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Row(modifier = Modifier.padding(15.dp)) {
            Button(
                onClick = { viewModel.handle(MainEvent.OnStartCancellation) },
                modifier = Modifier.padding(5.dp)
            ) {
                Text(
                    text = "Cancelamento",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }


        AnimatedVisibility(visible = viewModel.state.status == Status.MESSAGE) {
            Text(text = viewModel.state.message)
        }

        AnimatedVisibility(visible = viewModel.state.message.contains("insira", true)) {
            CancelButton(handler = viewModel::handle)
        }

        AnimatedVisibility(visible = viewModel.state.status == Status.QR_CODE) {
            Column(
                modifier = Modifier.padding(top = 15.dp, bottom = 15.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = rememberQrBitmapPainter(viewModel.state.qrCode),
                    contentDescription = "Token QRCode",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.size(135.dp),
                    alignment = Alignment.Center
                )
                CancelButton(handler = viewModel::handle)
            }
        }


        AnimatedVisibility(visible = viewModel.state.status == Status.DISPLAY_LIST) {
            Column(
                modifier = Modifier.padding(top = 15.dp, bottom = 15.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AssembleVoidTransactionList(state = viewModel.state, handler = viewModel::handle)
                CancelButton(handler = viewModel::handle)
            }
        }
    }
}

@Composable
fun CancelButton(handler: (MainEvent) -> Unit) {
    OutlinedButton(
        onClick = { handler(MainEvent.OnCancelAction) },
        modifier = Modifier
            .padding(5.dp)
            .background(color = Color.Transparent)
    ) {
        Text(
            text = "Cancelar operação",
            modifier = Modifier.padding(8.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun AssembleVoidTransactionList(state: MainState, handler: (MainEvent) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.80f)
            .padding(top = 15.dp, bottom = 15.dp),
        verticalArrangement = Arrangement.Top
    ) {
        itemsIndexed(items = state.transactionsList) { _, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable { handler(MainEvent.OnSelectTransaction(item)) },
            ) {
                Text(
                    text = "R$ " + item.amount,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .align(Alignment.CenterVertically),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Start
                )

                Text(
                    text = "${item.date} ${item.time}",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1.5f)
                        .padding(end = 10.dp),
                    fontSize = 20.sp,
                    textAlign = TextAlign.End
                )
            }

            Divider(color = Color.Gray)
        }
    }
}

}
