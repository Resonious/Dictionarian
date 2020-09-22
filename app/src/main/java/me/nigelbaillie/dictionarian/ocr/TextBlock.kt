package me.nigelbaillie.dictionarian.ocr

import androidx.compose.ui.geometry.Rect

data class TextBlock(var bounds: Rect, var text: String) {
}