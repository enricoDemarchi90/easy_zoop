package com.example.EasyMobilePDV

import com.zoop.pos.collection.VoidTransaction

sealed class MainEvent {
    object OnStartCheckKey : MainEvent()
    object OnStartLogin : MainEvent()
    object OnStartPayment : MainEvent()
    object OnStartPix : MainEvent()
    object OnStartCancellation : MainEvent()
    object OnCancelAction : MainEvent()
    class OnSelectTransaction(val transaction: VoidTransaction) : MainEvent()
}