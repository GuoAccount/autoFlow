package com.g2s.autoflow.core.script.exception

/**
 * 脚本解析异常
 */
class ScriptParseException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
