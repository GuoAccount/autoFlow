package com.g2s.autoflow.core.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * 使用 ML Kit 实现的 OCR 服务
 */
class TesseractOcrService(private val context: Context) : OcrService {
    private val tag = "MlKitOcr"
    private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    companion object {
        private const val TAG = "MlKitOcr"
    }
    
    override suspend fun initialize(language: String) = withContext(Dispatchers.IO) {
        // ML Kit 不需要显式初始化
    }
    
    override suspend fun recognizeText(bitmap: Bitmap, rect: Rect?): OcrResult = withContext(Dispatchers.IO) {
        try {
            // 创建输入图像
            val image = InputImage.fromBitmap(bitmap, 0)
            
            // 识别文本
            val textResult = textRecognizer.process(image).await()
            
            // 提取文本块和行信息
            val blocks = mutableListOf<OcrTextBlock>()
            
            for (block in textResult.textBlocks) {
                val blockRect = block.boundingBox ?: continue
                val lines = mutableListOf<OcrTextLine>()
                
                for (line in block.lines) {
                    val lineRect = line.boundingBox ?: continue
                    val elements = mutableListOf<OcrTextElement>()
                    
                    for (element in line.elements) {
                        val elementRect = element.boundingBox ?: continue
                        elements.add(
                            OcrTextElement(
                                text = element.text,
                                boundingBox = elementRect
                            )
                        )
                    }
                    
                    lines.add(
                        OcrTextLine(
                            text = line.text,
                            elements = elements,
                            boundingBox = lineRect
                        )
                    )
                }
                
                blocks.add(
                    OcrTextBlock(
                        text = block.text,
                        lines = lines,
                        boundingBox = blockRect
                    )
                )
            }
            
            // 返回识别结果
            OcrResult(
                text = textResult.text,
                blocks = blocks,
                success = true
            )
        } catch (e: Exception) {
            OcrResult(
                text = "",
                blocks = emptyList(),
                success = false,
                error = "Error recognizing text: ${e.message}"
            )
        }
    }
    
    override fun release() {
        // ML Kit 会自动管理资源，此方法保留接口兼容性
    }
}
