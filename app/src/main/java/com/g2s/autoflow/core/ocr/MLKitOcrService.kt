package com.g2s.autoflow.core.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

/**
 * 使用 ML Kit 实现的 OCR 服务
 */
class MLKitOcrService(private val context: Context) : OcrService {

    private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognizeText(bitmap: Bitmap, rect: Rect?): OcrResult {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val textResult = textRecognizer.process(image).await()
            
            val textBlocks = mutableListOf<OcrTextBlock>()
            
            for (block in textResult.textBlocks) {
                val lines = block.lines.map { line ->
                    val elements = line.elements.map { element ->
                        OcrTextElement(
                            text = element.text,
                            boundingBox = element.boundingBox ?: Rect(),
                            confidence = 1.0f // ML Kit 不提供置信度分数
                        )
                    }
                    OcrTextLine(
                        text = line.text,
                        elements = elements,
                        boundingBox = line.boundingBox ?: Rect()
                    )
                }
                
                textBlocks.add(
                    OcrTextBlock(
                        text = block.text,
                        lines = lines,
                        boundingBox = block.boundingBox ?: Rect()
                    )
                )
            }
            
            OcrResult(
                text = textResult.text,
                blocks = textBlocks,
                success = true
            )
        } catch (e: Exception) {
            OcrResult(
                text = "",
                blocks = emptyList(),
                success = false,
                error = e.message ?: "Unknown error"
            )
        }
    }

    override fun release() {
        // ML Kit 会自动管理资源，无需显式释放
    }

    override suspend fun initialize(language: String) {
        // ML Kit 不需要显式初始化
    }
}
