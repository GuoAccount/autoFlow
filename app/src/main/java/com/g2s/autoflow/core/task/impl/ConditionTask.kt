package com.g2s.autoflow.core.task.impl

import com.g2s.autoflow.core.task.Task
import com.g2s.autoflow.core.task.TaskExecutor
import com.g2s.autoflow.utils.AppLogger

/**
 * 条件任务
 */
class ConditionTask(
    override val id: String,
    val condition: String,
    val thenTasks: List<Task>,
    val elseTasks: List<Task>,
    description: String = "",
    override val priority: Int = 5,
    override val isBlocking: Boolean = true
) : BaseTask(id, description) {
    
    private val tag = "ConditionTask"
    
    override suspend fun execute() {
        AppLogger.d(tag, "Executing condition task: $description")
        
        val conditionMet = evaluateCondition(condition)
        val tasksToExecute = if (conditionMet) thenTasks else elseTasks
        
        AppLogger.d(tag, "Condition '$condition' evaluated to: $conditionMet")
        
        if (tasksToExecute.isNotEmpty()) {
            // 使用协程并发执行所有任务
            tasksToExecute.forEach { task ->
                task.execute()
            }
        }
        
        AppLogger.d(tag, "Condition task completed")
    }
    
    private fun evaluateCondition(condition: String): Boolean {
        // TODO: 实现条件表达式求值
        // 这里可以集成表达式求值库，如 exp4j 或自己实现简单的条件解析
        // 目前只实现简单的 true/false 判断
        return condition.trim().equals("true", ignoreCase = true)
    }
}
