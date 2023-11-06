package com.example.smartpos

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.zoop.pos.Zoop
import com.zoop.pos.collection.UserSelection
import com.zoop.pos.collection.VoidTransaction
import com.zoop.pos.plugin.DashboardTokenResponse
import com.zoop.pos.plugin.ZoopFoundationPlugin
import com.zoop.pos.type.Request
import com.zoop.pos.type.Callback
import com.zoop.pos.type.Option
import com.zoop.pos.exception.ZoopRequestCanceledException
import com.zoop.pos.requestfield.MessageCallbackRequestField
import com.zoop.pos.requestfield.PinCallbackRequestField
import com.zoop.pos.requestfield.QRCodeCallbackRequestField
import com.zoop.pos.terminal.Terminal
import com.zoop.pos.plugin.DashboardConfirmationResponse
import com.zoop.pos.plugin.DashboardThemeResponse
import com.zoop.pos.plugin.smartpos.SmartPOSPlugin
import com.zoop.pos.plugin.smartpos.requestBuilder.*

class MainViewModel : ViewModel() {

    var state by mutableStateOf(MainState())
        private set
    private var voidTransaction: UserSelection<VoidTransaction>? = null
    private var paymentRequest: Request? = null
    private var voidRequest: Request? = null
    private var loginRequest: Request? = null
    private var pixRequest: Request? = null

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
}