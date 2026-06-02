package com.ferlagod.miscuras.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.Response

data class FormPayload(
    val name: String,
    val is_health_professional: String,
    val belongs_to_laboratory: String,
    val product_name: String,
    val wound_bed: String,
    val exudate_level: String,
    val other_suggestions: String,
    val _subject: String = "Nueva sugerencia de apósito - Mis Curas"
)

interface FormSubmitApi {
    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("f/{formId}")
    suspend fun submitForm(
        @Path("formId") formId: String,
        @Body payload: FormPayload
    ): Response<Any>
}

object NetworkClient {
    private const val BASE_URL = "https://formspree.io/"

    val formSubmitApi: FormSubmitApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FormSubmitApi::class.java)
    }
}
