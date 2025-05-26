package com.g2s.autoflow.core.script.converter

import com.g2s.autoflow.core.script.model.TaskScript
import com.g2s.autoflow.core.task.Task

/**
 * 任务转换器接口
 */
interface TaskConverter {
    /**
     * 将脚本步骤转换为任务
     * @param step 脚本步骤
     * @param variables 变量映射
     * @return 转换后的任务列表
     */
    fun convert(step: TaskScript.Step, variables: Map<String, Any> = emptyMap()): List<Task>
    
    /**
     * 替换变量
     * @param input 输入字符串
     * @param variables 变量映射
     * @return 替换变量后的字符串
     */
    fun replaceVariables(input: String, variables: Map<String, Any>): String {
        var result = input
        variables.forEach { (key, value) ->
            result = result.replace("\${$key}", value.toString())
        }
        return result
    }
}
