package com.g2s.autoflow.core.task.impl

import com.g2s.autoflow.core.accessibility.AutoFlowAccessibilityService
import com.g2s.autoflow.core.ocr.OcrService
import com.g2s.autoflow.core.task.Task
import com.g2s.autoflow.utils.AppLogger
import kotlinx.coroutines.delay

/**
 * 点击任务
 */
class ClickTask(
    override val id: String,
    val targetId: String? = null,
    val text: String? = null,
    val contentDescription: String? = null,
    val useOcr: Boolean = false,
    val waitAfter: Long = 500,
    val timeout: Long = 10000,
    description: String = "",
    override val priority: Int = 5,
    override val isBlocking: Boolean = true
) : BaseTask(id, description) {
    
    private val tag = "ClickTask"
    
    override suspend fun execute() {
        AppLogger.d(tag, "Executing click task: $description")
        
        val startTime = System.currentTimeMillis()
        var success = false
        
        while (!success && (System.currentTimeMillis() - startTime) < timeout) {
            success = try {
                if (useOcr) {
                    clickWithOcr()
                } else {
                    clickWithAccessibility()
                }
            } catch (e: Exception) {
                AppLogger.e(tag, "Error during click execution", e)
                false
            }
            
            if (!success) {
                delay(500) // 等待500ms后重试
            }
        }
        
        if (success) {
            AppLogger.d(tag, "Click task completed successfully")
            if (waitAfter > 0) {
                delay(waitAfter)
            }
        } else {
            throw RuntimeException("Failed to perform click within timeout")
        }
    }
    
    private suspend fun clickWithAccessibility(): Boolean {
        val service = AutoFlowAccessibilityService.getInstance() ?: return false
        
        return when {
            !targetId.isNullOrEmpty() -> {
                service.findNodeById(targetId, null)?.let { node ->
                    service.performClick(node).also { AutoFlowAccessibilityService.safeRecycle(node) }
                } ?: false
            }
            !text.isNullOrEmpty() -> {
                service.findNodeByText(text, null)?.let { node ->
                    service.performClick(node).also { AutoFlowAccessibilityService.safeRecycle(node) }
                } ?: false
            }
            !contentDescription.isNullOrEmpty() -> {
                service.findNodeByContentDescription(contentDescription, null)?.let { node ->
                    service.performClick(node).also { AutoFlowAccessibilityService.safeRecycle(node) }
                } ?: false
            }
            else -> false
        }
    }
    
    private suspend fun clickWithOcr(): Boolean {
        // TODO: 实现OCR点击
        return false
    }
}
