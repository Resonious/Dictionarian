package me.nigelbaillie.dictionarian.ocr

import android.graphics.Bitmap

sealed class OCRResult
class Success(var image: Bitmap, var blocks: Array<TextBlock>) : OCRResult()
class InProgress(var message: String) : OCRResult()
class Failure(var reason: String) : OCRResult()