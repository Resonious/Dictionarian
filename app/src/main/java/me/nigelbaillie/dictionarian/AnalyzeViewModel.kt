package me.nigelbaillie.dictionarian

import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.graphics.decodeBitmap
import androidx.lifecycle.*
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.nigelbaillie.dictionarian.ocr.Analyzer
import me.nigelbaillie.dictionarian.ocr.Failure
import me.nigelbaillie.dictionarian.ocr.InProgress
import me.nigelbaillie.dictionarian.ocr.OCRResult

class AnalyzeViewModel : ViewModel() {
    var recognizer: TextRecognizer? = null

    var result: OCRResult? by mutableStateOf(null)
        private set

    fun analyze(image: Bitmap) = viewModelScope.launch {
        result = InProgress("Test delay lol")
        delay(1000)
        result = Analyzer(recognizer).analyze(image)
    }

    fun analyze(intent: Intent, contentResolver: ContentResolver) {
        val uri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
        if (uri == null) {
            result = Failure("Invalid image share")
            return
        }
        result = InProgress("Downloading image")

        viewModelScope.launch(Dispatchers.IO) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source)
            result = InProgress("Analyzing image")
            result = Analyzer(recognizer).analyze(bitmap)
        }
    }
}