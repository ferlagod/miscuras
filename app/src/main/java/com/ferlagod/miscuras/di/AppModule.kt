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
