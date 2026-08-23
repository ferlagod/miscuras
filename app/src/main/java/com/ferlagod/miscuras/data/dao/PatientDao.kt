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
package com.ferlagod.miscuras.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ferlagod.miscuras.data.entities.EvaluationEntity
import com.ferlagod.miscuras.data.entities.PatientEntity
import com.ferlagod.miscuras.data.entities.WoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
/**
 * Data Access Object (DAO) para interactuar con la tabla de pacientes.
 */
interface PatientDao {
    @Insert
    fun insertPatient(patient: PatientEntity): Long

    @Query("SELECT * FROM pacientes ORDER BY createdAt DESC")
    fun getAllPatients(): Flow<List<PatientEntity>>

    @Insert
    fun insertWound(wound: WoundEntity): Long

    @Query("SELECT * FROM heridas WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getWoundsForPatient(patientId: Long): Flow<List<WoundEntity>>
    
    @Query("SELECT * FROM heridas WHERE id = :woundId")
    fun getWoundById(woundId: Long): WoundEntity?

    @Insert
    fun insertEvaluation(evaluation: EvaluationEntity): Long

    @Query("SELECT * FROM evaluaciones WHERE woundId = :woundId ORDER BY timestamp ASC")
    fun getEvaluationsForWound(woundId: Long): Flow<List<EvaluationEntity>>

    @Query("SELECT * FROM evaluaciones WHERE woundId = :woundId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestEvaluationForWound(woundId: Long): EvaluationEntity?

    // --- Backup/Restore Methods ---
    
    @Query("SELECT * FROM pacientes")
    suspend fun getAllPatientsSync(): @JvmSuppressWildcards List<PatientEntity>

    @Query("SELECT * FROM heridas")
    suspend fun getAllWoundsSync(): @JvmSuppressWildcards List<WoundEntity>

    @Query("SELECT * FROM evaluaciones")
    suspend fun getAllEvaluationsSync(): @JvmSuppressWildcards List<EvaluationEntity>

    @Query("DELETE FROM pacientes")
    suspend fun deleteAllPatients(): @JvmSuppressWildcards Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatients(patients: List<PatientEntity>): @JvmSuppressWildcards List<Long>

    @Update
    suspend fun updatePatient(patient: PatientEntity): @JvmSuppressWildcards Int



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWounds(wounds: List<WoundEntity>): @JvmSuppressWildcards List<Long>

    @Update
    suspend fun updateWound(wound: WoundEntity): @JvmSuppressWildcards Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluations(evaluations: List<EvaluationEntity>): @JvmSuppressWildcards List<Long>
}
