package me.nigelbaillie.dictionarian

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.nigelbaillie.dictionarian.ocr.Analyzer
import me.nigelbaillie.dictionarian.ocr.OCRResult

class AnalyzeViewModel(application: Application) : AndroidViewModel(application) {
    private val _result: MutableLiveData<OCRResult?> = MutableLiveData(null)
    val result: LiveData<OCRResult?> = _result

    public fun analyze(image: Bitmap) {
        viewModelScope.launch {
            delay(1000)
            _result.value = Analyzer().analyze(image)
        }
    }
}