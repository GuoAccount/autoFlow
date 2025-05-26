package com.g2s.autoflow.core.task.impl

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import com.g2s.autoflow.core.accessibility.AutoFlowAccessibilityService
import com.g2s.autoflow.core.task.Task
import com.g2s.autoflow.utils.AppLogger
import kotlinx.coroutines.delay

/**
 * 滑动任务
 */
class SwipeTask(
    override val id: String,
    val startX: Int,
    val startY: Int,
    val endX: Int,
    val endY: Int,
    val duration: Long = 500,
    val waitAfter: Long = 500,
    override val description: String = "",
    override val priority: Int = 5,
    override val isBlocking: Boolean = true
) : BaseTask(id, description) {
    
    private val tag = "SwipeTask"
    
    override suspend fun execute() {
        AppLogger.d(tag, "Executing swipe task: $description")
        
        val service = AutoFlowAccessibilityService.getInstance() ?: 
            throw IllegalStateException("Accessibility service not available")
            
        val path = Path().apply {
            moveTo(startX.toFloat(), startY.toFloat())
            lineTo(endX.toFloat(), endY.toFloat())
        }
        
        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(
                    path,
                    0,
                    duration
                )
            )
            .build()
            
        val dispatched = service.dispatchGesture(gesture, null, null)
        
        if (!dispatched) {
            throw RuntimeException("Failed to dispatch swipe gesture")
        }
        
        delay(duration) // 等待手势完成
        
        if (waitAfter > 0) {
            delay(waitAfter)
        }
        
        AppLogger.d(tag, "Swipe task completed successfully")
    }
}
