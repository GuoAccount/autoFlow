package com.g2s.autoflow.core.task

/**
 * 基础任务接口
 */
interface Task {
    val id: String
    val priority: Int
    val isBlocking: Boolean
    
    suspend fun execute()
}

/**
 * 任务执行结果
 */
data class TaskResult(
    val taskId: String,
    val isSuccess: Boolean,
    val message: String = "",
    val data: Any? = null
)

/**
 * 任务执行器
 */
interface TaskExecutor {
    suspend fun execute(task: Task): TaskResult
    fun cancel(taskId: String)
    fun shutdown()
}

/**
 * 任务队列
 */
interface TaskQueue {
    fun addTask(task: Task)
    fun getNextTask(): Task?
    fun removeTask(taskId: String): Boolean
    fun clear()
}

/**
 * 任务优先级
 */
object Priority {
    const val LOW = 0
    const val NORMAL = 5
    const val HIGH = 10
}
