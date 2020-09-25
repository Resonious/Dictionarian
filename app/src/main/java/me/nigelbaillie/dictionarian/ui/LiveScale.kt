package me.nigelbaillie.dictionarian.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.MutableLiveData

class LiveScale(private val wrapped: ContentScale, val setScale: (Float) -> Unit) : ContentScale {
    private val scaleResult = mutableStateOf(1F)

    override fun scale(srcSize: Size, dstSize: Size): Float {
        val result = wrapped.scale(srcSize, dstSize)
        setScale(result)
        return result
    }
}