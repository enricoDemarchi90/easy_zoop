package com.example.smartpos

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.pax.dal.IPrinter
import com.zoop.pos.Zoop
import com.zoop.pos.collection.UserSelection
import com.zoop.pos.collection.VoidTransaction
import com.zoop.pos.exception.ZoopRequestCanceledException
import com.zoop.pos.plugin.DashboardConfirmationResponse
import com.zoop.pos.plugin.DashboardThemeResponse
import com.zoop.pos.plugin.DashboardTokenResponse
import com.zoop.pos.plugin.ZoopFoundationPlugin
import com.zoop.pos.plugin.smartpos.SmartPOSPlugin
import com.zoop.pos.plugin.smartpos.requestBuilder.SmartPOSMenuOptions
import com.zoop.pos.plugin.smartpos.requestBuilder.SmartPOSPaymentResponse
import com.zoop.pos.plugin.smartpos.requestBuilder.SmartPOSPixPaymentResponse
import com.zoop.pos.plugin.smartpos.requestBuilder.SmartPOSVoidResponse
import com.zoop.pos.plugin.smartpos.requestBuilder.SmartPOSZoopKeyValidationResponse
import com.zoop.pos.requestfield.MessageCallbackRequestField
import com.zoop.pos.requestfield.PinCallbackRequestField
import com.zoop.pos.requestfield.QRCodeCallbackRequestField
import com.zoop.pos.terminal.Terminal
import com.zoop.pos.type.Callback
import com.zoop.pos.type.Option
import com.zoop.pos.type.Request
import kotlinx.coroutines.Runnable
import org.json.JSONObject

class MainViewModel() : ViewModel() {
    var state by mutableStateOf(MainState())
        private set
    private var voidTransaction: UserSelection<VoidTransaction>? = null
    private var paymentRequest: Request? = null
    private var voidRequest: Request? = null
    private var loginRequest: Request? = null
    private var pixRequest: Request? = null

    private var printer: IPrinter? = null
    var printerTester: PrinterTester? = null



    val TAG = "ExampleSmartPos"

    fun handle(event: MainEvent) {
        when (event) {
            MainEvent.OnStartCheckKey -> checkZoopKey()
            MainEvent.OnStartLogin -> login()
            MainEvent.OnStartPayment -> payment()
            MainEvent.OnStartPix -> pix()
            MainEvent.OnStartCancellation -> void()
            MainEvent.OnCancelAction -> cancelAction()
            is MainEvent.OnSelectTransaction -> {
                voidTransaction?.select(event.transaction)
            }
        }
    }

    private fun cancelAction() {
        paymentRequest?.cancel() ?: pixRequest?.cancel() ?: voidTransaction?.cancel()
        ?: voidRequest?.cancel() ?: loginRequest?.cancel()
    }

    private fun login() {
        paymentRequest = null
        voidRequest = null
        loginRequest = ZoopFoundationPlugin.createDashboardActivationRequestBuilder()
            .tokenCallback(object : Callback<DashboardTokenResponse>() {
                override fun onStart() {
                    state = state.copy(status = Status.MESSAGE, message = "Requisitando token")
                }
                override fun onFail(error: Throwable) {
                    Log.d(TAG, "Falha ao requisitar token")
                    state =
                        state.copy(status = Status.MESSAGE, message = "Falha ao requisitar token")
                }
                override fun onSuccess(response: DashboardTokenResponse) {
                    Log.d(TAG, "Apresentar token ao usuário: ${response.token}")
                    state = state.copy(
                        status = Status.MESSAGE,
                        message = "Insira o token no dashboard: ${response.token}"
                    )
                }
            })
            .confirmCallback(object : Callback<DashboardConfirmationResponse>() {
                override fun onFail(error: Throwable) {
                    /**
                    Caso o login seja cancelado, receberá a resposta aqui, com mensagem "request canceled"
                    loginRequest.cancel()
                     */
                    Log.d(TAG, "Apresentar erro na confirmação do token: ${error.message}")
                    state = when (error) {
                        is ZoopRequestCanceledException -> state.copy(
                            status = Status.MESSAGE,
                            message = "Operação cancelada"
                        )

                        else -> state.copy(
                            status = Status.MESSAGE,
                            message = error.message.toString()
                        )
                    }
                }

                override fun onSuccess(response: DashboardConfirmationResponse) {
                    /**
                     * Nesse ponto, recomendamos guardar as credenciais localmente em um banco de dados/shared preferences,
                     * para usar na próxima inicialização, passando como parâmetro para o SmartPOSPluginManager
                     */
                    Log.d(TAG, "Aqui, você recebe as credenciais do estabelecimento")
                    Log.d(TAG, "MarketplaceId: ${response.credentials.marketplace}")
                    Log.d(TAG, "SellerId: ${response.credentials.seller}")
                    Log.d(TAG, "Terminal: ${response.credentials.terminal}")
                    Log.d(TAG, "AccessKey: ${response.credentials.accessKey}")
                    Log.d(TAG, "SellerName: ${response.owner.name}")
                    state = state.copy(
                        status = Status.MESSAGE,
                        message = "SellerName: ${response.owner.name}"
                    )
                }
            })

            .themeCallback(object : Callback<DashboardThemeResponse>() {
                override fun onStart() {
                    if (loginRequest?.isCancelRequested == true) return
                    state = state.copy(status = Status.MESSAGE, message = "Baixando temas")
                }

                override fun onFail(error: Throwable) {
                    Log.d(TAG, "Apresentar erro no download de temas: ${error.message}")
                    state = state.copy(status = Status.MESSAGE, message = error.message.toString())
                }

                override fun onSuccess(response: DashboardThemeResponse) {
                    /**
                     * Aqui você recebe o esquema de cores configurado para o seller no dashboard,
                     * e também sinaliza o sucesso no fluxo de ativação do terminal.
                     */
                    Log.d(TAG, "Exemplo de cor de fonte ${response.color.font}")
                    Log.d(TAG, "Exemplo de cor de botão ${response.color.button}")
                    Log.d(TAG, "Exemplo de logo colorido ${response.logo.coloredBase64}")
                    state = state.copy(status = Status.MESSAGE, message = "Login realizado")

                }

            }).build()
        Zoop.post(loginRequest!!)
    }

    private fun payment() {
        loginRequest = null
        voidRequest = null
        paymentRequest = SmartPOSPlugin.createPaymentRequestBuilder()
            .amount(5)
            .option(Option.CREDIT)
            .installments(1)
            .callback(object : Callback<SmartPOSPaymentResponse>() {
                override fun onStart() {
//                startLoadingAnimation()
                    Log.d("SmartPOS", "onStart")
                    state = state.copy(status = Status.MESSAGE, message = "Iniciando")
                }

                override fun onSuccess(response: SmartPOSPaymentResponse) {
//                handleSucessfullPayment(response)
                    Log.d("SmartPOS", "onSuccess")
                    state = state.copy(status = Status.MESSAGE, message = "SUCESSO")
                }

                override fun onFail(error: Throwable) {
//                handlePaymentFailure(exception)
                    Log.d("SmartPOS", "onFail ${error.message}")
                    val message = if (error.message?.contains("invalid session") == true) {
                        "Não foi realizado um login"
                    } else {
                        error.message
                    }

                    state = state.copy(status = Status.MESSAGE, message = message ?: "Falha")
                }

            })
            .messageCallback(object : Callback<MessageCallbackRequestField.MessageData>() {
                override fun onSuccess(response: MessageCallbackRequestField.MessageData) {
//                displayUserMessage(messageData.message)
                    Log.d("SmartPOS", "messageCallback ${response.message}")
                    state = state.copy(status = Status.MESSAGE, message = response.message)
                }

                override fun onFail(error: Throwable) {
                    Log.d("SmartPOS", "messageCallback fail")
                }
            })

            .pinCallback(object : Callback<PinCallbackRequestField.PinData>() {
                override fun onSuccess(response: PinCallbackRequestField.PinData) {
                    when (response.type) {
                        Terminal.PinEventHandler.EventType.Start -> Log.d(
                            "SmartPOS",
                            "Terminal.PinEventHandler.EventType.Start"
                        )

                        Terminal.PinEventHandler.EventType.Finish -> Log.d(
                            "SmartPOS",
                            "Terminal.PinEventHandler.EventType.Finish"
                        )

                        Terminal.PinEventHandler.EventType.Inserted -> Log.d(
                            "SmartPOS",
                            "Terminal.PinEventHandler.EventType.Inserted"
                        )

                        Terminal.PinEventHandler.EventType.Removed -> Log.d(
                            "SmartPOS",
                            "Terminal.PinEventHandler.EventType.Removed"
                        )

                        else -> Log.d("SmartPOS", "Terminal.PinEventHandler.EventType Else")
                    }
                }

                override fun onFail(error: Throwable) {
                    Log.d("SmartPOS", "onFail ${error.message}")
                }
            })

            .menuSelectionCallback(object : Callback<SmartPOSMenuOptions>() {
                override fun onFail(error: Throwable) {
                }

                override fun onSuccess(response: SmartPOSMenuOptions) {
                    // Apresentar lista ao usuário conforme a documentação
                }

            })
            .build()

        Zoop.post(paymentRequest!!)
    }

    private fun pix() {
        pixRequest = SmartPOSPlugin.createPixPaymentRequestBuilder()
            .amount(5L)
            .callback(object : Callback<SmartPOSPixPaymentResponse>() {
                override fun onStart() {
                    state = state.copy(status = Status.MESSAGE, message = "Iniciando")
                }
                override fun onSuccess(response: SmartPOSPixPaymentResponse) {
                    state = state.copy(status = Status.MESSAGE, message = "SUCESSO")
                }
                override fun onFail(error: Throwable) {
                    val message = if (error.message?.contains("invalid session") == true) {
                        "Não foi realizado um login"
                    } else {
                        error.message
                    }
                    state = state.copy(status = Status.MESSAGE, message = message ?: "Falha")
                }

            })
            .messageCallback(object : Callback<MessageCallbackRequestField.MessageData>() {
                override fun onSuccess(response: MessageCallbackRequestField.MessageData) {
                    state = state.copy(status = Status.MESSAGE, message = response.message)
                }

                override fun onFail(error: Throwable) {
                }
            })
            .qrCodeCallback(object : Callback<QRCodeCallbackRequestField.QRCodeData>() {
                override fun onSuccess(response: QRCodeCallbackRequestField.QRCodeData) {
                    state = state.copy(status = Status.QR_CODE, qrCode = response.data)
                }

                override fun onFail(error: Throwable) {
                }
            })
            .build()

        Zoop.post(pixRequest!!)
    }

    private fun checkZoopKey() {
        SmartPOSPlugin.createZoopKeyValidationRequestBuilder()
            .callback(object : Callback<SmartPOSZoopKeyValidationResponse>() {
                override fun onFail(error: Throwable) {
                }

                override fun onSuccess(response: SmartPOSZoopKeyValidationResponse) {
                    state = if (response.hasKey) {
                        state.copy(status = Status.MESSAGE, message = "Possui chave")
                    } else {
                        state.copy(
                            status = Status.MESSAGE,
                            message = "Chave ausente, necessário envio para PAX para gravação de chave"
                        )
                    }
                }

            })
            .build().run { Zoop.post(this) }
    }

    private fun void() {
        loginRequest = null
        paymentRequest = null
        voidRequest = SmartPOSPlugin.createVoidRequestBuilder()
            .callback(object : Callback<SmartPOSVoidResponse>() {
                override fun onStart() {
                    state = state.copy(status = Status.MESSAGE, message = "Processando")
                }

                override fun onSuccess(response: SmartPOSVoidResponse) {
                    state = state.copy(status = Status.MESSAGE, message = "Cancelamento realizado")
                    voidTransaction = null
                }

                override fun onFail(error: Throwable) {
                    state = state.copy(
                        status = Status.MESSAGE,
                        message = error.message ?: "Falha na operação"
                    )
                    voidTransaction = null
                }

                override fun onComplete() {
                    state = state.copy(status = Status.FINISHED, message = "")
                }
            })
            .voidTransactionCallback(object : Callback<UserSelection<VoidTransaction>>() {
                override fun onSuccess(response: UserSelection<VoidTransaction>) {
                    voidTransaction = response
                    state = state.copy(
                        transactionsList = voidTransaction!!.items.toList(),
                        status = Status.DISPLAY_LIST
                    )
                }

                override fun onFail(error: Throwable) {
                }

            })
            .messageCallback(object : Callback<MessageCallbackRequestField.MessageData>() {
                override fun onSuccess(response: MessageCallbackRequestField.MessageData) {
                    state = state.copy(status = Status.MESSAGE, message = response.message)
                }

                override fun onFail(error: Throwable) {
                }
            })
            .build()

        Zoop.post(voidRequest!!)

    }

    @JavascriptInterface
    fun getReadCodBar(){
        //val integrator = IntentIntegrator(mActivity)
        //integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        //integrator.setPrompt("Scan")
        //integrator.setCameraId(0)
        //integrator.initiateScan()
    }

    @JavascriptInterface
    fun zSair(){
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        tsStatic.SuperContexto!!.startActivity(homeIntent)
    }

    @JavascriptInterface
    fun zPagamento(Conteudo: String) {
        try {
            val jObject: JSONObject = JSONObject(Conteudo)
            val TipoOP: Int = jObject.getInt("TipoOP")
            val Valor: Long = jObject.getLong("Valor")
            val TipoAVista: Int = jObject.getInt("TipoAVista")
            val Parcelas: Int = jObject.getInt("Parcelas")


/*            Log.d("Impressora", "Teste Inicio Impressora ======================")
            printerTester?.getInstance()?.init()
            printerTester?.getStatus()
            printerTester?.getInstance()?.start()
            printerTester?.getInstance()?.printStr("Hello World",null)
            Log.d("Impressora", "Teste  Final Impressora ======================")*/

            //printer?.init()
            //printer?.printStr("Hello World",null)

            val r = object : Runnable{
                override fun run() {
                    startPayment(TipoOP, Valor, TipoAVista, Parcelas)
                }
            }
            r.run()
        }catch (ex: Exception){
            ExecutarJS("EasyPDVPOSPayError(-4, 0, '" + ex.toString().replace("'","")+"')")
        }
    }

    fun startPayment(TipoOP: Int, Valor: Long, TipoAVista: Int, Parcelas: Int){
        Log.d("teste startPayment", "==============")
        try {
            if (TipoOP == 1){//CC
                if(TipoAVista == 1){//CC a vista
                    loginRequest = null
                    voidRequest = null

                    Log.d("teste verificar se entrou ", "=================================================")

                    paymentRequest = SmartPOSPlugin.createPaymentRequestBuilder()
                        .amount(Valor)
                        .option(Option.CREDIT)
                        .installments(1)
                        .callback(object : Callback<SmartPOSPaymentResponse>() {
                            override fun onStart() {
//                startLoadingAnimation()
                                Log.d("SmartPOS", "onStart")
                                state = state.copy(status = Status.MESSAGE, message = "Iniciando")
                            }
                            override fun onSuccess(response: SmartPOSPaymentResponse) {
//                handleSucessfullPayment(response)
                                Log.d("SmartPOS", "onSuccess")
                                state = state.copy(status = Status.MESSAGE, message = "SUCESSO")
                            }
                            override fun onFail(error: Throwable) {
//                handlePaymentFailure(exception)
                                Log.d("SmartPOS", "onFail ${error.message}")
                                val message = if (error.message?.contains("invalid session") == true) {
                                    "Não foi realizado um login"
                                } else {
                                    error.message
                                }
                                state = state.copy(status = Status.MESSAGE, message = message ?: "Falha")
                            }
                        })
                        .messageCallback(object : Callback<MessageCallbackRequestField.MessageData>() {
                            override fun onSuccess(response: MessageCallbackRequestField.MessageData) {
//                displayUserMessage(messageData.message)
                                Log.d("SmartPOS", "messageCallback ${response.message}")
                                state = state.copy(status = Status.MESSAGE, message = response.message)
                            }
                            override fun onFail(error: Throwable) {
                                Log.d("SmartPOS", "messageCallback fail")
                            }
                        })
                        .pinCallback(object : Callback<PinCallbackRequestField.PinData>() {
                            override fun onSuccess(response: PinCallbackRequestField.PinData) {
                                when (response.type) {
                                    Terminal.PinEventHandler.EventType.Start -> Log.d(
                                        "SmartPOS",
                                        "Terminal.PinEventHandler.EventType.Start"
                                    )
                                    Terminal.PinEventHandler.EventType.Finish -> Log.d(
                                        "SmartPOS",
                                        "Terminal.PinEventHandler.EventType.Finish"
                                    )
                                    Terminal.PinEventHandler.EventType.Inserted -> Log.d(
                                        "SmartPOS",
                                        "Terminal.PinEventHandler.EventType.Inserted"
                                    )
                                    Terminal.PinEventHandler.EventType.Removed -> Log.d(
                                        "SmartPOS",
                                        "Terminal.PinEventHandler.EventType.Removed"
                                    )
                                    else -> Log.d("SmartPOS", "Terminal.PinEventHandler.EventType Else")
                                }
                            }
                            override fun onFail(error: Throwable) {
                                Log.d("SmartPOS", "onFail ${error.message}")
                            }
                        })
                        .menuSelectionCallback(object : Callback<SmartPOSMenuOptions>() {
                            override fun onFail(error: Throwable) {
                            }
                            override fun onSuccess(response: SmartPOSMenuOptions) {
                                // Apresentar lista ao usuário conforme a documentação
                            }
                        })
                        .build()
                    Zoop.post(paymentRequest!!)
                }
                if(TipoAVista == 2){//CC parcelado
                    loginRequest = null
                    voidRequest = null
                    paymentRequest = SmartPOSPlugin.createPaymentRequestBuilder()
                        .amount(Valor)
                        .option(Option.CREDIT_WITH_INSTALLMENTS)
                        .installments(Parcelas)
                        .callback(object : Callback<SmartPOSPaymentResponse>() {
                            override fun onStart() {
//                startLoadingAnimation()
                                Log.d("SmartPOS", "onStart")
                                state = state.copy(status = Status.MESSAGE, message = "Iniciando")
                            }
                            override fun onSuccess(response: SmartPOSPaymentResponse) {
//                handleSucessfullPayment(response)
                                Log.d("SmartPOS", "onSuccess")
                                state = state.copy(status = Status.MESSAGE, message = "SUCESSO")
                            }
                            override fun onFail(error: Throwable) {
//                handlePaymentFailure(exception)
                                Log.d("SmartPOS", "onFail ${error.message}")
                                val message = if (error.message?.contains("invalid session") == true) {
                                    "Não foi realizado um login"
                                } else {
                                    error.message
                                }
                                state = state.copy(status = Status.MESSAGE, message = message ?: "Falha")
                            }
                        })
                        .messageCallback(object : Callback<MessageCallbackRequestField.MessageData>() {
                            override fun onSuccess(response: MessageCallbackRequestField.MessageData) {
//                displayUserMessage(messageData.message)
                                Log.d("SmartPOS", "messageCallback ${response.message}")
                                state = state.copy(status = Status.MESSAGE, message = response.message)
                            }
                            override fun onFail(error: Throwable) {
                                Log.d("SmartPOS", "messageCallback fail")
                            }
                        })
                        .pinCallback(object : Callback<PinCallbackRequestField.PinData>() {
                            override fun onSuccess(response: PinCallbackRequestField.PinData) {
                                when (response.type) {
                                    Terminal.PinEventHandler.EventType.Start -> Log.d(
                                        "SmartPOS",
                                        "Terminal.PinEventHandler.EventType.Start"
                                    )
                                    Terminal.PinEventHandler.EventType.Finish -> Log.d(
                                        "SmartPOS",
                                        "Terminal.PinEventHandler.EventType.Finish"
                                    )
                                    Terminal.PinEventHandler.EventType.Inserted -> Log.d(
                                        "SmartPOS",
                                        "Terminal.PinEventHandler.EventType.Inserted"
                                    )
                                    Terminal.PinEventHandler.EventType.Removed -> Log.d(
                                        "SmartPOS",
                                        "Terminal.PinEventHandler.EventType.Removed"
                                    )
                                    else -> Log.d("SmartPOS", "Terminal.PinEventHandler.EventType Else")
                                }
                            }
                            override fun onFail(error: Throwable) {
                                Log.d("SmartPOS", "onFail ${error.message}")
                            }
                        })
                        .menuSelectionCallback(object : Callback<SmartPOSMenuOptions>() {
                            override fun onFail(error: Throwable) {
                            }
                            override fun onSuccess(response: SmartPOSMenuOptions) {
                                // Apresentar lista ao usuário conforme a documentação
                            }
                        })
                        .build()
                    Zoop.post(paymentRequest!!)
                }
            }
            if (TipoOP == 2){//CD
                loginRequest = null
                voidRequest = null
                paymentRequest = SmartPOSPlugin.createPaymentRequestBuilder()
                    .amount(Valor)
                    .option(Option.DEBIT)
                    .installments(1)
                    .callback(object : Callback<SmartPOSPaymentResponse>() {
                        override fun onStart() {
//                startLoadingAnimation()
                            Log.d("SmartPOS", "onStart")
                            state = state.copy(status = Status.MESSAGE, message = "Iniciando")
                        }
                        override fun onSuccess(response: SmartPOSPaymentResponse) {
//                handleSucessfullPayment(response)
                            Log.d("SmartPOS", "onSuccess")
                            state = state.copy(status = Status.MESSAGE, message = "SUCESSO")
                        }
                        override fun onFail(error: Throwable) {
//                handlePaymentFailure(exception)
                            Log.d("SmartPOS", "onFail ${error.message}")
                            val message = if (error.message?.contains("invalid session") == true) {
                                "Não foi realizado um login"
                            } else {
                                error.message
                            }
                            state = state.copy(status = Status.MESSAGE, message = message ?: "Falha")
                        }
                    })
                    .messageCallback(object : Callback<MessageCallbackRequestField.MessageData>() {
                        override fun onSuccess(response: MessageCallbackRequestField.MessageData) {
//                displayUserMessage(messageData.message)
                            Log.d("SmartPOS", "messageCallback ${response.message}")
                            state = state.copy(status = Status.MESSAGE, message = response.message)
                        }
                        override fun onFail(error: Throwable) {
                            Log.d("SmartPOS", "messageCallback fail")
                        }
                    })
                    .pinCallback(object : Callback<PinCallbackRequestField.PinData>() {
                        override fun onSuccess(response: PinCallbackRequestField.PinData) {
                            when (response.type) {
                                Terminal.PinEventHandler.EventType.Start -> Log.d(
                                    "SmartPOS",
                                    "Terminal.PinEventHandler.EventType.Start"
                                )
                                Terminal.PinEventHandler.EventType.Finish -> Log.d(
                                    "SmartPOS",
                                    "Terminal.PinEventHandler.EventType.Finish"
                                )
                                Terminal.PinEventHandler.EventType.Inserted -> Log.d(
                                    "SmartPOS",
                                    "Terminal.PinEventHandler.EventType.Inserted"
                                )
                                Terminal.PinEventHandler.EventType.Removed -> Log.d(
                                    "SmartPOS",
                                    "Terminal.PinEventHandler.EventType.Removed"
                                )
                                else -> Log.d("SmartPOS", "Terminal.PinEventHandler.EventType Else")
                            }
                        }
                        override fun onFail(error: Throwable) {
                            Log.d("SmartPOS", "onFail ${error.message}")
                        }
                    })
                    .menuSelectionCallback(object : Callback<SmartPOSMenuOptions>() {
                        override fun onFail(error: Throwable) {
                        }
                        override fun onSuccess(response: SmartPOSMenuOptions) {
                            // Apresentar lista ao usuário conforme a documentação
                        }
                    })
                    .build()
                Zoop.post(paymentRequest!!)
            }
            if (TipoOP == 3){//PIX
                pixRequest = SmartPOSPlugin.createPixPaymentRequestBuilder()
                    .amount(Valor)
                    .callback(object : Callback<SmartPOSPixPaymentResponse>() {
                        override fun onStart() {
                            state = state.copy(status = Status.MESSAGE, message = "Iniciando")
                        }
                        override fun onSuccess(response: SmartPOSPixPaymentResponse) {
                            state = state.copy(status = Status.MESSAGE, message = "SUCESSO")
                        }
                        override fun onFail(error: Throwable) {
                            val message = if (error.message?.contains("invalid session") == true) {
                                "Não foi realizado um login"
                            } else {
                                error.message
                            }
                            state = state.copy(status = Status.MESSAGE, message = message ?: "Falha")
                        }

                    })
                    .messageCallback(object : Callback<MessageCallbackRequestField.MessageData>() {
                        override fun onSuccess(response: MessageCallbackRequestField.MessageData) {
                            state = state.copy(status = Status.MESSAGE, message = response.message)
                        }
                        override fun onFail(error: Throwable) {
                        }
                    })
                    .qrCodeCallback(object : Callback<QRCodeCallbackRequestField.QRCodeData>() {
                        override fun onSuccess(response: QRCodeCallbackRequestField.QRCodeData) {
                            state = state.copy(status = Status.QR_CODE, qrCode = response.data)
                        }
                        override fun onFail(error: Throwable) {
                        }
                    })
                    .build()
                Zoop.post(pixRequest!!)
            }
        }catch (ex: Exception){
            ExecutarJS("EasyPDVPosPayError(-3, 0, '" + ex.toString().replace("'","")+"');")
        }
    }

    @JavascriptInterface
    fun zImprimir(DANFENFCE: String){
        try {
            val jObject = JSONObject(DANFENFCE)
            val Header = jObject.getString("Header")
            val QR = jObject.getString("QR")
            val Footer = jObject.getString("Footer")

           // printerTester.init()
            //printerTester.printStr("Bounjour Le Monde", null)
            printer?.init()
            printer?.status


            printer?.printStr("Hello World",null)
        } catch (ex: java.lang.Exception) {
            ExecutarJS("EasyPDVPOSErrorDet('" + ex.toString().replace("'", "") + "')")
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Throws(java.lang.Exception::class)
    fun printFile(Header: String, QR: String, Footer: String) {
        try {

            //gertecPrinter.setConfigImpressao(configPrint)
            //val sStatus: String = gertecPrinter.getStatusImpressora()
            //if (gertecPrinter.isImpressoraOK()) {
            //    gertecPrinter.imprimeBitmap(GerarF(Header, QR, Footer))
            //    gertecPrinter.avancaLinha(100)
            //    gertecPrinter.ImpressoraOutput()
            Log.d("teste impressao","teste")
            //} else {
            //    //ExecutarJS("EasyPDVPOSErrorDet('" + sStatus.replace("'", "") + "')")
            //}
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    @JavascriptInterface
    fun zToken(){
        paymentRequest = null
        voidRequest = null
        loginRequest = ZoopFoundationPlugin.createDashboardActivationRequestBuilder()
            .tokenCallback(object : Callback<DashboardTokenResponse>() {
                override fun onStart() {
                    state = state.copy(status = Status.MESSAGE, message = "Requisitando token")
                }
                override fun onFail(error: Throwable) {
                    Log.d(TAG, "Falha ao requisitar token")
                    state =
                        state.copy(status = Status.MESSAGE, message = "Falha ao requisitar token")
                }
                override fun onSuccess(response: DashboardTokenResponse) {
                    Log.d(TAG, "Apresentar token ao usuário: ${response.token}")
                    state = state.copy(
                        status = Status.MESSAGE,
                        message = "Insira o token no dashboard: ${response.token}"
                    )
                }
            })
            .confirmCallback(object : Callback<DashboardConfirmationResponse>() {
                override fun onFail(error: Throwable) {
                    /**
                    Caso o login seja cancelado, receberá a resposta aqui, com mensagem "request canceled"
                    loginRequest.cancel()
                     */
                    Log.d(TAG, "Apresentar erro na confirmação do token: ${error.message}")
                    state = when (error) {
                        is ZoopRequestCanceledException -> state.copy(
                            status = Status.MESSAGE,
                            message = "Operação cancelada"
                        )
                        else -> state.copy(
                            status = Status.MESSAGE,
                            message = error.message.toString()
                        )
                    }
                }
                override fun onSuccess(response: DashboardConfirmationResponse) {
                    /**
                     * Nesse ponto, recomendamos guardar as credenciais localmente em um banco de dados/shared preferences,
                     * para usar na próxima inicialização, passando como parâmetro para o SmartPOSPluginManager
                     */
                    Log.d(TAG, "Aqui, você recebe as credenciais do estabelecimento")
                    Log.d(TAG, "MarketplaceId: ${response.credentials.marketplace}")
                    Log.d(TAG, "SellerId: ${response.credentials.seller}")
                    Log.d(TAG, "Terminal: ${response.credentials.terminal}")
                    Log.d(TAG, "AccessKey: ${response.credentials.accessKey}")
                    Log.d(TAG, "SellerName: ${response.owner.name}")


                /*    val preferences = getSharedPreferences("user_preferences", ComponentActivity.MODE_PRIVATE)
                    val editor = preferences.edit()
                    editor.putString("marketplace","0bc5d980777d43fd9aee0f8d215d8735")
                    editor.putString("seller","afc4a20ebe09433fac674ad0856ac33c")
                    editor.putString("accessKey","57c813b3-2330-4ed9-9ad7-14534ff595cd")
                    editor.commit()*/

                    state = state.copy(
                        status = Status.MESSAGE,
                        message = "SellerName: ${response.owner.name}"
                    )
                }
            })
            .themeCallback(object : Callback<DashboardThemeResponse>() {
                override fun onStart() {
                    if (loginRequest?.isCancelRequested == true) return
                    state = state.copy(status = Status.MESSAGE, message = "Baixando temas")
                }
                override fun onFail(error: Throwable) {
                    Log.d(TAG, "Apresentar erro no download de temas: ${error.message}")
                    state = state.copy(status = Status.MESSAGE, message = error.message.toString())
                }
                override fun onSuccess(response: DashboardThemeResponse) {
                    /**
                     * Aqui você recebe o esquema de cores configurado para o seller no dashboard,
                     * e também sinaliza o sucesso no fluxo de ativação do terminal.
                     */
                    Log.d(TAG, "Exemplo de cor de fonte ${response.color.font}")
                    Log.d(TAG, "Exemplo de cor de botão ${response.color.button}")
                    Log.d(TAG, "Exemplo de logo colorido ${response.logo.coloredBase64}")
                    state = state.copy(status = Status.MESSAGE, message = "Login realizado")
                }
            }).build()
        Zoop.post(loginRequest!!)
    }

    fun ExecutarJS(JS: String) {
        tsStatic.webView?.post(Runnable {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                tsStatic.webView!!.evaluateJavascript("(function() { $JS })();") { value ->
                    value
                }
            } else{
                tsStatic.webView!!.loadUrl("javascript:android.onData($JS)")
            }
        })
    }

}



