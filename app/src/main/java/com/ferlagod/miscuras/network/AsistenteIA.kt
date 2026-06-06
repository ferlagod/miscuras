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

import com.ferlagod.miscuras.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import android.util.Log

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
        systemInstruction = content {
            text(
                "Actúas estrictamente como un asistente de redacción educativa para enfermería basado en las guías TIME y GNEAUPP. " +
                "Tu función es explicar la lógica teórica del tratamiento basándote en los datos locales provistos. " +
                "Puedes y debes mencionar las familias genéricas de apósitos (ej. hidrogeles, alginatos, espumas de poliuretano, plata) que la literatura científica aconseja para ese tipo de lecho y exudado. " +
                "Para terminar, añade una única frase concisa con un consejo clínico teórico sobre la protección de la piel perilesional o signos de alerta a vigilar en esa fase específica. " +
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
        infeccion: Boolean,
        germen: String,
        tamanoLargo: String,
        tamanoAncho: String,
        recomendacionBD: String
    ): String {
        val infText = if (infeccion) "Sí (Germen: $germen)" else "No"
        val tamText = if (tamanoLargo.isNotEmpty() && tamanoAncho.isNotEmpty()) "${tamanoLargo}x${tamanoAncho} cm" else "No especificado"
        
        val promptUsuario = """
            Datos clínicos de la evaluación:
            - Lecho de la herida: $lecho
            - Exudado: $exudado
            - Infección: $infText
            - Tamaño (Largo x Ancho): $tamText
            - Tratamiento recomendado por el sistema: $recomendacionBD
            
            Instrucciones:
            Redacta un único párrafo BREVE, fluido y altamente educativo (MÁXIMO 3-4 oraciones cortas, sin usar listas ni viñetas). 
            Tu explicación DEBE integrar de forma resumida el lecho ($lecho), exudado ($exudado), infección ($infText) y tamaño ($tamText). 
            Basándote en estos valores y el acrónimo TIME, justifica brevemente por qué el tratamiento ($recomendacionBD) es adecuado según GNEAUPP y termina con un consejo rápido sobre la protección perilesional.
            Sé sintético y profesional.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(promptUsuario)
            val textoIA = response.text
            if (textoIA.isNullOrBlank()) {
                Log.e("AsistenteIA", "Gemini devolvió texto nulo o bloqueado por seguridad.")
                recomendacionBD
            } else {
                textoIA
            }
        } catch (e: Exception) {
            Log.e("AsistenteIA", "Excepción al llamar a Gemini: ${e.message}", e)
            // Si hay error 429 (límite de cuota), caída de red o cualquier fallo de la IA,
            // devolvemos directamente la recomendación local para no romper el flujo.
            recomendacionBD
        }
    }
}
