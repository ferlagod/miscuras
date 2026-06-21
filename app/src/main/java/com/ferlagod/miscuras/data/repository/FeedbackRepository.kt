package com.ferlagod.miscuras.data.repository

import com.ferlagod.miscuras.network.FormPayload
import com.ferlagod.miscuras.network.FormSubmitApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeedbackRepository(
    private val formSubmitApi: FormSubmitApi
) {
    suspend fun submitProductSuggestion(payload: FormPayload): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = formSubmitApi.submitForm(formId = com.ferlagod.miscuras.BuildConfig.FORMSPREE_ID, payload = payload)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
