package com.g2s.autoflow.core.script.parser

import com.g2s.autoflow.core.script.exception.ScriptParseException
import com.g2s.autoflow.core.script.model.TaskScript

/**
 * 脚本解析器接口
 */
interface ScriptParser {
    /**
     * 解析脚本内容
     * @param content 脚本内容
     * @return 解析后的任务脚本对象
     * @throws ScriptParseException 当解析失败时抛出
     */
    @Throws(ScriptParseException::class)
    fun parse(content: String): TaskScript
    
    /**
     * 检查是否支持指定格式
     * @param format 脚本格式（如：json, yaml）
     * @return 如果支持返回true，否则返回false
     */
    fun supports(format: String): Boolean
}
