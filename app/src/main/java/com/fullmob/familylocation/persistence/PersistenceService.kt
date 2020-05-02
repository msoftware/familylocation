package com.fullmob.familylocation.persistence

import com.fullmob.familylocation.models.ActionableKeywords
import com.fullmob.familylocation.models.Actions
import com.fullmob.familylocation.models.Sender

interface PersistenceService {

    fun saveSmsRequest(from: Sender, content: String, actions: List<Actions>, isSuccess: Boolean)

    fun getAllowedSenders(): Array<Sender>

    fun getKeywords(): ActionableKeywords

    fun saveSmsResponds(to: Sender, content: String, actions: List<Actions>, isSuccess: Boolean)
}