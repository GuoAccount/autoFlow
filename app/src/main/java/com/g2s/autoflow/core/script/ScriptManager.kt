package com.g2s.autoflow.core.script

import com.g2s.autoflow.core.script.exception.ScriptParseException
import com.g2s.autoflow.core.script.model.TaskScript
import com.g2s.autoflow.core.script.parser.ScriptParserFactory
import com.g2s.autoflow.core.task.Task
import com.g2s.autoflow.core.task.TaskExecutor
import com.g2s.autoflow.utils.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * 脚本管理器
 */
class ScriptManager(
    private val taskExecutor: TaskExecutor
) {
    private val tag = "ScriptManager"
    private val scope = CoroutineScope(Dispatchers.IO)
    
    /**
     * 加载并执行脚本文件
     * @param file 脚本文件
     * @param onSuccess 执行成功回调
     * @param onError 执行失败回调
     */
    fun loadAndExecuteScript(
        file: File,
        onSuccess: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null
    ) {
        if (!file.exists() || !file.isFile) {
            val error = IllegalArgumentException("File not found: ${file.absolutePath}")
            onError?.invoke(error)
            return
        }
        
        scope.launch {
            try {
                val script = parseScript(file)
                executeScript(script)
                onSuccess?.invoke()
            } catch (e: Exception) {
                AppLogger.e(tag, "Failed to execute script: ${file.name}", e)
                onError?.invoke(e)
            }
        }
    }
    
    /**
     * 解析脚本文件
     * @param file 脚本文件
     * @return 解析后的任务脚本
     * @throws ScriptParseException 当解析失败时抛出
     */
    @Throws(ScriptParseException::class)
    suspend fun parseScript(file: File): TaskScript {
        return try {
            val parser = ScriptParserFactory.getParserForFile(file.name)
            val content = file.readText()
            parser.parse(content)
        } catch (e: Exception) {
            throw ScriptParseException("Failed to parse script: ${file.name}", e)
        }
    }
    
    /**
     * 执行脚本
     * @param script 任务脚本
     */
    suspend fun executeScript(script: TaskScript) {
        // 将脚本转换为任务并执行
        val tasks = script.toTasks()
        tasks.forEach { task: Task ->
            taskExecutor.execute(task)
        }
    }
    
    /**
     * 取消当前正在执行的所有任务
     */
    fun cancelAll() {
        // 这里需要实现取消逻辑
        // 可以通过taskExecutor来取消所有任务
    }
}
