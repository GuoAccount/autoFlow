package com.g2s.autoflow.core.ocr

import android.graphics.Bitmap
import android.graphics.Rect

/**
 * OCR 服务接口
 */
interface OcrService {
    /**
     * 初始化 OCR 引擎
     * @param language 语言代码，如 "en", "zh" 等
     */
    suspend fun initialize(language: String = "en")

    /**
     * 识别图片中的文本
     * @param bitmap 要识别的图片
     * @param rect 识别区域（可选）
     * @return 识别结果
     */
    suspend fun recognizeText(bitmap: Bitmap, rect: Rect? = null): OcrResult

    /**
     * 释放资源
     */
    fun release()
}

/**
 * OCR 识别结果
 */
data class OcrResult(
    val text: String,
    val blocks: List<OcrTextBlock> = emptyList(),
    val success: Boolean = true,
    val error: String? = null
)

/**
 * OCR 文本块
 */
data class OcrTextBlock(
    val text: String,
    val lines: List<OcrTextLine> = emptyList(),
    val boundingBox: Rect
)

/**
 * OCR 文本行
 */
data class OcrTextLine(
    val text: String,
    val elements: List<OcrTextElement> = emptyList(),
    val boundingBox: Rect
)

/**
 * OCR 文本元素
 */
data class OcrTextElement(
    val text: String,
    val boundingBox: Rect,
    val confidence: Float = 1.0f
)
