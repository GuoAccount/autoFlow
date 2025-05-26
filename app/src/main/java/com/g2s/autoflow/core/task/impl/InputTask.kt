package com.g2s.autoflow.core.task.impl

import android.view.accessibility.AccessibilityNodeInfo
import com.g2s.autoflow.core.accessibility.AutoFlowAccessibilityService
import com.g2s.autoflow.core.task.Task
import com.g2s.autoflow.utils.AppLogger
import kotlinx.coroutines.delay

/**
 * 输入任务
 */
class InputTask(
    override val id: String,
    val targetId: String? = null,
    val text: String? = null,
    val input: String,
    val append: Boolean = false,
    val waitAfter: Long = 500,
    override val description: String = "",
    override val priority: Int = 5,
    override val isBlocking: Boolean = true
) : BaseTask(id, description) {
    
    private val tag = "InputTask"
    
    override suspend fun execute() {
        AppLogger.d(tag, "Executing input task: $description")
        
        val service = AutoFlowAccessibilityService.getInstance() ?: 
            throw IllegalStateException("Accessibility service not available")
            
        val node = findTargetNode(service) ?: 
            throw IllegalStateException("Target node not found")
            
        if (!node.isEditable) {
            throw IllegalStateException("Target node is not editable")
        }
        
        val textToInput = if (append) {
            (node.text?.toString() ?: "") + input
        } else {
            input
        }
        
        val args = android.os.Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                textToInput
            )
        }
        
        val success = node.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            args
        )
        
        if (!success) {
            throw RuntimeException("Failed to set text")
        }
        
        if (waitAfter > 0) {
            delay(waitAfter)
        }
        
        AppLogger.d(tag, "Input task completed successfully")
    }
    
    private fun findTargetNode(service: AutoFlowAccessibilityService): AccessibilityNodeInfo? {
        return when {
            !targetId.isNullOrEmpty() -> service.findNodeById(targetId)
            !text.isNullOrEmpty() -> service.findNodeByText(text)
            else -> null
        }
    }
}
