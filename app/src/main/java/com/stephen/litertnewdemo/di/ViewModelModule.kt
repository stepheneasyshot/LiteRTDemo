package com.stephen.litertnewdemo.di

import com.stephen.litertnewdemo.MainViewModel
import com.stephen.litertnewdemo.usecase.CopyFileUsecase
import com.stephen.litertnewdemo.usecase.LiteRtLoadEngineUsecase
import org.koin.dsl.module


val viewModelModule = module {
    single { MainViewModel(get(), get()) }
    factory { CopyFileUsecase() }
    factory { LiteRtLoadEngineUsecase() }
}