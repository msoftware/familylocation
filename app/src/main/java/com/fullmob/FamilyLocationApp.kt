package com.fullmob

import android.app.Application
import com.fullmob.familylocation.network.CellTowersInfoProvider
import com.fullmob.familylocation.di.MainModule
import com.fullmob.familylocation.network.WifiNetworksInfoProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FamilyLocationApp : Application() {

    lateinit var cellTowerProvider: CellTowersInfoProvider
    lateinit var wifiProvider: WifiNetworksInfoProvider

    override fun onCreate() {
        super.onCreate()
        initDepInjection()
    }

    fun initDepInjection() {
        cellTowerProvider = CellTowersInfoProvider(this)
        wifiProvider = WifiNetworksInfoProvider(this)
        startKoin {
            // declare used Android context
            androidContext(this@FamilyLocationApp)
            // declare modules
            modules(MainModule)
        }
    }


}