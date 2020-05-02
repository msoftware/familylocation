package com.fullmob.familylocation.di

import android.util.Log
import com.fullmob.familylocation.models.*
import com.fullmob.familylocation.persistence.PersistenceService
import com.fullmob.familylocation.sms.SmsListener
import org.koin.dsl.module
import java.util.*
import kotlin.collections.HashSet

class SmsListenerImpl(private val persistenceService: PersistenceService) : SmsListener {
    companion object {
        private const val TAG: String = "SMSListener"
    }

    override fun messageReceived(from: String, messageText: String) {
        persistenceService.getAllowedSenders().firstOrNull { from == it.phone }
            ?.let { sender ->
                processMessage(sender, messageText)
            }
    }

    private fun processMessage(sender: Sender, messageText: String) {
        val actions: MutableSet<Actions> = HashSet<Actions>().toMutableSet()
        persistenceService.getKeywords().forEach { (action, keywords) ->
            val messageLower = messageText.toLowerCase(Locale.US)
            keywords.forEach { keyword ->
                if (messageLower.contains(keyword)) {
                    actions.add(action)
                }
            }
        }
        val totalRequestedActions = actions.size
        if (totalRequestedActions == 0) {
            return
        }
        val grantedPermissions = sender.permissions.toSet()
        val areActionsPermitted =
            actions.filter { grantedPermissions.contains(it) }.size == totalRequestedActions
        persistenceService.saveSmsRequest(
            sender,
            messageText,
            actions.toList(),
            areActionsPermitted
        )
        if (areActionsPermitted) {
            executeActions(sender, messageText, actions)
        } else {
            respondWithInvalidActions(sender, messageText, actions)
        }
    }

    private fun executeActions(sender: Sender, messageText: String, actions: MutableSet<Actions>) {
        Log.d(TAG, "Executing actions: $actions from: ${sender.name}(${sender.phone})")
    }

    private fun respondWithInvalidActions(
        sender: Sender,
        messageText: String,
        actions: MutableSet<Actions>
    ) {
        Log.d(TAG, "Some actions are invalid from: $actions")
    }
}

class PersistenceServiceImpl : PersistenceService {
    override fun saveSmsRequest(
        from: Sender,
        content: String,
        actions: List<Actions>,
        isSuccess: Boolean
    ) {
        Log.d(
            "Saving request",
            "From: $from\nContent: $content\nActions: $actions\nSuccessful: $isSuccess"
        )
    }

    override fun getAllowedSenders(): Array<Sender> = arrayOf(
        Sender(
            name = "Papa",
            phone = "+4917675516622",
            photo = "",
            isAdult = true,
            responseType = ResponseType.APPROVE_AFTER_TIMEOUT,
            connectivityResponse = ConnectivityResponse.REPLY_WITH_LOCATION_WITHOUT_INTERNET,
            canForceConnect = true,
            permissions = arrayOf(
                Actions.UNSILENCE_PHONE,
                Actions.OBTAIN_LOCATION,
                Actions.FORCE_CONNECT
            )
        ), Sender(
            name = "Mama",
            phone = "+4917665921732",
            photo = "",
            isAdult = true,
            responseType = ResponseType.APPROVE_AFTER_TIMEOUT,
            connectivityResponse = ConnectivityResponse.REPLY_WITH_LOCATION_WITHOUT_INTERNET,
            canForceConnect = true,
            permissions = arrayOf(
                Actions.UNSILENCE_PHONE,
                Actions.OBTAIN_LOCATION,
                Actions.FORCE_CONNECT
            )
        )
    )

    override fun getKeywords(): ActionableKeywords = mapOf(
        Actions.OBTAIN_LOCATION to arrayOf("where", "location", "locate"),
        Actions.FORCE_CONNECT to arrayOf(
            "go online",
            "connect to wifi",
            "connect to cellular",
            "connect to data",
            "connect now",
            "just connect"
        ),
        Actions.UNSILENCE_PHONE to arrayOf(
            "silence off",
            "no vibration",
            "turn off vibration",
            "unsilence"
        )
    )

    override fun saveSmsResponds(
        to: Sender,
        content: String,
        actions: List<Actions>,
        isSuccess: Boolean
    ) {
        Log.d(
            "Saving response",
            "To: $to\nContent: $content\nActions: $actions\nSuccessful: $isSuccess"
        )
    }
}

val MainModule = module {
    single { SmsListenerImpl(PersistenceServiceImpl()) }
}
