package me.nigelbaillie.dictionarian.ocr

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

class Analyzer(val recognizer: TextRecognizer?) {
    suspend fun analyze(image: Bitmap): OCRResult = coroutineScope {
        if (recognizer == null)
            return@coroutineScope Failure("TextRecognizer not supplied");
        if (!recognizer.isOperational)
            return@coroutineScope Failure("TextRecognizer not operational");

        val imageCopy = image.copy(Bitmap.Config.ARGB_8888, false)
        val items = recognizer.detect(Frame.Builder().setBitmap(imageCopy).build())
        val blocks: MutableList<TextBlock> = mutableListOf()

        for (i in 0 until items.size()) {
            val item = items.valueAt(i)
            val text = item?.value ?: continue
            val bounds = item.boundingBox

            Log.d("NIGELMSG", "Found \"${text}\"")

            blocks.add(
                TextBlock(
                    bounds.left.toFloat(),
                    bounds.top.toFloat(),
                    bounds.right.toFloat(),
                    bounds.bottom.toFloat(),
                    text
                )
            )
        }

        Success(image, blocks.toTypedArray())
    }
}
