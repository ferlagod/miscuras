/*
 * Mis Curas
 * Copyright (C) Fernando Lago. 2026
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
package com.ferlagod.miscuras.data.repository

import com.ferlagod.miscuras.network.FormPayload
import com.ferlagod.miscuras.network.FormSubmitApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio encargado de gestionar el envío de sugerencias y comentarios por parte del usuario.
 * Utiliza Formspree u otro servicio externo mediante [FormSubmitApi].
 * 
 * @property formSubmitApi Interfaz Retrofit para realizar las peticiones HTTP POST de envío.
 */
class FeedbackRepository(
    private val formSubmitApi: FormSubmitApi
) {
    /**
     * Envía una sugerencia de producto a través del formulario externo.
     * La operación se realiza de forma asíncrona en el hilo [Dispatchers.IO].
     * 
     * @param payload Objeto que contiene el nombre del producto, marca, comentarios y email.
     * @return Result<Unit> indicando éxito (si la respuesta es satisfactoria) o fallo con una excepción.
     */
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
