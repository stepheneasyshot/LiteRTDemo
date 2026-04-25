package com.stephen.litertnewdemo.usecase

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class CopyFileUsecase {

    /**
     * 获取应用私有目录下所有 .litertlm 后缀的文件
     */
    fun fetchInternalFiles(context: Context): List<File> {
        val dir = context.filesDir
        return dir.listFiles { _, name -> name.endsWith(".litertlm") }?.toList() ?: emptyList()
    }

    suspend fun copyFileWithProgress(
        context: Context,
        uri: Uri,
        onProgress: (Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver

            // 1. 获取源文件的原始名称
            var fileName = "temp_file.litertlm"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }

            // 2. 获取文件大小用于计算进度
            val fileSize =
                contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L

            // 3. 准备目标文件
            val targetFile = File(context.filesDir, fileName)

            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(64 * 1024) // 64KB 缓冲区提升性能
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    var lastUpdatePercent = 0f

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        if (fileSize > 0) {
                            val currentPercent = totalBytesRead.toFloat() / fileSize
                            // 阈值过滤：进度变化超过 1% 才切换到主线程更新 UI，大幅减少线程调度开销
                            if (currentPercent - lastUpdatePercent >= 0.01f || currentPercent >= 1f) {
                                lastUpdatePercent = currentPercent
                                withContext(Dispatchers.Main) {
                                    onProgress(currentPercent)
                                }
                            }
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}