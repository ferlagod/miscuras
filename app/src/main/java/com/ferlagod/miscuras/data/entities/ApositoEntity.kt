package com.ferlagod.miscuras.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa un producto (apósito) en la base de datos local.
 * Contiene información clínica y comercial sobre cada apósito disponible.
 */
@Entity(tableName = "ProductosApositos")
data class ApositoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "nombre_comercial") val nombreComercial: String,
    @ColumnInfo(name = "fabricante") val fabricante: String,
    @ColumnInfo(name = "familia_generica") val familiaGenerica: String,
    @ColumnInfo(name = "dimensiones") val dimensiones: String,
    @ColumnInfo(name = "imagen_url") val imagenUrl: String,
    @ColumnInfo(name = "codigo_cn") val codigoCn: String,
    @ColumnInfo(name = "descripcion") val descripcion: String,
    @ColumnInfo(name = "interacciones") val interacciones: String,
    @ColumnInfo(name = "uso_primario_secundario") val usoPrimarioSecundario: String
)