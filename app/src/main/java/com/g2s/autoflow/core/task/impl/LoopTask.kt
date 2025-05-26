package com.g2s.autoflow.core.task.impl

import com.g2s.autoflow.core.task.Task
import com.g2s.autoflow.core.task.TaskExecutor
import com.g2s.autoflow.utils.AppLogger

/**
 * 循环任务
 */
class LoopTask(
    override val id: String,
    val times: Int = -1,  // -1 表示无限循环
    val condition: String? = null,  // 循环条件
    val tasks: List<Task>,
    override val description: String = "",
    override val priority: Int = 5,
    override val isBlocking: Boolean = true
) : BaseTask(id, description) {
    
    private val tag = "LoopTask"
    
    override suspend fun execute() {
        AppLogger.d(tag, "Executing loop task: $description")
        
        var count = 0
        var shouldContinue = true
        
        while (shouldContinue) {
            // 检查循环次数限制
            if (times > 0 && count >= times) {
                AppLogger.d(tag, "Loop completed after $count iterations (times limit)")
                break
            }
            
            // 检查条件
            if (condition != null) {
                val conditionMet = evaluateCondition(condition)
                if (!conditionMet) {
                    AppLogger.d(tag, "Loop condition no longer met, stopping after $count iterations")
                    break
                }
            }
            
            // 执行任务
            AppLogger.d(tag, "Loop iteration ${count + 1}...")
            tasks.forEach { task ->
                task.execute()
            }
            
            count++
            
            // 对于无限循环，添加一个小延迟，避免CPU占用过高
            if (times == -1) {
                kotlinx.coroutines.delay(100)
            }
        }
        
        AppLogger.d(tag, "Loop task completed after $count iterations")
    }
    
    private fun evaluateCondition(condition: String): Boolean {
        // TODO: 实现条件表达式求值
        // 这里可以集成表达式求值库，如 exp4j 或自己实现简单的条件解析
        // 目前只实现简单的 true/false 判断
        return condition.trim().equals("true", ignoreCase = true)
    }
}
