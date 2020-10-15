package me.nigelbaillie.dictionarian

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.nigelbaillie.dictionarian.ocr.*

class AnalyzeViewModel : ViewModel() {
    var context: Context? = null

    var result: OCRResult? by mutableStateOf(null)
        private set

    var query: TextFieldValue by mutableStateOf(TextFieldValue())

    var opacity: Float by mutableStateOf(0F)

    private var lastAnalyzedUri: Uri? = null

    fun analyze(image: Bitmap) = viewModelScope.launch {
        result = InProgress("Test delay lol")
        delay(1000)
        result = Analyzer().analyze(image)
    }

    fun analyze(uri: Uri) {
        result = InProgress("Downloading image")

        viewModelScope.launch(Dispatchers.IO) {
            val source = ImageDecoder.createSource(context!!.contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source)
            result = InProgress("Analyzing image")
            result = Analyzer().analyze(bitmap)

            lastAnalyzedUri = when (result) {
                is Success -> uri
                else -> null
            }
        }
    }

    fun analyzeExtraStream(intent: Intent) {
        val uri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
        if (uri == null) {
            result = Failure("Invalid image share")
            return
        }
        else if (uri == lastAnalyzedUri) {
            return
        }
        analyze(uri)
    }

    fun analyzeData(intent: Intent) {
        val uri = intent.data
        if (uri == null) {
            result = Failure("No data")
            return
        }
        else if (uri == lastAnalyzedUri) {
            return
        }
        analyze(uri)
    }
}