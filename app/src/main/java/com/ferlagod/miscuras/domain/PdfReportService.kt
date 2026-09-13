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
package com.ferlagod.miscuras.domain

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.ferlagod.miscuras.data.entities.EvaluationEntity
import com.ferlagod.miscuras.data.entities.PatientEntity
import com.ferlagod.miscuras.data.entities.WoundEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfReportService(private val context: Context) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    suspend fun generateWoundReport(
        patient: PatientEntity,
        wound: WoundEntity,
        evaluations: List<EvaluationEntity>,
        outputFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard (595x842 pt)
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(33, 33, 33)
                textSize = 10f
            }

            val primaryColor = Color.rgb(0, 105, 137)
            val surfaceBg = Color.rgb(245, 247, 250)
            val dividerColor = Color.rgb(220, 224, 230)

            var y = 40f

            // --- Header Banner ---
            paint.color = primaryColor
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(30f, y, 565f, y + 55f, 12f, 12f, paint)

            paint.color = Color.WHITE
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText("MIS CURAS — INFORME CLÍNICO DE EVOLUCIÓN", 46f, y + 25f, paint)

            paint.textSize = 9f
            paint.isFakeBoldText = false
            paint.color = Color.rgb(215, 235, 245)
            canvas.drawText("Guía de Apósitos y Valoración TIMERS de Heridas", 46f, y + 42f, paint)

            val emitDate = "Emitido: ${dateFormat.format(Date())}"
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(emitDate, 550f, y + 42f, paint)
            paint.textAlign = Paint.Align.LEFT

            y += 75f

            // --- Patient & Wound Info Box ---
            paint.color = surfaceBg
            canvas.drawRoundRect(30f, y, 565f, y + 75f, 8f, 8f, paint)

            paint.color = dividerColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(30f, y, 565f, y + 75f, 8f, 8f, paint)

            paint.style = Paint.Style.FILL
            paint.color = primaryColor
            paint.textSize = 11f
            paint.isFakeBoldText = true
            canvas.drawText("DATOS DEL PACIENTE Y LESIÓN", 42f, y + 18f, paint)

            textPaint.textSize = 9.5f
            textPaint.isFakeBoldText = false
            canvas.drawText("Paciente: ${patient.anonymizedName}", 42f, y + 36f, textPaint)
            canvas.drawText("Habitación / Cama: ${patient.roomNumber}", 220f, y + 36f, textPaint)
            canvas.drawText("Herida: ${wound.name}", 390f, y + 36f, textPaint)

            val allergiesStr = patient.allergies?.ifEmpty { "Ninguna conocida" } ?: "Ninguna conocida"
            canvas.drawText("Alergias: $allergiesStr", 42f, y + 52f, textPaint)

            val stateStr = if (wound.isDischarged) "DE ALTA (${wound.dischargedAt?.let { shortDateFormat.format(Date(it)) } ?: ""})" else "ACTIVA EN TRATAMIENTO"
            canvas.drawText("Estado: $stateStr", 42f, y + 66f, textPaint)
            val medHist = patient.medicalHistory?.take(35) ?: "Sin antecedentes registrados"
            canvas.drawText("Antecedentes: $medHist", 220f, y + 66f, textPaint)

            y += 90f

            // --- Latest Evaluation Highlights ---
            val latestEval = evaluations.lastOrNull()
            if (latestEval != null) {
                paint.color = primaryColor
                paint.textSize = 12f
                paint.isFakeBoldText = true
                canvas.drawText("ÚLTIMA EVALUACIÓN CLÍNICA (${dateFormat.format(Date(latestEval.timestamp))})", 30f, y, paint)
                y += 15f

                paint.color = surfaceBg
                canvas.drawRoundRect(30f, y, 565f, y + 160f, 8f, 8f, paint)
                paint.color = dividerColor
                paint.style = Paint.Style.STROKE
                canvas.drawRoundRect(30f, y, 565f, y + 160f, 8f, 8f, paint)
                paint.style = Paint.Style.FILL

                var evalY = y + 20f
                textPaint.isFakeBoldText = true
                canvas.drawText("Etiología: ", 42f, evalY, textPaint)
                textPaint.isFakeBoldText = false
                canvas.drawText(latestEval.etiology, 100f, evalY, textPaint)

                textPaint.isFakeBoldText = true
                canvas.drawText("Lecho (T): ", 220f, evalY, textPaint)
                textPaint.isFakeBoldText = false
                canvas.drawText(latestEval.bedState, 280f, evalY, textPaint)

                textPaint.isFakeBoldText = true
                canvas.drawText("Exudado (M): ", 390f, evalY, textPaint)
                textPaint.isFakeBoldText = false
                canvas.drawText("${latestEval.exudateLevel} (${latestEval.exudateType})", 465f, evalY, textPaint)

                evalY += 18f
                textPaint.isFakeBoldText = true
                canvas.drawText("Infección (I): ", 42f, evalY, textPaint)
                textPaint.isFakeBoldText = false
                val inf = if (latestEval.infection) "SÍ (Germen: ${latestEval.infectionGerm})" else "NO"
                canvas.drawText(inf, 115f, evalY, textPaint)

                textPaint.isFakeBoldText = true
                canvas.drawText("Bordes (E): ", 220f, evalY, textPaint)
                textPaint.isFakeBoldText = false
                canvas.drawText(latestEval.edges, 285f, evalY, textPaint)

                textPaint.isFakeBoldText = true
                canvas.drawText("Piel Perilesional: ", 390f, evalY, textPaint)
                textPaint.isFakeBoldText = false
                canvas.drawText(latestEval.perilesional, 475f, evalY, textPaint)

                evalY += 18f
                textPaint.isFakeBoldText = true
                canvas.drawText("Medidas: ", 42f, evalY, textPaint)
                textPaint.isFakeBoldText = false
                val depthStr = if (latestEval.depth.isNotEmpty()) " x ${latestEval.depth}" else ""
                val cavStr = if (latestEval.hasCavitation) " [Cavitada]" else ""
                canvas.drawText("${latestEval.length} x ${latestEval.width}$depthStr cm$cavStr", 95f, evalY, textPaint)

                textPaint.isFakeBoldText = true
                canvas.drawText("Dolor EVA (S): ", 220f, evalY, textPaint)
                textPaint.isFakeBoldText = false
                canvas.drawText("${latestEval.painLevel.toInt()}/10", 300f, evalY, textPaint)

                evalY += 22f
                paint.color = dividerColor
                paint.strokeWidth = 0.5f
                canvas.drawLine(42f, evalY, 550f, evalY, paint)

                evalY += 16f
                textPaint.isFakeBoldText = true
                canvas.drawText("Tratamiento Recomendado:", 42f, evalY, textPaint)
                textPaint.isFakeBoldText = false
                val treatLines = latestEval.recommendedTreatment.split("\n").filter { it.isNotBlank() }
                for (line in treatLines.take(2)) {
                    canvas.drawText(line.take(85), 42f, evalY + 14f, textPaint)
                    evalY += 14f
                }

                if (!latestEval.selectedProducts.isNullOrBlank()) {
                    evalY += 6f
                    textPaint.isFakeBoldText = true
                    canvas.drawText("Productos Aplicados:", 42f, evalY, textPaint)
                    textPaint.isFakeBoldText = false
                    canvas.drawText(latestEval.selectedProducts.take(80), 160f, evalY, textPaint)
                }

                y += 175f
            }

            // --- History Table (Last 6 evaluations) ---
            if (evaluations.isNotEmpty()) {
                paint.color = primaryColor
                paint.textSize = 12f
                paint.isFakeBoldText = true
                canvas.drawText("HISTÓRICO EVOLUTIVO", 30f, y, paint)
                y += 15f

                // Table Header
                paint.color = Color.rgb(230, 235, 242)
                canvas.drawRect(30f, y, 565f, y + 20f, paint)

                paint.color = primaryColor
                paint.textSize = 8.5f
                paint.isFakeBoldText = true
                canvas.drawText("Fecha", 36f, y + 13f, paint)
                canvas.drawText("Medidas", 115f, y + 13f, paint)
                canvas.drawText("Lecho / Tejido", 195f, y + 13f, paint)
                canvas.drawText("Exudado", 300f, y + 13f, paint)
                canvas.drawText("Infección", 390f, y + 13f, paint)
                canvas.drawText("Dolor", 465f, y + 13f, paint)
                canvas.drawText("Bordes", 505f, y + 13f, paint)

                y += 20f

                val recentEvals = evaluations.takeLast(6)
                for (ev in recentEvals) {
                    textPaint.textSize = 8f
                    textPaint.isFakeBoldText = false
                    canvas.drawText(shortDateFormat.format(Date(ev.timestamp)), 36f, y + 13f, textPaint)
                    canvas.drawText("${ev.length}x${ev.width} cm", 115f, y + 13f, textPaint)
                    canvas.drawText(ev.bedState.take(18), 195f, y + 13f, textPaint)
                    canvas.drawText(ev.exudateLevel, 300f, y + 13f, textPaint)
                    canvas.drawText(if (ev.infection) "Sí (${ev.infectionGerm.take(8)})" else "No", 390f, y + 13f, textPaint)
                    canvas.drawText("${ev.painLevel.toInt()}/10", 465f, y + 13f, textPaint)
                    canvas.drawText(ev.edges.take(12), 505f, y + 13f, textPaint)

                    paint.color = dividerColor
                    paint.strokeWidth = 0.5f
                    canvas.drawLine(30f, y + 18f, 565f, y + 18f, paint)
                    y += 18f
                }
            }

            // --- Footer / Signature area ---
            val footerY = 770f
            paint.color = dividerColor
            paint.strokeWidth = 1f
            canvas.drawLine(30f, footerY, 565f, footerY, paint)

            textPaint.textSize = 8f
            textPaint.color = Color.rgb(120, 130, 140)
            canvas.drawText("Documento generado por Mis Curas — No sustituye el juicio clínico del profesional sanitario.", 30f, footerY + 15f, textPaint)

            paint.color = Color.rgb(70, 80, 90)
            paint.textSize = 8.5f
            paint.isFakeBoldText = true
            canvas.drawText("Firma del Profesional Sanitario / Enfermera:", 370f, footerY + 15f, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f
            paint.color = Color.rgb(180, 190, 200)
            canvas.drawLine(370f, footerY + 45f, 545f, footerY + 45f, paint)

            pdfDocument.finishPage(page)

            FileOutputStream(outputFile).use { fos ->
                pdfDocument.writeTo(fos)
            }
            pdfDocument.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
