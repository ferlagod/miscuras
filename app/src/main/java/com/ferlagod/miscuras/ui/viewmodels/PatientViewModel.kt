package com.ferlagod.miscuras.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ferlagod.miscuras.data.database.AppDatabase
import com.ferlagod.miscuras.data.entities.EvaluationEntity
import com.ferlagod.miscuras.data.entities.PatientEntity
import com.ferlagod.miscuras.data.entities.WoundEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class PatientViewModel(application: Application) : AndroidViewModel(application) {
    private val patientDao = AppDatabase.getDatabase(application).patientDao()

    private val _patients = MutableStateFlow<List<PatientEntity>>(emptyList())
    val patients: StateFlow<List<PatientEntity>> = _patients.asStateFlow()

    private val _currentPatientWounds = MutableStateFlow<List<WoundEntity>>(emptyList())
    val currentPatientWounds: StateFlow<List<WoundEntity>> = _currentPatientWounds.asStateFlow()

    private val _currentWoundEvaluations = MutableStateFlow<List<EvaluationEntity>>(emptyList())
    val currentWoundEvaluations: StateFlow<List<EvaluationEntity>> = _currentWoundEvaluations.asStateFlow()
    
    private val _currentWound = MutableStateFlow<WoundEntity?>(null)
    val currentWound: StateFlow<WoundEntity?> = _currentWound.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            patientDao.getAllPatients()
                .catch { e -> e.printStackTrace() }
                .collect { list ->
                    _patients.value = list
                }
        }
    }

    fun loadWoundsForPatient(patientId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            patientDao.getWoundsForPatient(patientId)
                .catch { e -> e.printStackTrace() }
                .collect { list ->
                    _currentPatientWounds.value = list
                }
        }
    }

    fun loadEvaluationsForWound(woundId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _currentWound.value = patientDao.getWoundById(woundId)
            patientDao.getEvaluationsForWound(woundId)
                .catch { e -> e.printStackTrace() }
                .collect { list ->
                    _currentWoundEvaluations.value = list
                }
        }
    }

    fun addPatient(name: String, room: String) {
        viewModelScope.launch(Dispatchers.IO) {
            patientDao.insertPatient(PatientEntity(anonymizedName = name, roomNumber = room))
        }
    }

    fun addWound(patientId: Long, woundName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            patientDao.insertWound(WoundEntity(patientId = patientId, name = woundName))
        }
    }
}
