/*
 * Mis Curas
 * Copyright (C) 2026
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.ferlagod.miscuras.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.Response

/**
 * Carga de datos para el envío de formularios de feedback.
 */
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

/**
 * Cliente HTTP Retrofit para el servicio Formspree.io.
 * Permite enviar sugerencias de productos de forma anónima o identificada
 * directamente a un correo electrónico sin necesitar backend propio.
 */
interface FormSubmitApi {
    /**
     * Dispara la petición POST al servicio de envío de correos de Formspree.
     *
     * @param formId Identificador único asignado por Formspree para la cuenta de correo de destino.
     * @param payload Objeto que contiene los datos del formulario a enviar.
     * @return Respuesta que indica el éxito de la petición.
     */
    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("f/{formId}")
    suspend fun submitForm(
        @Path("formId") formId: String,
        @Body payload: FormPayload
    ): Response<Any>
}

/**
 * Cliente de red configurado con Retrofit para llamadas a la API.
 */
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
