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
package com.ferlagod.miscuras.di

import android.content.Context
import android.content.SharedPreferences
import com.ferlagod.miscuras.data.database.AppDatabase
import com.ferlagod.miscuras.data.repository.ApositosRepository
import com.ferlagod.miscuras.data.repository.FeedbackRepository
import com.ferlagod.miscuras.domain.rules.RulesEngine
import com.ferlagod.miscuras.domain.usecase.EvaluateWoundUseCase
import com.ferlagod.miscuras.network.NetworkClient
import com.ferlagod.miscuras.ui.WoundViewModel
import com.ferlagod.miscuras.ui.viewmodels.PatientViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Módulo principal de inyección de dependencias utilizando Koin.
 * Provee las instancias únicas (singletons) para la base de datos, repositorios,
 * preferencias compartidas, casos de uso y los ViewModels de la aplicación.
 */
val appModule = module {
    // Database
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().apositoDao() }
    single { get<AppDatabase>().aiCacheDao() }
    single { get<AppDatabase>().patientDao() }

    // SharedPreferences
    single<SharedPreferences> { 
        androidContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
    }

    // API
    single { NetworkClient.formSubmitApi }

    // Repositories
    single { ApositosRepository(get(), get()) }
    single { FeedbackRepository(get()) }

    // Use Cases & Engines
    single { RulesEngine() }
    single { EvaluateWoundUseCase(get(), get()) }

    // ViewModels
    viewModelOf(::WoundViewModel)
    viewModelOf(::PatientViewModel)
}
