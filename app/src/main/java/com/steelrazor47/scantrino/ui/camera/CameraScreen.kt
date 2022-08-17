package com.steelrazor47.scantrino.ui.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toComposeRect
import com.steelrazor47.scantrino.TextAnalyzer


@Composable
fun CameraScreen() {
    val rectangles = remember { mutableStateListOf<Rect>() }

    Preview(TextAnalyzer {
        rectangles.clear()
        rectangles.addAll(it.textBlocks.flatMap { block -> block.lines }
            .map { line -> line.boundingBox?.toComposeRect() ?: Rect.Zero })
    })
    Canvas(modifier = Modifier.fillMaxSize()) {
        rectangles.forEach {
            this.drawRect(
                color = Color.Red,
                topLeft = it.topLeft,
                size = it.size,
                style = Stroke(4.0f)
            )
        }
    }
}
