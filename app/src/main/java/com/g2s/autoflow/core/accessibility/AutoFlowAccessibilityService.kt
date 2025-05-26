package com.g2s.autoflow.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import com.g2s.autoflow.utils.AppLogger

/**
 * 自动化流程无障碍服务
 * 负责监听界面变化并执行自动化操作
 */
class AutoFlowAccessibilityService : AccessibilityService() {

    private val tag = "AutoFlowService"
    private var isServiceConnected = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceConnected = true
        AppLogger.d(tag, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    val packageName = event.packageName?.toString()
                    val className = event.className?.toString()
                    AppLogger.d(tag, "Window changed: $packageName/$className")
                    // 处理窗口变化事件
                    processWindowChanged(packageName, className)
                }
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    // 处理点击事件
                    event.source?.let { source ->
                        try {
                            val text = source.text?.toString()
                            val contentDescription = source.contentDescription?.toString()
                            AppLogger.d(tag, "View clicked - Text: $text, Desc: $contentDescription")
                            processViewClicked(text, contentDescription)
                        } finally {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                source.recycle()
                            }
                        }
                    }
                }
                // 添加其他事件处理...
            }
        } catch (e: Exception) {
            AppLogger.e(tag, "Error in onAccessibilityEvent", e)
        }
    }
    
    /**
     * 处理窗口变化事件
     * @param packageName 包名
     * @param className 类名
     */
    private fun processWindowChanged(packageName: String?, className: String?) {
        // 在这里添加窗口变化处理逻辑
        packageName?.let { pkg ->
            className?.let { cls ->
                AppLogger.d(tag, "Processing window changed: $pkg/$cls")
                // 在这里添加窗口变化处理逻辑
            }
        }
    }
    
    /**
     * 处理视图点击事件
     * @param text 视图文本
     * @param contentDescription 视图内容描述
     */
    private fun processViewClicked(text: String?, contentDescription: String?) {
        // 在这里添加视图点击处理逻辑
        text?.let { txt ->
            AppLogger.d(tag, "View clicked with text: $txt")
        }
        contentDescription?.let { desc ->
            AppLogger.d(tag, "View clicked with description: $desc")
        }
    }

    /**
     * 点击屏幕指定位置
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun click(x: Int, y: Int): Boolean {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(
            GestureDescription.StrokeDescription(
                path, 
                0, 
                100 // 点击持续100ms
            )
        )
        
        return dispatchGesture(gestureBuilder.build(), null, null)
    }

    /**
     * 查找包含指定文本的节点
     * @param text 要查找的文本
     * @param rootNode 从哪个节点开始查找，如果为null则从根节点开始
     * @return 找到的节点，如果没有找到则返回null
     */
    /**
     * 通过文本查找节点
     * @param text 要查找的文本
     * @param rootNode 根节点，如果为null则使用当前活动窗口的根节点
     * @return 找到的节点，如果没有找到则返回null
     */
    fun findNodeByText(text: String, rootNode: AccessibilityNodeInfo? = null): AccessibilityNodeInfo? {
        val startNode = rootNode ?: rootInActiveWindow ?: return null
        return try {
            val nodes = startNode.findAccessibilityNodeInfosByText(text)
            if (nodes.isNullOrEmpty()) null else nodes[0]
        } finally {
            if (rootNode == null) {
                // 在Android 5.0 (API 21) 及以上版本中，不再需要手动回收
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    @Suppress("DEPRECATION")
                    startNode.recycle()
                }
            }
        }
    }
    
    /**
     * 通过ID查找节点
     * @param viewId 视图ID，格式为 "包名:id/viewId"
     * @param rootNode 根节点，如果为null则使用当前活动窗口的根节点
     * @return 找到的节点，如果没有找到则返回null
     */
    fun findNodeById(viewId: String, rootNode: AccessibilityNodeInfo? = null): AccessibilityNodeInfo? {
        val startNode = rootNode ?: rootInActiveWindow ?: return null
        return try {
            val nodes = startNode.findAccessibilityNodeInfosByViewId(viewId)
            if (nodes.isNullOrEmpty()) null else nodes[0]
        } finally {
            if (rootNode == null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    @Suppress("DEPRECATION")
                    startNode.recycle()
                }
            }
        }
    }
    
    /**
     * 通过内容描述查找节点
     * @param contentDescription 内容描述
     * @param rootNode 根节点，如果为null则使用当前活动窗口的根节点
     * @return 找到的节点，如果没有找到则返回null
     */
    fun findNodeByContentDescription(contentDescription: String, rootNode: AccessibilityNodeInfo? = null): AccessibilityNodeInfo? {
        val startNode = rootNode ?: rootInActiveWindow ?: return null
        
        // 递归查找匹配内容描述的节点
        fun findNodeRecursive(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
            // 检查当前节点是否匹配
            if (node.contentDescription?.toString()?.contains(contentDescription, ignoreCase = true) == true) {
                return node
            }
            
            // 递归检查子节点
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                val found = findNodeRecursive(child)
                if (found != null) {
                    return found
                }
            }
            
            return null
        }
        
        return try {
            findNodeRecursive(startNode)
        } finally {
            if (rootNode == null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    @Suppress("DEPRECATION")
                    startNode.recycle()
                }
            }
        }
    }

    /**
     * 执行点击操作
     */
    fun performClick(node: AccessibilityNodeInfo): Boolean {
        return if (node.isClickable) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            true
        } else {
            node.parent?.let { parent ->
                performClick(parent)
            } ?: false
        }
    }

    override fun onInterrupt() {
        AppLogger.d(tag, "Accessibility service interrupted")
        isServiceConnected = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceConnected = false
        AppLogger.d(tag, "Accessibility service destroyed")
    }

    companion object {
        private var instance: AutoFlowAccessibilityService? = null
        
        fun getInstance(): AutoFlowAccessibilityService? = instance
        
        /**
         * 安全地回收节点
         * @param node 要回收的节点
         */
        fun safeRecycle(node: AccessibilityNodeInfo?) {
            if (node != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                @Suppress("DEPRECATION")
                node.recycle()
            }
        }
    }
    
    init {
        instance = this
    }
}
