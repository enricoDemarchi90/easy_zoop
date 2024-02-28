package com.example.EasyMobilePDV

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.util.EnumMap

class PaymentActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        paymentMessageTextView = findViewById(R.id.payment_message)
        paymentMessageSubTextView = findViewById(R.id.payment_message_sub)
        paymentMessageCloseTextView = findViewById(R.id.payment_close)
        paymentMessageIconView = findViewById(R.id.payment_icon)
        paymentMessagePasswordTextView = findViewById(R.id.payment_password)
        paymentMessageQRCodeView = findViewById(R.id.payment_qrcode)
        paymentMessageQRCodeViewLink = findViewById(R.id.payment_qrcode_link)
        paymentMessageQRCodeViewLinkText = findViewById(R.id.payment_qrcode_link_text)
        paymentMessageCloseTextView.setOnClickListener {
            finish()
        }
        paymentContext = this

    }

    companion object {
        private lateinit var paymentMessageTextView: TextView
        private lateinit var paymentMessageSubTextView: TextView
        private lateinit var paymentMessageCloseTextView: RelativeLayout
        private lateinit var paymentMessageIconView: ImageView
        private lateinit var paymentMessagePasswordTextView: TextView
        private lateinit var paymentMessageQRCodeView: ImageView
        private lateinit var paymentMessageQRCodeViewLink: LinearLayout
        private lateinit var paymentMessageQRCodeViewLinkText: TextView
        private lateinit var paymentContext: PaymentActivity


        fun close(){
            paymentContext.finish()
        }

        fun updatePayment(type: PaymentStatus, message: String) {

            when (type) {
                PaymentStatus.PIX -> {
                    paymentMessageTextView.text = "PIX"
                    paymentMessageSubTextView.text = "Você tem 2 minutos!"
                    paymentMessageIconView.visibility = View.GONE
                    paymentMessagePasswordTextView.visibility = View.GONE
                    val bitmap = gerarQRCode(message, 512, 512)
                    paymentMessageQRCodeView.setImageBitmap(bitmap)
                    paymentMessageQRCodeView.visibility = View.VISIBLE
                    paymentMessageQRCodeViewLink.visibility = View.VISIBLE
                    paymentMessageQRCodeViewLinkText.visibility = View.VISIBLE
                    paymentMessageQRCodeViewLinkText.text = message
                }
                PaymentStatus.START -> {
                    paymentMessageTextView.text = message
                    paymentMessageSubTextView.text = "Aguarde"
                    paymentMessageIconView.visibility = View.VISIBLE
                    paymentMessageIconView.setImageResource(R.drawable.icon2)
                    paymentMessagePasswordTextView.visibility = View.GONE
                    paymentMessageQRCodeView.visibility = View.GONE
                    paymentMessageQRCodeViewLink.visibility = View.GONE
                    paymentMessageQRCodeViewLinkText.visibility = View.GONE


                }

                PaymentStatus.PROCESSING -> {
                    paymentMessageTextView.text = message
                    paymentMessageSubTextView.text = "Processando Informações"
                    paymentMessageIconView.visibility = View.VISIBLE
                    paymentMessagePasswordTextView.visibility = View.GONE
                    paymentMessageQRCodeView.visibility = View.GONE
                    paymentMessageQRCodeViewLink.visibility = View.GONE
                    paymentMessageQRCodeViewLinkText.visibility = View.GONE

                }
                PaymentStatus.SUCCESS -> {
                    paymentMessageTextView.text = message
                    paymentMessageSubTextView.text = "Aguarde a Impressão"
                    paymentMessageIconView.visibility = View.VISIBLE
                    paymentMessageIconView.setImageResource(R.drawable.icon3)
                    paymentMessagePasswordTextView.visibility = View.GONE
                    paymentMessageQRCodeView.visibility = View.GONE
                    paymentMessageQRCodeViewLink.visibility = View.GONE
                    paymentMessageQRCodeViewLinkText.visibility = View.GONE

                }
                PaymentStatus.CARD -> {
                    paymentMessageTextView.text = message
                    paymentMessageSubTextView.text = "Insira ou Aproxime"
                    paymentMessageIconView.visibility = View.VISIBLE
                    paymentMessageIconView.setImageResource(R.drawable.icon1)
                    paymentMessagePasswordTextView.visibility = View.GONE
                    paymentMessageQRCodeView.visibility = View.GONE
                    paymentMessageQRCodeViewLink.visibility = View.GONE
                    paymentMessageQRCodeViewLinkText.visibility = View.GONE
                }
                PaymentStatus.PASSWORD -> {
                    paymentMessagePasswordTextView.visibility = View.VISIBLE
                    paymentMessageTextView.text = message
                    paymentMessageSubTextView.text = "Depois é só confirmar"
                    paymentMessageIconView.visibility = View.GONE
                    paymentMessageQRCodeView.visibility = View.GONE
                    paymentMessageQRCodeViewLink.visibility = View.GONE
                    paymentMessageQRCodeViewLinkText.visibility = View.GONE

                }
                PaymentStatus.PINPAD -> {
                    paymentMessagePasswordTextView.visibility = View.VISIBLE
                    if (message.contains("add")) {
                        paymentMessagePasswordTextView.text =
                            paymentMessagePasswordTextView.text.toString() + "*"
                    } else if (message.contains("del")) {
                        paymentMessagePasswordTextView.text =
                            paymentMessagePasswordTextView.text.toString().dropLast(1)
                    }
                    paymentMessageTextView.text = "Insira sua Senha"
                    paymentMessageSubTextView.text = "Depois é só confirmar"
                    paymentMessageIconView.visibility = View.GONE
                    paymentMessageQRCodeView.visibility = View.GONE
                    paymentMessageQRCodeViewLink.visibility = View.GONE
                    paymentMessageQRCodeViewLinkText.visibility = View.GONE
                }

                PaymentStatus.ERROR -> {
                    paymentMessageTextView.text = message
                    paymentMessageSubTextView.text = "Confira a mensagem"
                    paymentMessageIconView.visibility = View.VISIBLE
                    paymentMessageIconView.setImageResource(R.drawable.icon4)
                    paymentMessagePasswordTextView.visibility = View.GONE
                    paymentMessageQRCodeView.visibility = View.GONE
                    paymentMessageQRCodeViewLink.visibility = View.GONE
                    paymentMessageQRCodeViewLinkText.visibility = View.GONE
                }

                PaymentStatus.MESSAGE -> {
                    paymentMessageTextView.text = message
                    paymentMessageSubTextView.text = "Informativo"
                    paymentMessageIconView.visibility = View.GONE
                    paymentMessagePasswordTextView.visibility = View.GONE
                    paymentMessageQRCodeView.visibility = View.GONE
                    paymentMessageQRCodeViewLink.visibility = View.GONE
                    paymentMessageQRCodeViewLinkText.visibility = View.GONE
                }
            }


        }

        @Throws(WriterException::class)
        private fun gerarQRCode(conteudo: String, largura: Int, altura: Int): Bitmap {
            val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 1
            val writer = QRCodeWriter()
            val bitMatrix: BitMatrix = writer.encode(conteudo, BarcodeFormat.QR_CODE, largura, altura, hints)
            val pixels = IntArray(largura * altura)
            for (y in 0 until altura) {
                for (x in 0 until largura) {
                    pixels[y * largura + x] = if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                }
            }

            val bitmap = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, largura, 0, 0, largura, altura)
            return bitmap
        }
    }

}

enum class PaymentStatus {
    START,
    PROCESSING,
    SUCCESS,
    CARD,
    PIX,
    PINPAD,
    PASSWORD,
    ERROR,
    MESSAGE
}

class PaymentManager {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateTextRunnable: Runnable

    fun startUpdatingText() {
        var i = 0
        updateTextRunnable = object : Runnable {
            override fun run() {
                when (i) {
                    0 -> {
                        updatePayment(PaymentStatus.START, "Iniciando Pagamento")
                        handler.postDelayed(this, 5000) // 5000 milissegundos = 5 segundos

                    }

                    1 -> {
                        updatePayment(
                            PaymentStatus.PIX,
                            "00020126580014BR.GOV.BCB.PIX013612ac4d94-9fdf-481e-8f3d-bf22c7da413a52040000530398654041.005802BR5918Lidiani Moro Isaac6009SAO PAULO621405102dsgcLSM5v630428D3"
                        )
                        handler.postDelayed(this, 20000) // 5000 milissegundos = 5 segundos

                    }
//                    1 -> {
//                        updatePayment(PaymentStatus.CARD, "Insira seu Cartão")
//                        handler.postDelayed(this, 5000) // 5000 milissegundos = 5 segundos
//
//                    }
//                    2 -> {
//                        updatePayment(PaymentStatus.PASSWORD, "Informe sua Senha")
//                        handler.postDelayed(this, 5000) // 5000 milissegundos = 5 segundos
//
//                    }
//                    3 -> {
//                        updatePayment(PaymentStatus.PINPAD, "add")
//                        handler.postDelayed(this, 5000) // 5000 milissegundos = 5 segundos
//
//                    }
//                    4 -> {
//                        updatePayment(PaymentStatus.PINPAD, "add")
//                        handler.postDelayed(this, 5000) // 5000 milissegundos = 5 segundos
//
//                    }
//                    5 -> {
//                        updatePayment(PaymentStatus.PINPAD, "add")
//                        handler.postDelayed(this, 5000) // 5000 milissegundos = 5 segundos
//
//                    }
//                    6 -> {
//                        updatePayment(PaymentStatus.PINPAD, "del")
//                        handler.postDelayed(this, 5000) // 5000 milissegundos = 5 segundos
//
//                    }
//                    7 -> {
//                        updatePayment(PaymentStatus.SUCCESS, "Pagamento Confirmado")
//                        handler.postDelayed(this, 5000) // 5000 milissegundos = 5 segundos
//
//                    }
//                    8 -> {
//                        updatePayment(PaymentStatus.ERROR, "Não foi possível processar")
//                        handler.postDelayed(this, 5000) // 5000 milissegundos = 5 segundos
//                    }
                }
                i++;
            }
        }

        // Inicia a atualização do texto
        handler.postDelayed(updateTextRunnable, 5000)
    }

    fun stopUpdatingText() {
        // Para o handler quando a atividade for destruída
        handler.removeCallbacks(updateTextRunnable)
    }

    fun updatePayment(type: PaymentStatus, message: String) {
        PaymentActivity.updatePayment(type, message)
    }

    fun closeActivity(){
        PaymentActivity.close()
    }


}