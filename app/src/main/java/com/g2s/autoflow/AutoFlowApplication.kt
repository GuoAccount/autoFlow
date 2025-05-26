package com.g2s.autoflow

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.g2s.autoflow.core.ocr.OcrService
import com.g2s.autoflow.core.ocr.MLKitOcrService
import com.g2s.autoflow.service.AutoFlowForegroundService
import com.g2s.autoflow.utils.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 应用入口类，负责全局初始化和资源管理
 * 1. 管理应用生命周期
 * 2. 初始化全局组件（如日志系统、OCR服务等）
 * 3. 提供全局访问应用上下文的静态方法
 * 4. 管理前台服务
 */

/**
 * 应用入口类，负责全局初始化和资源管理
 */
class AutoFlowApplication : Application() {

    companion object {
        // 日志标签
        private const val TAG = "AutoFlowApp"
        // 通知渠道ID，用于前台服务通知
        private const val NOTIFICATION_CHANNEL_ID = "auto_flow_service_channel"
        
        // 应用单例实例，使用@Volatile确保多线程可见性
        @Volatile
        private var instance: AutoFlowApplication? = null
        
        /**
         * 获取应用上下文
         * @return 应用上下文
         * @throws IllegalStateException 如果应用未初始化
         */
        fun getAppContext(): Context {
            return instance?.applicationContext ?: 
                throw IllegalStateException("Application instance is not initialized")
        }
    }
    
    // 应用级协程作用域，使用SupervisorJob允许子协程独立失败
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // 主线程Handler，用于在主线程执行任务
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // OCR服务，使用懒加载方式初始化
    val ocrService: OcrService by lazy { 
        // 使用ML Kit实现的OCR服务
        MLKitOcrService(applicationContext)
    }
    
    /**
     * 应用创建时调用，执行全局初始化
     */
    override fun onCreate() {
        super.onCreate()
        // 保存单例实例
        instance = this
        
        // 初始化日志系统
        AppLogger.initialize(this)
        
        // 创建通知渠道（Android 8.0+ 需要）
        createNotificationChannel()
        
        // 初始化OCR服务
        initializeOcrService()
        
        // 启动前台服务
        startForegroundService()
    }
    
    /**
     * 启动前台服务
     */
    private fun startForegroundService() {
        try {
            AutoFlowForegroundService.start(this)
            AppLogger.d(TAG, "前台服务已启动")
        } catch (e: Exception) {
            AppLogger.e(TAG, "启动前台服务失败", e)
        }
    }
    
    /**
     * 停止前台服务
     */
    private fun stopForegroundService() {
        try {
            AutoFlowForegroundService.stop(this)
            AppLogger.d(TAG, "前台服务已停止")
        } catch (e: Exception) {
            AppLogger.e(TAG, "停止前台服务失败", e)
        }
    }
    
    /**
     * 创建通知渠道（Android 8.0+ 需要）
     */
    private fun createNotificationChannel() {
        // 检查Android版本，8.0及以上需要创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "AutoFlow 服务"
            val descriptionText = "AutoFlow后台服务"
            // 设置通知重要性级别为低，不会发出声音但会在通知栏显示
            val importance = NotificationManager.IMPORTANCE_LOW
            
            // 创建通知渠道
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            // 注册通知渠道到系统
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
            AppLogger.d(TAG, "通知渠道已创建: $NOTIFICATION_CHANNEL_ID")
        }
    }
    
    /**
     * 初始化OCR服务
     * ML Kit会自动初始化，这里只记录日志
     */
    private fun initializeOcrService() {
        // ML Kit 不需要显式初始化，这里只记录日志
        AppLogger.d(TAG, "ML Kit OCR服务已准备就绪")
    }
    
    /**
     * 应用终止时调用，执行清理工作
     * 注意：在真实设备上可能不会调用此方法
     */
    override fun onTerminate() {
        // 停止前台服务
        stopForegroundService()
        
        // 清理资源
        // ML Kit 会自动管理资源，无需显式释放
        
        AppLogger.d(TAG, "应用正在终止...")
        super.onTerminate()
    }
}
