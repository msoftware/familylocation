package com.fullmob.familylocation.models


enum class ResponseType {
    MUST_APPROVE,
    APPROVE_AFTER_TIMEOUT,
    APPROVE_IMMEDIATELY,
    REJECT,
}

enum class ConnectivityResponse {
    REPLY_WHEN_CONNECTED,
    REPLY_WITH_LOCATION_WITHOUT_INTERNET,
    CONNECT_TO_WIFI_AND_REPLY,
    CONNECT_TO_MOBILE_DATA_AND_REPLY,
    REPLY_WITHOUT_LOCATION_VIA_SMS,
}

class Sender(
    val name: String,
    val photo: String,
    val phone: String,
    val isAdult: Boolean,
    val responseType: ResponseType,
    val connectivityResponse: ConnectivityResponse,
    val permissions: Array<Actions>,
    val canForceConnect: Boolean,
    val allowAlias: Boolean = false,
    val aliasName: String? = null,
    val aliasMessage: String? = null,
    val aliasPhoto: String? = null
)