package me.nigelbaillie.dictionarian

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.nigelbaillie.dictionarian.ocr.Analyzer
import me.nigelbaillie.dictionarian.ocr.OCRResult

class AnalyzeViewModel : ViewModel() {
    var result: OCRResult? by mutableStateOf(null)
        private set

    public fun analyze(image: Bitmap) {
        viewModelScope.launch {
            delay(1000)
            result = Analyzer().analyze(image)
        }
    }
}