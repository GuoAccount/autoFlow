package com.g2s.autoflow.core.task.impl

import com.g2s.autoflow.core.task.Task

/**
 * 基础任务类
 */
abstract class BaseTask(
    override val id: String,
    open val description: String = ""
) : Task {
    override val priority: Int = 5  // 默认优先级
    override val isBlocking: Boolean = true  // 默认阻塞式任务
}
