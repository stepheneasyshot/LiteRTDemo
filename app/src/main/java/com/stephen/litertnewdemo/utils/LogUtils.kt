package com.stephen.litertnewdemo.utils

import android.util.Log

object LogSetting {

    const val LOG_VERBOSE = 1
    const val LOG_DEBUG = 2
    const val LOG_INFO = 3
    const val LOG_WARNING = 4
    const val LOG_ERROR = 5

    var COMMON_TAG = "RedfinDemo"
        private set

    var LOGLEVEL = 0
        private set

    /**
     * 大于此LEVEL的才会打出来
     */
    fun initLogSettings(tagPreffix: String, logLevel: Int) {
        COMMON_TAG = tagPreffix
        LOGLEVEL = logLevel
    }
}

fun verboseLog(message: String? = "", tag: String? = null) {
    if (LogSetting.LOGLEVEL > LogSetting.LOG_VERBOSE) return
    tag?.let {
        printLog("$it $message", LogSetting.LOG_VERBOSE)
    } ?: run {
        printLog(
            "${getStackInfo(Thread.currentThread())} $message",
            LogSetting.LOG_VERBOSE
        )
    }
}

fun debugLog(message: String? = "", tag: String? = null) {
    if (LogSetting.LOGLEVEL > LogSetting.LOG_DEBUG) return
    tag?.let {
        printLog("$it $message", LogSetting.LOG_DEBUG)
    } ?: run {
        printLog(
            "${getStackInfo(Thread.currentThread())} $message",
            LogSetting.LOG_DEBUG
        )
    }
}

fun infoLog(message: String? = "", tag: String? = null) {
    if (LogSetting.LOGLEVEL > LogSetting.LOG_INFO) return
    tag?.let {
        printLog("$it $message", LogSetting.LOG_INFO)
    } ?: run {
        printLog(
            "${getStackInfo(Thread.currentThread())} $message",
            LogSetting.LOG_INFO
        )
    }
}

fun warningLog(message: String? = "", tag: String? = null) {
    if (LogSetting.LOGLEVEL > LogSetting.LOG_WARNING) return
    tag?.let {
        printLog("$it $message", LogSetting.LOG_WARNING)
    } ?: run {
        printLog(
            "${getStackInfo(Thread.currentThread())} $message",
            LogSetting.LOG_WARNING
        )
    }
}

fun errorLog(message: String? = "", tag: String? = null) {
    if (LogSetting.LOGLEVEL > LogSetting.LOG_ERROR) return
    tag?.let {
        printLog("$it $message", LogSetting.LOG_ERROR)
    } ?: run {
        printLog(
            "${getStackInfo(Thread.currentThread())} $message",
            LogSetting.LOG_ERROR
        )
    }
}

/**
 * 获取调用栈信息
 */
fun getStackInfo(thread: Thread): String {
    val threadInfo = "thread:${thread.name}(${thread.id})"
    val usefulStackTrace = thread.stackTrace[5]
    return "{$threadInfo ${
        usefulStackTrace.className.split(".").last()
    }:${usefulStackTrace.lineNumber} ${usefulStackTrace.methodName}}"
}


/**
 * 实际打印处，根据等级打印log
 */
fun printLog(message: String, logLevel: Int) {
    when (logLevel) {
        LogSetting.LOG_ERROR -> Log.e(LogSetting.COMMON_TAG, message)
        LogSetting.LOG_WARNING -> Log.w(LogSetting.COMMON_TAG, message)
        LogSetting.LOG_INFO -> Log.i(LogSetting.COMMON_TAG, message)
        LogSetting.LOG_DEBUG -> Log.d(LogSetting.COMMON_TAG, message)
        LogSetting.LOG_VERBOSE -> Log.v(LogSetting.COMMON_TAG, message)
    }
}