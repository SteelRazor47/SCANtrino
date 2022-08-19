package com.steelrazor47.scantrino.ui.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.lifecycle.viewmodel.compose.viewModel
import com.steelrazor47.scantrino.TextAnalyzer


@Composable
fun CameraScreen(
    onReviewReceipt: () -> Unit = {},
    viewModel: ReceiptReviewViewModel = viewModel()
) {
    Box {
        Preview(TextAnalyzer { viewModel.setLines(it.textBlocks.flatMap { block -> block.lines }) })

        Canvas(modifier = Modifier.fillMaxSize()) {
            viewModel.boxes.forEach {
                this.drawRect(
                    color = Color.Red,
                    topLeft = it.topLeft,
                    size = it.size,
                    style = Stroke(4.0f)
                )
            }
        }
        FloatingActionButton(
            onClick = onReviewReceipt,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Filled.Check, "")
        }
    }
}
