package com.ferlagod.miscuras.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.ferlagod.miscuras.data.entities.EvaluationEntity
import com.ferlagod.miscuras.data.entities.PatientEntity
import com.ferlagod.miscuras.data.entities.WoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
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
}
