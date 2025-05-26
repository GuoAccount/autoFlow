package com.g2s.autoflow.core.script.parser

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.g2s.autoflow.core.script.exception.ScriptParseException
import com.g2s.autoflow.core.script.model.TaskScript

/**
 * JSON 脚本解析器实现
 */
class JsonScriptParser : ScriptParser {
    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    override fun parse(content: String): TaskScript {
        return try {
            objectMapper.readValue(content)
        } catch (e: Exception) {
            throw ScriptParseException("Failed to parse JSON script", e)
        }
    }

    override fun supports(format: String): Boolean {
        return format.equals("json", ignoreCase = true)
    }
}
