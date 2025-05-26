package com.g2s.autoflow.core.script.model

import com.g2s.autoflow.core.script.converter.DefaultTaskConverter
import com.g2s.autoflow.core.script.converter.TaskConverter
import com.g2s.autoflow.core.task.Task
import com.g2s.autoflow.core.task.impl.TaskGroupWrapper
import com.g2s.autoflow.utils.AppLogger
import kotlinx.coroutines.delay

/**
 * 任务脚本数据类
 * 支持简洁的配置方式，同时保持扩展性
 */
data class TaskScript(
    /**
     * 应用信息
     */
    val app: AppInfo = AppInfo(),
    
    /**
     * 全局配置
     */
    val config: Config = Config(),
    
    /**
     * 任务组
     */
    val tasks: List<TaskGroup> = emptyList(),
    
    /**
     * 变量定义
     */
    val variables: Map<String, Any> = emptyMap()
) {
    /**
     * 应用信息
     */
    data class AppInfo(
        /**
         * 包名
         */
        val packageName: String = "",
        
        /**
         * 应用名称
         */
        val name: String = "",
        
        /**
         * 主活动类名
         */
        val mainActivity: String = "",
        
        /**
         * 图标URL
         */
        val icon: String = ""
    )
    
    /**
     * 全局配置
     */
    data class Config(
        /**
         * 默认点击后等待时间(ms)
         */
        val defaultWaitAfter: Long = 500,
        
        /**
         * 默认超时时间(ms)
         */
        val defaultTimeout: Long = 10000,
        
        /**
         * 默认滑动持续时间(ms)
         */
        val defaultSwipeDuration: Long = 500,
        
        /**
         * 默认重试次数
         */
        val defaultRetryCount: Int = 3
    )
    
    /**
     * 任务组
     */
    data class TaskGroup(
        /**
         * 任务组名称
         */
        val name: String,
        
        /**
         * 任务组描述
         */
        val description: String = "",
        
        /**
         * 是否启用
         */
        val enabled: Boolean = true,
        
        /**
         * 任务列表
         */
        val steps: List<Step> = emptyList(),
        
        /**
         * 执行条件
         */
        val condition: String? = null,
        
        /**
         * 执行次数（-1表示无限循环）
         */
        val repeat: Int = 1,
        
        /**
         * 执行间隔(ms)
         */
        val interval: Long = 0,
        
        /**
         * 超时时间(ms)
         */
        val timeout: Long = 30000
    )
    

    
    /**
     * 将脚本转换为任务列表
     * @param converter 任务转换器，默认为 DefaultTaskConverter
     * @return 转换后的任务列表
     */
    fun toTasks(converter: TaskConverter = DefaultTaskConverter()): List<Task> {
        return tasks.flatMap { taskGroup ->
            if (!taskGroup.enabled) return@flatMap emptyList<Task>()
            
            val groupTasks = taskGroup.steps.flatMap { step ->
                try {
                    converter.convert(step, variables)
                } catch (e: Exception) {
                    emptyList<Task>().also {
                        AppLogger.e("TaskScript", "Failed to convert step: ${step::class.simpleName}", e)
                    }
                }
            }
            
            // 添加任务组控制任务
            val wrappedTasks = mutableListOf<Task>()
            if (taskGroup.condition != null || taskGroup.repeat != 1) {
                wrappedTasks.add(
                    TaskGroupWrapper(
                        id = "group_${taskGroup.name}",
                        tasks = groupTasks,
                        condition = taskGroup.condition,
                        repeat = taskGroup.repeat,
                        interval = taskGroup.interval,
                        timeout = taskGroup.timeout
                    )
                )
            } else {
                wrappedTasks.addAll(groupTasks)
            }
            
            wrappedTasks
        }
    }
    
    /**
     * 脚本元数据
     */
    data class Metadata(
        /**
         * 脚本名称
         */
        val name: String = "",
        
        /**
         * 脚本版本
         */
        val version: String = "1.0",
        
        /**
         * 脚本描述
         */
        val description: String = "",
        
        /**
         * 作者
         */
        val author: String = "",
        
        /**
         * 创建时间
         */
        val createdAt: String = ""
    )
    
    /**
     * 任务步骤
     */
    sealed class Step {
        /**
         * 点击操作
         */
        data class Click(
            /**
             * 目标元素ID
             */
            val targetId: String? = null,
            
            /**
             * 目标文本
             */
            val text: String? = null,
            
            /**
             * 目标描述
             */
            val contentDescription: String? = null,
            
            /**
             * 点击后等待时间（毫秒）
             */
            val waitAfter: Long = 500,
            
            /**
             * 最大等待时间（毫秒）
             */
            val timeout: Long = 10000,
            
            /**
             * 是否使用OCR识别
             */
            val useOcr: Boolean = false,
            
            /**
             * 步骤描述
             */
            val description: String = ""
        ) : Step()
        
        /**
         * 输入文本
         */
        data class Input(
            /**
             * 目标元素ID
             */
            val targetId: String? = null,
            
            /**
             * 目标文本
             */
            val text: String? = null,
            
            /**
             * 输入内容
             */
            val input: String,
            
            /**
             * 是否追加输入
             */
            val append: Boolean = false,
            
            /**
             * 输入后等待时间（毫秒）
             */
            val waitAfter: Long = 500,
            
            /**
             * 步骤描述
             */
            val description: String = ""
        ) : Step()
        
        /**
         * 滑动操作
         */
        data class Swipe(
            /**
             * 起始X坐标
             */
            val startX: Int,
            
            /**
             * 起始Y坐标
             */
            val startY: Int,
            
            /**
             * 结束X坐标
             */
            val endX: Int,
            
            /**
             * 结束Y坐标
             */
            val endY: Int,
            
            /**
             * 滑动持续时间（毫秒）
             */
            val duration: Long = 500,
            
            /**
             * 滑动后等待时间（毫秒）
             */
            val waitAfter: Long = 500,
            
            /**
             * 步骤描述
             */
            val description: String = ""
        ) : Step()
        
        /**
         * 等待操作
         */
        data class Wait(
            /**
             * 等待时间（毫秒）
             */
            val milliseconds: Long,
            
            /**
             * 步骤描述
             */
            val description: String = ""
        ) : Step()
        
        /**
         * 条件判断
         */
        data class Condition(
            /**
             * 条件表达式
             */
            val condition: String,
            
            /**
             * 条件为真时执行的步骤
             */
            val then: List<Step> = emptyList(),
            
            /**
             * 条件为假时执行的步骤
             */
            val `else`: List<Step> = emptyList(),
            
            /**
             * 步骤描述
             */
            val description: String = ""
        ) : Step()
        
        /**
         * 循环执行
         */
        data class Loop(
            /**
             * 循环次数，-1表示无限循环
             */
            val times: Int = -1,
            
            /**
             * 循环条件
             */
            val `while`: String? = null,
            
            /**
             * 循环步骤
             */
            val steps: List<Step>,
            
            /**
             * 步骤描述
             */
            val description: String = ""
        ) : Step()
        
        /**
         * 执行Shell命令
         */
        data class Shell(
            /**
             * 要执行的命令
             */
            val command: String,
            
            /**
             * 是否使用root权限
             */
            val useRoot: Boolean = false,
            
            /**
             * 超时时间（毫秒）
             */
            val timeout: Long = 10000,
            
            /**
             * 步骤描述
             */
            val description: String = ""
        ) : Step()
    }
    

}
