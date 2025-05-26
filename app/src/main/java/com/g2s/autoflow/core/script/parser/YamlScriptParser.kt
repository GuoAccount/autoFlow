package com.g2s.autoflow.core.script.parser

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.g2s.autoflow.core.script.exception.ScriptParseException
import com.g2s.autoflow.core.script.model.TaskScript

/**
 * YAML 脚本解析器实现
 */
class YamlScriptParser : ScriptParser {
    private val objectMapper = ObjectMapper(YAMLFactory())
        .registerModule(KotlinModule.Builder().build())

    override fun parse(content: String): TaskScript {
        return try {
            objectMapper.readValue(content)
        } catch (e: Exception) {
            throw ScriptParseException("Failed to parse YAML script", e)
        }
    }

    override fun supports(format: String): Boolean {
        return format.equals("yaml", ignoreCase = true) || 
               format.equals("yml", ignoreCase = true)
    }
}
