package com.steelrazor47.scantrino.ui.camera

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toComposeRect
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.text.Text
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReceiptReviewViewModel @Inject constructor() : ViewModel() {
    private val lines = mutableStateListOf<Text.Line>()
    val text: List<String>
        get() = lines.map { it.text }
    val boxes: List<Rect>
        get() = lines.map { it.boundingBox?.toComposeRect() ?: Rect.Zero }

    fun setLines(lines: List<Text.Line>) {
        this.lines.clear()
        this.lines.addAll(lines)
    }
}
