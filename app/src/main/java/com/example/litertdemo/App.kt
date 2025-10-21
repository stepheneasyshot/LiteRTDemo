package com.example.litertdemo

import android.app.Application
import android.content.Context

class App: Application() {
    companion object{
        lateinit var instance: Application
    }

    init {
        instance = this
    }
}

val appContext: Context = App.instance.applicationContext