package me.nigelbaillie.dictionarian.ocr

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.geometry.Rect
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Analyzer() {
    private val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
        .setLanguageHints(listOf("ja", "en"))
        .build()
    private val detector = FirebaseVision.getInstance().getCloudTextRecognizer(options)

    suspend fun analyze(image: Bitmap): OCRResult = suspendCoroutine {
        val imageCopy = image.copy(image.config, false)
        val inputImage = FirebaseVisionImage.fromBitmap(imageCopy)

        detector.processImage(inputImage)
            .addOnSuccessListener { result ->
                val blocks: MutableList<TextBlock> = mutableListOf()

                for (block in result.textBlocks) {
                    for (line in block.lines) {
                        val bounds = line.boundingBox ?: continue
                        Log.d("NIGELMSG", "Found \"${line.text}\" at ${bounds.toString()}")

                        for (element in line.elements) {
                            val bounds = element.boundingBox ?: continue
                            val padding = 1.2F

                            val rect = Rect(
                                bounds.left.toFloat(),
                                bounds.top.toFloat(),
                                bounds.right.toFloat(),
                                bounds.bottom.toFloat(),
                            )

                            blocks.add(
                                TextBlock(rect.inflate(padding), element.text)
                            )
                        }
                    }
                }

                it.resume(Success(image, blocks.toTypedArray()))
            }
            .addOnFailureListener { error ->
                it.resume(
                    Failure(
                        if (error.localizedMessage != null) error.localizedMessage
                        else "Unknown error"
                    )
                )
            }
    }
}
