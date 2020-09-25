package me.nigelbaillie.dictionarian.ocr

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

data class TextBlock(var bounds: Rect, var text: String) {
    constructor(x1: Float, y1: Float, x2: Float, y2: Float, text: String) : this(
            Rect(Offset(x1, y1), Offset(x2, y2)), text
    ) {}

    val x get() = bounds.left
    val y get() = bounds.top
    val width get() = bounds.size.width
    val height get() = bounds.size.height

}