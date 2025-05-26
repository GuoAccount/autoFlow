package com.g2s.autoflow.core.task

import com.g2s.autoflow.utils.AppLogger
import kotlinx.coroutines.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * 默认任务执行器实现
 */
class DefaultTaskExecutor : TaskExecutor {
    private val tag = "TaskExecutor"
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // 任务队列
    private val taskQueue = PriorityBlockingQueue<TaskItem>()
    
    // 工作线程池
    private val executor = ThreadPoolExecutor(
        CORE_POOL_SIZE,
        MAX_POOL_SIZE,
        KEEP_ALIVE_TIME, TimeUnit.SECONDS,
        PriorityBlockingQueue(),
        TaskThreadFactory()
    )
    
    // 任务映射表
    private val taskMap = ConcurrentHashMap<String, Job>()
    
    init {
        // 启动任务调度协程
        scope.launch {
            while (isActive) {
                try {
                    // 从队列中获取任务
                    val taskItem = taskQueue.take()
                    
                    // 提交到线程池执行
                    val job = scope.launch(Dispatchers.IO) {
                        try {
                            AppLogger.d(tag, "Executing task: ${taskItem.task.id}")
                            taskItem.task.execute()
                            AppLogger.d(tag, "Task completed: ${taskItem.task.id}")
                        } catch (e: Exception) {
                            AppLogger.e(tag, "Task execution failed: ${taskItem.task.id}", e)
                        } finally {
                            // 从映射表中移除已完成的任务
                            taskMap.remove(taskItem.task.id)
                        }
                    }
                    
                    // 保存任务引用
                    taskMap[taskItem.task.id] = job
                    
                    // 等待任务完成（如果是阻塞任务）
                    if (taskItem.task.isBlocking) {
                        job.join()
                    }
                } catch (e: InterruptedException) {
                    // 线程被中断，退出循环
                    break
                } catch (e: Exception) {
                    AppLogger.e(tag, "Error in task scheduler", e)
                }
            }
        }
    }
    
    override suspend fun execute(task: Task): TaskResult {
        return withContext(Dispatchers.IO) {
            try {
                // 如果任务已存在，返回当前任务
                if (taskMap.containsKey(task.id)) {
                    return@withContext TaskResult(task.id, false, "Task with id ${task.id} already exists")
                }
                
                // 创建任务项
                val taskItem = TaskItem(task, task.priority)
                
                // 添加到队列
                taskQueue.put(taskItem)
                
                TaskResult(task.id, true, "Task ${task.id} added to queue")
            } catch (e: Exception) {
                AppLogger.e(tag, "Failed to execute task: ${task.id}", e)
                TaskResult(task.id, false, e.message ?: "Unknown error")
            }
        }
    }
    
    override fun cancel(taskId: String) {
        taskMap[taskId]?.cancel()
        taskMap.remove(taskId)
        
        // 从队列中移除未执行的任务
        taskQueue.removeIf { it.task.id == taskId }
    }
    
    override fun shutdown() {
        // 取消所有任务
        taskMap.values.forEach { it.cancel() }
        taskMap.clear()
        taskQueue.clear()
        
        // 关闭线程池
        executor.shutdownNow()
        
        // 取消协程作用域
        scope.cancel()
    }
    
    /**
     * 任务包装类，用于优先级队列
     */
    private data class TaskItem(
        val task: Task,
        val priority: Int
    ) : Comparable<TaskItem> {
        override fun compareTo(other: TaskItem): Int {
            // 优先级高的排在前面
            return other.priority - this.priority
        }
    }
    
    /**
     * 线程工厂，设置线程名称和优先级
     */
    private class TaskThreadFactory : ThreadFactory {
        private val threadNumber = AtomicInteger(1)
        
        override fun newThread(r: Runnable): Thread {
            val thread = Thread(r, "TaskThread-${threadNumber.getAndIncrement()}")
            thread.priority = Thread.NORM_PRIORITY
            thread.isDaemon = true
            return thread
        }
    }
    
    companion object {
        private const val CORE_POOL_SIZE = 2
        private const val MAX_POOL_SIZE = 4
        private const val KEEP_ALIVE_TIME = 60L // 60秒
    }
}
