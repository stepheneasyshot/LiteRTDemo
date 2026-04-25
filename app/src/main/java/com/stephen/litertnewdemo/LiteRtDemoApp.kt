package com.stephen.litertnewdemo

import android.app.Application
import com.stephen.litertnewdemo.utils.LogSetting
import com.stephen.litertnewdemo.utils.infoLog
import com.stephen.litertnewdemo.di.appModule
import org.koin.core.context.startKoin

class LiteRtDemoApp: Application() {

    companion object {
        lateinit var instance: LiteRtDemoApp
    }

    override fun onCreate() {
        super.onCreate()

        LogSetting.initLogSettings("IntelligenceEngineApp[${BuildConfig.VERSION_NAME}]", LogSetting.LOG_DEBUG)
        infoLog()

        instance = this

        startKoin {
            modules(appModule)
        }
    }
}

val appContext = LiteRtDemoApp.instance.applicationContext

val appRes = LiteRtDemoApp.instance.resources