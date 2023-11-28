package com.example.EasyMobilePDV

import com.zoop.pos.collection.VoidTransaction
import android.content.Context
import android.webkit.WebView

data class MainState(
    val status: Status = Status.FINISHED,
    val checkValidationKey: Boolean = false,
    val startPayment: Boolean = false,
    val startLogin: Boolean = false,
    val message: String = "",
    val qrCode: String = "",
    val transactionsList: List<VoidTransaction> = listOf()
)

object tsStatic{
    var webView: WebView? = null
    var SuperContexto: Context? = null
}

enum class Status {
    STARTED,
    MESSAGE,
    QR_CODE,
    DISPLAY_LIST,
    FINISHED
}
