package com.steelrazor47.scantrino.ui.camera

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toComposeRect
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.text.Text
import com.steelrazor47.scantrino.model.ReceiptsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ReceiptReviewViewModel @Inject constructor(private val receiptsRepo: ReceiptsRepository) : ViewModel() {
    var previewReceipt by mutableStateOf(Receipt())
        private set

    private val _boundingBoxes = mutableStateListOf<Rect>()
    val boundingBoxes: List<Rect> = _boundingBoxes

    fun setAnalyzedText(text: Text) {
        runBlocking { receiptsRepo.getReceipts() }
        val lines = text.textBlocks.flatMap { it.lines }.filter { it.boundingBox != null }

        _boundingBoxes.clear()
        _boundingBoxes.addAll(lines.map { it.boundingBox!!.toComposeRect() })

        val average = lines.map { it.boundingBox!!.height() }.average()
        val items = lines.sortedBy { it.boundingBox!!.top }
            .groupBy { (it.boundingBox!!.centerY() / average).roundToInt() }
            .filter { (_, list) -> list.size >= 2 }
            .map { (_, list) ->
                val sortedList = list.sortedBy { it.boundingBox!!.left }
                ReceiptItem(sortedList.first().text, sortedList.last().text)
            }

        previewReceipt = Receipt(items)
    }
}

data class Receipt(val items: List<ReceiptItem> = listOf())

data class ReceiptItem(val name: String, val price: String)