package me.nigelbaillie.dictionarian.ocr

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

class Analyzer {
    // TODO
    // Try to come up with an interface that supports the processImage API here
    // https://firebase.google.com/docs/ml/android/recognize-text#kotlin+ktx_11
    // Then implement a dummy that uses a coroutine to sleep and return some constant Result and
    // TextBlocks for testing...
    // NOTE that you can use Bitmap.asImageAsset(), and then the composable Image(imageAsset).
    // https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/package-summary
    // https://developer.android.com/jetpack/compose/tutorial

    suspend fun analyze(image: Bitmap): OCRResult = coroutineScope {
        delay(2000)

        val blocks = arrayOf(
                TextBlock(
                        bounds=Rect(Offset(10.0F, 10.0F), Offset(50.0F, 20.0F)),
                        text="hehe"
                ),
                TextBlock(
                        bounds=Rect(Offset(195.0F, 142.0F), Offset(878.0F, 242.0F)),
                        text="台風の前にやっておく..."
                ),
                TextBlock(
                        bounds=Rect(Offset(155.0F, 380.0F), Offset(294.0F, 455.0F)),
                        text="目次"
                ),
                TextBlock(
                        bounds=Rect(Offset(155.0F, 530.0F), Offset(791.0F, 592.0F)),
                        text="台風の接近が予想される場合"
                )
        )

        Success(image, blocks)
    }
}
