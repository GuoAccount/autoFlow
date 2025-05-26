package com.g2s.autoflow.core.model

import android.content.Context
import android.graphics.Bitmap

/**
 * 模型管理器接口
 */
interface ModelManager {
    /**
     * 初始化模型
     */
    suspend fun initialize(context: Context): Boolean

    /**
     * 执行推理
     * @param input 输入数据，可以是图片、文本等
     * @return 推理结果
     */
    suspend fun <T> predict(input: Any): ModelResult<T>

    /**
     * 释放模型资源
     */
    fun release()
}

/**
 * 模型推理结果
 */
data class ModelResult<T>(
    val isSuccess: Boolean,
    val data: T? = null,
    val error: String? = null
)

/**
 * 支持的模型类型
 */
enum class ModelType {
    // UI元素检测
    UI_ELEMENT_DETECTION,
    // 文本识别
    TEXT_RECOGNITION,
    // 图像分类
    IMAGE_CLASSIFICATION
}

/**
 * 模型配置
 */
data class ModelConfig(
    val type: ModelType,
    val modelPath: String,
    val inputShape: IntArray,
    val outputShape: IntArray,
    val isQuantized: Boolean = false,
    val labelPath: String? = null
)
