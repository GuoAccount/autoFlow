package com.g2s.autoflow.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

/**
 * 应用日志工具类
 */
object AppLogger {
    private const val TAG = "AutoFlow"
    private const val MAX_LOG_FILES = 7
    private const val MAX_LOG_SIZE = 5 * 1024 * 1024 // 5MB
    
    private var logDir: File? = null
    private var currentLogFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val ioExecutor = Executors.newSingleThreadExecutor()
    
    private var isInitialized = false
    
    /**
     * 初始化日志系统
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        try {
            logDir = File(context.getExternalFilesDir(null), "logs").apply {
                if (!exists()) mkdirs()
            }
            
            // 清理旧的日志文件
            cleanOldLogs()
            
            // 创建新的日志文件
            createNewLogFile()
            
            isInitialized = true
            d(TAG, "Logger initialized successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Failed to initialize logger", e)
        }
    }
    
    /**
     * 记录调试日志
     */
    fun d(tag: String, message: String) {
        Log.d(tag, message)
        logToFile("D", tag, message)
    }
    
    /**
     * 记录信息日志
     */
    fun i(tag: String, message: String) {
        Log.i(tag, message)
        logToFile("I", tag, message)
    }
    
    /**
     * 记录警告日志
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
        logToFile("W", tag, "$message\n${throwable?.stackTraceToString() ?: ""}")
    }
    
    /**
     * 记录错误日志
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        logToFile("E", tag, "$message\n${throwable?.stackTraceToString() ?: ""}")
    }
    
    /**
     * 记录崩溃信息
     */
    fun logCrash(throwable: Throwable) {
        val stackTrace = throwable.stackTraceToString()
        e("Crash", "Application crash detected: ${throwable.message}\n$stackTrace")
    }
    
    private fun logToFile(level: String, tag: String, message: String) {
        if (!isInitialized) return
        
        val timestamp = dateFormat.format(Date())
        val logMessage = "$timestamp $level/$tag: $message\n"
        
        // 异步写入文件
        ioExecutor.execute {
            try {
                // 检查日志文件大小
                if (currentLogFile?.length() ?: 0 > MAX_LOG_SIZE) {
                    createNewLogFile()
                }
                
                // 写入日志
                currentLogFile?.appendText(logMessage)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write log to file", e)
            }
        }
    }
    
    private fun createNewLogFile() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        currentLogFile = File(logDir, "autoflow_${timestamp}.log")
    }
    
    private fun cleanOldLogs() {
        logDir?.listFiles()
            ?.filter { it.name.startsWith("autoflow_") && it.name.endsWith(".log") }
            ?.sortedBy { it.lastModified() }
            ?.let { files ->
                if (files.size > MAX_LOG_FILES) {
                    files.take(files.size - MAX_LOG_FILES).forEach { it.delete() }
                }
            }
    }
}
