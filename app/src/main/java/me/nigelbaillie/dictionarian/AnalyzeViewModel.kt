package me.nigelbaillie.dictionarian

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import me.nigelbaillie.dictionarian.ocr.Analyzer
import me.nigelbaillie.dictionarian.ocr.OCRResult

class AnalyzeViewModel(application: Application) : AndroidViewModel(application) {
    val result: MutableLiveData<OCRResult?> = MutableLiveData()

    public fun analyze(image: Bitmap) {
        viewModelScope.launch {
            result.value = Analyzer().analyze(image)
        }
    }
}