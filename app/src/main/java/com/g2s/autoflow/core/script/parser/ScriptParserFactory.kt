package com.g2s.autoflow.core.script.parser

import com.g2s.autoflow.core.script.exception.ScriptParseException

/**
 * 脚本解析器工厂
 */
object ScriptParserFactory {
    private val parsers = listOf(
        JsonScriptParser(),
        YamlScriptParser()
    )
    
    /**
     * 根据文件扩展名获取对应的解析器
     * @param format 文件格式（如：json, yaml, yml）
     * @return 对应的解析器实例
     * @throws ScriptParseException 当没有找到对应的解析器时抛出
     */
    @Throws(ScriptParseException::class)
    fun getParser(format: String): ScriptParser {
        return parsers.find { it.supports(format) }
            ?: throw ScriptParseException("Unsupported script format: $format")
    }
    
    /**
     * 根据文件扩展名获取对应的解析器
     * @param fileName 文件名
     * @return 对应的解析器实例
     * @throws ScriptParseException 当没有找到对应的解析器时抛出
     */
    @Throws(ScriptParseException::class)
    fun getParserForFile(fileName: String): ScriptParser {
        val extension = fileName.substringAfterLast('.').lowercase()
        return getParser(extension)
    }
}
