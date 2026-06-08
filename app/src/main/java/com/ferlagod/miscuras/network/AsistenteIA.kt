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

import android.util.Log
import com.ferlagod.miscuras.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import org.json.JSONObject

/**
 * Servicio encargado de la integración con el modelo de lenguaje de IA de Google (Gemini).
 * Formatea el contexto clínico y obtiene respuestas educativas alineadas con TIME y GNEAUPP.
 */
class AsistenteIA {

    // Se instancia el modelo usando la clave API inyectada de forma segura desde BuildConfig
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        safetySettings = listOf(
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE)
        ),
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        },
        systemInstruction = content {
            text(
                "Actúas estrictamente como un asistente de redacción educativa para enfermería basado en las guías TIMERS y GNEAUPP. " +
                "Tu función es explicar la lógica teórica del tratamiento basándote en los datos locales provistos. " +
                "Puedes y debes mencionar las familias genéricas de apósitos que la literatura científica aconseja para ese tipo de lecho y exudado. " +
                "DEBES DEVOLVER EXCLUSIVAMENTE UN JSON PLANO CON ESTOS 3 CAMPOS OBLIGATORIOS: " +
                "\"objetivo_time\", \"justificacion_aposito\", \"consejo_clinico\".\n" +
                "Ejemplo de respuesta PROHIBIDA (escueta): {\"objetivo_time\": \"Controlar\", \"justificacion_aposito\": \"Absorbe\", \"consejo_clinico\": \"Lavar\"}\n" +
                "Ejemplo de respuesta CORRECTA (educativa y basada en TIMERS): {\n" +
                "  \"objetivo_time\": \"El objetivo principal según el esquema TIMERS es el control del exudado (M) y la carga bacteriana (I) para favorecer la preparación del lecho.\",\n" +
                "  \"justificacion_aposito\": \"Se recomienda el uso de un apósito de alginato con plata porque su capacidad de gelificación gestiona el exceso de humedad, mientras que la plata actúa disminuyendo la carga bacteriana del lecho frente a signos de infección local.\",\n" +
                "  \"consejo_clinico\": \"Vigilar signos de maceración en los bordes y proteger la piel perilesional con película barrera no irritante.\"\n" +
                "}\n" +
                "REGLA CRÍTICA: Queda totalmente prohibido diagnosticar casos reales o mencionar marcas comerciales concretas. " +
                "Si la consulta del usuario se desvía hacia un paciente real, responde textualmente: 'Como asistente educativo, no puedo valorar casos reales. Por favor, consulta los protocolos oficiales y aplica tu juicio clínico.'"
            )
        }
    )

    /**
     * Envía los datos locales de tu app a Gemini para que redacte la explicación.
     */
    suspend fun obtenerExplicacionEducativa(
        lecho: String,
        exudado: String,
        tipoExudado: String,
        infeccion: Boolean,
        germen: String,
        tamanoLargo: String,
        tamanoAncho: String,
        bordes: String,
        pielPerilesional: String,
        recomendacionBD: String,
        dolor: Int
    ): String {
        val infText = if (infeccion) "Sí (Germen: $germen)" else "No"
        val tamText = if (tamanoLargo.isNotEmpty() && tamanoAncho.isNotEmpty()) "${tamanoLargo}x${tamanoAncho} cm" else "No especificado"
        
        val promptUsuario = """
            Datos clínicos de la evaluación:
            - Lecho de la herida: $lecho
            - Exudado: Nivel $exudado, Tipo $tipoExudado
            - Bordes y Piel: Bordes $bordes, Piel perilesional $pielPerilesional
            - Infección: $infText
            - Sensibilidad/Dolor (S): $dolor/10
            - Tamaño (Largo x Ancho): $tamText
            - Tratamiento recomendado por el sistema: $recomendacionBD
            
            Instrucciones:
            Basándote en estos valores y el acrónimo TIMERS, justifica brevemente por qué el tratamiento ($recomendacionBD) es adecuado.
            Asegúrate de justificar el tratamiento en base a TODO el cuadro clínico, en especial prestando atención al tipo de exudado ($tipoExudado), el estado de la piel perilesional ($pielPerilesional) para recomendar protección si es necesario, la presencia de infección, el germen ($germen) y el nivel de dolor ($dolor/10). Si el dolor es elevado (>= 4), destaca la importancia de terapias atraumáticas.
            Devuelve un JSON estrictamente con los campos objetivo_time, justificacion_aposito, consejo_clinico.
        """.trimIndent()

        // 3. Fallback local de contingencia
        fun construirFallback(): String {
            val infPart = if (infeccion) "y reducir la carga bacteriana provocada por $germen" else "evitando complicaciones"
            val dolorPart = if (dolor >= 4) " y manejando el dolor local ($dolor/10) mediante técnicas atraumáticas" else ""
            return "Objetivo TIMERS: Preparar el lecho de la herida ($lecho), gestionar el exudado $tipoExudado ($exudado), cuidar los bordes ($bordes) y proteger la piel perilesional ($pielPerilesional)$dolorPart.\n\n" +
                   "Justificación: El uso de $recomendacionBD está indicado para mantener un ambiente húmedo óptimo $infPart.\n\n" +
                   "Consejo Clínico: Evaluar regularmente el estado de la piel perilesional y aplicar barrera protectora si es necesario."
        }

        return try {
            val response = generativeModel.generateContent(promptUsuario)
            val textoIA = response.text
            if (textoIA.isNullOrBlank()) {
                Log.e("AsistenteIA", "Gemini devolvió texto nulo o bloqueado por seguridad.")
                construirFallback()
            } else {
                try {
                    // Limpiar markdown residual si Gemini lo devuelve a pesar del mimeType
                    val cleanJson = textoIA.trim().removePrefix("```json").removeSuffix("```").trim()
                    val json = JSONObject(cleanJson)
                    
                    val objetivo = json.optString("objetivo_time", "").trim()
                    val justificacion = json.optString("justificacion_aposito", "").trim()
                    val consejo = json.optString("consejo_clinico", "").trim()

                    if (objetivo.isEmpty() || justificacion.isEmpty() || consejo.isEmpty()) {
                        Log.e("AsistenteIA", "El JSON devuelto tiene algún campo vacío.")
                        construirFallback()
                    } else {
                        val formattedResponse = "Objetivo TIME: $objetivo\n\nJustificación: $justificacion\n\nConsejo Clínico: $consejo"
                        
                        if (formattedResponse.length < 100) {
                            Log.e("AsistenteIA", "Respuesta descartada por ser demasiado escueta (<100 caracteres).")
                            construirFallback()
                        } else {
                            formattedResponse
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AsistenteIA", "Error al parsear el JSON de Gemini: ${e.message}")
                    construirFallback()
                }
            }
        } catch (e: Exception) {
            Log.e("AsistenteIA", "Excepción al llamar a Gemini: ${e.message}", e)
            construirFallback()
        }
    }
}
