package com.stephen.litertnewdemo

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.stephen.litertnewdemo.usecase.CopyFileUsecase
import com.stephen.litertnewdemo.usecase.LiteRtLoadEngineUsecase
import com.stephen.litertnewdemo.utils.infoLog

class MainViewModel(
    val copyFileUsecase: CopyFileUsecase,
    val liteRtLoadEngineUsecase: LiteRtLoadEngineUsecase,
) : ViewModel() {

    fun fetchInternalFiles(context: Context) = copyFileUsecase.fetchInternalFiles(context)

    suspend fun copyFileWithProgress(
        context: Context,
        uri: Uri,
        onProgress: (Float) -> Unit
    ) = copyFileUsecase.copyFileWithProgress(context, uri, onProgress)

    fun loadLiteRtEngine(path: String, onFinished: () -> Unit) {
        infoLog("[loadLiteRtEngine] path: $path")
        liteRtLoadEngineUsecase.setLiteRtLmFilePath(path)
        liteRtLoadEngineUsecase.initializeModel {
            onFinished()
        }
    }

    fun startConversation() {
        liteRtLoadEngineUsecase.startConversationTest()
    }

    fun closeLiteRtEngine() {
        infoLog()
        liteRtLoadEngineUsecase.close()
    }
}