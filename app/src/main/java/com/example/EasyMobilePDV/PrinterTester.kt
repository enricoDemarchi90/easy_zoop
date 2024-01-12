package com.example.EasyMobilePDV

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.pax.dal.IDAL
import com.pax.dal.IPrinter
import com.pax.dal.entity.EFontTypeAscii
import com.pax.dal.entity.EFontTypeExtCode
import com.pax.dal.exceptions.PrinterDevException
import com.pax.neptunelite.api.NeptuneLiteUser

class PrinterTester() {

    private var printerTester: PrinterTester? = null
    private var printer: IPrinter? = null
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

    fun getInstance(): PrinterTester? {
        if (printerTester == null) {
            printerTester = PrinterTester()
        }
        return printerTester
    }

    fun init() {
        try {
            printer = getDal()?.printer
            printer!!.init()
            Log.d("PrinterTester","init")
        } catch (e: PrinterDevException) {
            e.printStackTrace()
            Log.e("init", e.toString())
        }
    }

    fun getStatus(): String? {
        return try {
            val status = printer!!.status
            Log.d("PrinterTester","getStatus")
            statusCode2Str(status)
        } catch (e: PrinterDevException) {
            e.printStackTrace()
            Log.e("getStatus", e.toString())
            ""
        }
    }

    fun fontSet(asciiFontType: Any?, cFontType: Any?) {
        try {
            printer!!.fontSet(asciiFontType as EFontTypeAscii?, cFontType as EFontTypeExtCode?)
            Log.d("PrinterTester","fontSet")
        } catch (e: PrinterDevException) {
            e.printStackTrace()
            Log.e("fontSet", e.toString())
        }
    }

    fun spaceSet(wordSpace: Byte, lineSpace: Byte) {
        try {
            printer!!.spaceSet(wordSpace, lineSpace)
            Log.d("PrinterTester","spaceSet")
        } catch (e: PrinterDevException) {
            e.printStackTrace()
            Log.e("spaceSet", e.toString())
        }
    }

    fun printStr(str: String?, charset: String?) {
        try {
            printer!!.printStr(str, charset)
            Log.d("PrinterTester","printStr")
        } catch (e: PrinterDevException) {
            e.printStackTrace()
            Log.e("printStr", e.toString())
        }
    }

    fun step(b: Int) {
        try {
            printer!!.step(b)
            Log.d("PrinterTester","setStep")
        } catch (e: PrinterDevException) {
            e.printStackTrace()
            Log.e("setStep", e.toString())
        }
    }

    fun printBitmap(bitmap: Bitmap) {
        try {
            printer!!.printBitmap(bitmap)
            Log.d("PrinterTester","printBitmap")
        } catch (e: PrinterDevException) {
            e.printStackTrace()
            Log.e("printBitmap", e.toString())
        }
    }

    fun start(): String? {
        return try {
            val res = printer!!.start()
            Log.d("PrinterTester","start")
            statusCode2Str(res)
        } catch (e: PrinterDevException) {
            e.printStackTrace()
            Log.e("start", e.toString())
            ""
        }
    }

    fun leftIndents(indent: Short) {
        try {
            printer!!.leftIndent(indent.toInt())
            Log.d("PrinterTester","leftIndents")
        } catch (e: PrinterDevException) {
            e.printStackTrace()
            Log.e("leftIndent", e.toString())
        }
    }

    fun getDotLine(): Int {
        return try {
            val dotLine = printer!!.dotLine
            Log.d("PrinterTester","getDotLine")
            dotLine
        } catch (e: PrinterDevException) {
            e.printStackTrace()
            Log.e("getDotLine", e.toString())
            -2
        }
    }

    fun setGray(level: Int) {
        try {
            printer!!.setGray(level)
            Log.d("PrinterTester","setGray")
        } catch (e: PrinterDevException) {
            e.printStackTrace()
            Log.e("setGray", e.toString())
        }
    }

    fun setDoubleWidth(isAscDouble: Boolean, isLocalDouble: Boolean) {
        try {
            printer!!.doubleWidth(isAscDouble, isLocalDouble)
            Log.d("PrinterTester","setDoubleWidth")
        } catch (e: PrinterDevException) {
            e.printStackTrace()
            Log.e("doubleWidth", e.toString())
        }
    }

    fun setDoubleHeight(isAscDouble: Boolean, isLocalDouble: Boolean) {
        try {
            printer!!.doubleHeight(isAscDouble, isLocalDouble)
            Log.d("PrinterTester","setDoubleHeight")
        } catch (e: PrinterDevException) {
            e.printStackTrace()
            Log.e("doubleHeight", e.toString())
        }
    }

    fun setInvert(isInvert: Boolean) {
        try {
            printer!!.invert(isInvert)
            Log.d("PrinterTester","setInvert")
        } catch (e: PrinterDevException) {
            e.printStackTrace()
            Log.e("setInvert", e.toString())
        }
    }

    fun cutPaper(mode: Int): String? {
        return try {
            printer!!.cutPaper(mode)
            Log.d("PrinterTester","cutPaper")
            "cut paper successful"
        } catch (e: PrinterDevException) {
            e.printStackTrace()
            Log.e("cutPaper", e.toString())
            e.toString()
        }
    }

    fun statusCode2Str(status: Int): String? {
        var res = ""
        when (status) {
            0 -> res = "Success "
            1 -> res = "Printer is busy "
            2 -> res = "Out of paper "
            3 -> res = "The format of print data packet error "
            4 -> res = "Printer malfunctions "
            8 -> res = "Printer over heats "
            9 -> res = "Printer voltage is too low"
            -16 -> res = "Printing is unfinished "
            -4 -> res = " The printer has not installed font library "
            -2 -> res = "Data package is too long "
            else -> {}
        }
        return res
    }

}

