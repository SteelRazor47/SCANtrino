package com.steelrazor47.scantrino.ui.camera

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toComposeRect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.text.Text
import com.steelrazor47.scantrino.model.Receipt
import com.steelrazor47.scantrino.model.ReceiptItem
import com.steelrazor47.scantrino.model.ReceiptItemName
import com.steelrazor47.scantrino.model.ReceiptsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ReceiptReviewViewModel @Inject constructor(private val receiptsRepo: ReceiptsRepo) :
    ViewModel() {
    private var lines: List<Text.Line> = listOf()
    var boundingBoxes by mutableStateOf(listOf<Rect>())
        private set
    var receiptReview: Receipt by mutableStateOf(Receipt())

    fun getSimilarItems(name: String, count: Int) = receiptsRepo.getSimilarItems(name, count)

    fun setAnalyzedText(text: Text) {
        lines = text.textBlocks.flatMap { it.lines }.filter { it.boundingBox != null }
        boundingBoxes = lines.map { it.boundingBox!!.toComposeRect() }
    }

    fun setReviewReceipt() {
        viewModelScope.launch {
            val average = lines.map { it.boundingBox!!.height() }.average()

            val items = lines.sortedBy { it.boundingBox!!.top }
                .groupBy { (it.boundingBox!!.centerY() / average).roundToInt() }
                .filter { (_, list) -> list.size >= 2 }
                .map { (_, list) ->
                    val sortedList = list.sortedBy { it.boundingBox!!.left }
                    val input = sortedList.first().text
                    val itemName =
                        receiptsRepo.getMostSimilarItem(input) ?: ReceiptItemName(name = input)
                    val price = sortedList.last().text.replace("""[^\-\d]""".toRegex(), "")
                        .toIntOrNull() ?: 0
                    ReceiptItem(name = itemName, price = price)
                }

            receiptReview = Receipt(items = items)
        }
    }

    fun saveReviewReceipt() {
        viewModelScope.launch {
            receiptsRepo.insertReceipt(receiptReview)
            receiptReview = Receipt()
        }
    }

    fun addItemName(itemName: ReceiptItemName, onAdded: (ReceiptItemName) -> Unit = {}) {
        viewModelScope.launch {
            val addedName = receiptsRepo.addItemName(itemName)
            onAdded(addedName)
        }
    }
}
