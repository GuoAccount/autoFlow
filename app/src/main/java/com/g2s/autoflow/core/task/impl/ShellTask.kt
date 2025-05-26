package com.g2s.autoflow.core.task.impl

import android.os.Handler
import android.os.Looper
import com.g2s.autoflow.core.task.Task
import com.g2s.autoflow.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

/**
 * Shell 命令任务
 */
class ShellTask(
    override val id: String,
    val command: String,
    val useRoot: Boolean = false,
    val timeout: Long = 10000,
    override val description: String = "",
    override val priority: Int = 5,
    override val isBlocking: Boolean = true
) : BaseTask(id, description) {
    
    private val tag = "ShellTask"
    private val mainHandler = Handler(Looper.getMainLooper())
    
    override suspend fun execute() {
        AppLogger.d(tag, "Executing shell command: $command")
        
        val process = if (useRoot) {
            Runtime.getRuntime().exec("su")
        } else {
            Runtime.getRuntime().exec("sh")
        }
        
        val output = StringBuilder()
        val error = StringBuilder()
        
        // 启动超时检查
        val timeoutRunnable = Runnable {
            try {
                process.destroy()
                AppLogger.e(tag, "Command timed out: $command")
            } catch (e: Exception) {
                AppLogger.e(tag, "Error while terminating timed out process", e)
            }
        }
        
        mainHandler.postDelayed(timeoutRunnable, timeout)
        
        try {
            // 执行命令
            val outputStream = DataOutputStream(process.outputStream)
            outputStream.write("$command\n".toByteArray())
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            
            // 读取输出
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            
            // 在 IO 调度器上读取输出，避免阻塞主线程
            withContext(Dispatchers.IO) {
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.append("$line\n")
                }
                
                while (errorReader.readLine().also { line = it } != null) {
                    error.append("$line\n")
                }
            }
            
            val exitCode = process.waitFor()
            
            // 取消超时检查
            mainHandler.removeCallbacks(timeoutRunnable)
            
            if (exitCode != 0) {
                throw RuntimeException("Command failed with exit code $exitCode: ${error.toString().trim()}")
            }
            
            AppLogger.d(tag, "Command executed successfully. Output: ${output.toString().trim()}")
            
        } finally {
            try {
                process.destroy()
            } catch (e: Exception) {
                AppLogger.e(tag, "Error while destroying process", e)
            }
        }
    }
}
