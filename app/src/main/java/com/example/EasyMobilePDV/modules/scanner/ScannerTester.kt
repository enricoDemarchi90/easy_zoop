package com.example.EasyMobilePDV.modules.scanner

import android.content.Context
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.pax.dal.IDAL
import com.pax.dal.IScanner
import com.pax.dal.IScanner.IScanListener
import com.pax.dal.entity.EScannerType
import com.pax.dal.entity.ScanResult
import com.pax.neptunelite.api.NeptuneLiteUser

class ScannerTester() {
    private var scanner: IScanner? = null
    private var dal: IDAL? = null
    var context: Context? = null

    constructor(c: Context) : this() {
        this.context = c
    }

    fun getDal(): IDAL? {
        if (dal == null) {
            try {
                val start = System.currentTimeMillis()
                dal = NeptuneLiteUser.getInstance().getDal(context)
                Log.i("Test", "get dal cost:" + (System.currentTimeMillis() - start) + " ms")
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "error occurred,DAL is null.", Toast.LENGTH_LONG).show()
            }
        }
        return dal
    }

    fun scan(timeout: Int, callback: (String?)-> Unit) {
        scanner = getDal()?.getScanner(EScannerType.REAR)
        scanner!!.open()
        scanner!!.setTimeOut(timeout)
        scanner!!.setContinuousTimes(1)
        scanner!!.setContinuousInterval(1000)
        scanner!!.start(object : IScanListener {

            override fun onRead(result: ScanResult) {
                /*val message: Message = Message.obtain()
                message.what = 0
                message.obj = result.getContent()*/
                val codBar: String = result.content
                callback(codBar)
                Log.d("teste ", result.content)
            }
            override fun onFinish() {
                close()
            }

            override fun onCancel() {
                close()
            }
        })
        Log.d("SCANNER", "start")
    }

    fun close() {
        scanner!!.close()
    }

    fun setTimeOut(timeout: Int) {
        scanner!!.setTimeOut(timeout)
    }

    fun setFlashOn(isOn: Boolean): Boolean {
        val result = scanner!!.setFlashOn(isOn)
        if (result) {
            Log.d("SCANNER", "setFlashOn")
        } else {
            Log.d("SCANNER", "set flash on failed")
        }
        return result
    }

    fun setScannerType(type: Int): Boolean {
        val result = scanner!!.setScannerType(type)
        if (result) {
            Log.d("SCANNER", "setScannerType")
        } else {
            Log.d("SCANNER", "set scanner type failed")
        }
        return result
    }

}