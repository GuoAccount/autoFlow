package com.g2s.autoflow.core.task.impl

import com.g2s.autoflow.core.task.Task
import com.g2s.autoflow.utils.AppLogger
import kotlinx.coroutines.delay

/**
 * 等待任务
 */
class WaitTask(
    override val id: String,
    val milliseconds: Long,
    override val description: String = "",
    override val priority: Int = 1,  // 低优先级
    override val isBlocking: Boolean = false  // 非阻塞式任务
) : BaseTask(id, description) {
    
    private val tag = "WaitTask"
    
    override suspend fun execute() {
        AppLogger.d(tag, "Executing wait task: ${milliseconds}ms - $description")
        delay(milliseconds)
        AppLogger.d(tag, "Wait task completed")
    }
}
