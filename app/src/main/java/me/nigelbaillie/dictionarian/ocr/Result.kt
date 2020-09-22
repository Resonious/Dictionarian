package me.nigelbaillie.dictionarian.ocr

import android.graphics.Bitmap

sealed class Result
class Success(var image: Bitmap, var blocks: Array<TextBlock>) : Result()
class Failure(var reason: String) : Result()