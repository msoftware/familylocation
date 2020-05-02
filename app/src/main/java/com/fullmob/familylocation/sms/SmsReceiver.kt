package com.fullmob.familylocation.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.os.Build.VERSION_CODES.M
import android.os.Build.VERSION.SDK_INT
import com.fullmob.familylocation.di.PersistenceServiceImpl
import com.fullmob.familylocation.di.SmsListenerImpl

interface SmsListener {
    fun messageReceived(from: String, messageText: String)
}

class SmsReceiver : BroadcastReceiver() {

    lateinit var listener: SmsListener

    override fun onReceive(context: Context, intent: Intent) {
        listener = SmsListenerImpl(PersistenceServiceImpl())
        if (intent.action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            intent.extras?.let { extras ->
                val pdus = extras.get("pdus") as? Array<Any>
                pdus?.forEach { data ->
                    val smsMessage =
                        if (SDK_INT >= M) {
                            SmsMessage.createFromPdu(
                                data as ByteArray,
                                extras.getString("format")
                            )
                        } else {
                            SmsMessage.createFromPdu(data as ByteArray)
                        }
                    listener.messageReceived(
                        smsMessage.displayOriginatingAddress,
                        smsMessage.messageBody
                    )
                }
            }
        }

    }

}
