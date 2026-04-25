package com.stephen.litertnewdemo.utils

import android.widget.Toast
import com.stephen.litertnewdemo.appContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val mainContextCoroutine = CoroutineScope(Dispatchers.Main)

fun showToast(msg: String, duration: Int = Toast.LENGTH_SHORT) {
    if (msg.isBlank()) return
    mainContextCoroutine.launch {
        Toast.makeText(appContext, msg, duration).show()
    }
}