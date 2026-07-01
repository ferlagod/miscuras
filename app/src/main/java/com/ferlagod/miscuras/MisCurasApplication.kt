package com.ferlagod.miscuras

import android.app.Application
import com.ferlagod.miscuras.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MisCurasApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@MisCurasApplication)
            modules(appModule)
        }
    }
}
