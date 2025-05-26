package com.g2s.autoflow.core.task.impl

import com.g2s.autoflow.core.task.Task

/**
 * 任务组包装器，用于包装一组任务
 */
class TaskGroupWrapper(
    override val id: String,
    val tasks: List<Task>,
    val condition: String? = null,
    val repeat: Int = 1,
    val interval: Long = 0,
    val timeout: Long = 0,
    override val priority: Int = 5,
    override val isBlocking: Boolean = true
) : Task {
    override suspend fun execute() {
        // 检查条件
        if (condition != null && !evaluateCondition(condition)) {
            return
        }
        
        var count = 0
        while ((repeat <= 0 || count < repeat) && 
               (condition == null || evaluateCondition(condition))) {
            // 执行所有任务
            tasks.forEach { task ->
                task.execute()
                
                // 执行间隔
                if (interval > 0) {
                    kotlinx.coroutines.delay(interval)
                }
            }
            
            count++
            
            // 循环间隔
            if (repeat > 0 && count < repeat && interval > 0) {
                kotlinx.coroutines.delay(interval)
            }
        }
    }
    
    private fun evaluateCondition(condition: String): Boolean {
        try {
            // 简单实现：检查条件是否为 "true" 或 "1"
            return condition.trim().equals("true", ignoreCase = true) || 
                   condition.trim() == "1"
        } catch (e: Exception) {
            return false
        }
    }
    
    override fun toString(): String {
        return "TaskGroup(id='$id', tasks=${tasks.size})"
    }
}
