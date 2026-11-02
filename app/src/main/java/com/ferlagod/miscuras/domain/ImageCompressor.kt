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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Reduce las fotos clínicas antes de guardarlas:
 * - Lado mayor limitado (por defecto 2048 px), suficiente para valorar la herida.
 * - JPEG con calidad 85.
 * - Aplica la orientación EXIF a los píxeles y descarta los metadatos EXIF
 *   (incluida la ubicación GPS que añaden algunas cámaras).
 * Reduce mucho el tamaño de las copias de seguridad y del almacenamiento.
 */
object ImageCompressor {
    private const val TAG = "ImageCompressor"

    suspend fun compressInPlace(file: File, maxSide: Int = 2048, quality: Int = 85): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!file.exists() || file.length() == 0L) return@withContext false

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext false

            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2

            val decoded = BitmapFactory.decodeFile(
                file.absolutePath,
                BitmapFactory.Options().apply { inSampleSize = sample }
            ) ?: return@withContext false

            val rotation = when (ExifInterface(file.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            val longest = maxOf(decoded.width, decoded.height)
            val scale = if (longest > maxSide) maxSide.toFloat() / longest else 1f
            val matrix = Matrix().apply {
                if (scale < 1f) postScale(scale, scale)
                if (rotation != 0f) postRotate(rotation)
            }
            val result = if (scale < 1f || rotation != 0f) {
                Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            } else decoded

            val tmp = File(file.parentFile, "${file.name}.tmp")
            tmp.outputStream().use { out -> result.compress(Bitmap.CompressFormat.JPEG, quality, out) }
            if (result !== decoded) result.recycle()
            decoded.recycle()

            // Sustitución segura: solo si la nueva imagen se ha escrito bien
            if (tmp.length() > 0 && tmp.renameTo(file)) true else { tmp.delete(); false }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo comprimir ${file.name}", e)
            false
        }
    }
}
