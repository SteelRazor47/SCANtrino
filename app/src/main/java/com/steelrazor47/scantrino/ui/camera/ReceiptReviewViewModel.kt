package com.steelrazor47.scantrino.ui.camera

import android.graphics.Point
import android.graphics.PointF
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Path
import androidx.core.graphics.toPointF
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
    var boundingBoxes by mutableStateOf(listOf<BoundingBox>())
        private set
    var receiptReview: Receipt by mutableStateOf(Receipt())

    fun getSimilarItems(name: String, count: Int) = receiptsRepo.getSimilarItems(name, count)

    fun setAnalyzedText(text: Text) {
        lines = text.textBlocks.flatMap { it.lines }.filter { it.boundingBox != null }
        boundingBoxes = lines.mapNotNull { line -> line.cornerPoints?.let { BoundingBox(it) } }
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

data class BoundingBox(
    private val points: List<PointF>,
    val rotation: Float
) {
    constructor(points: Array<Point>, rotation: Float = 0.0f) : this(
        points.map { it.toPointF() },
        rotation
    )

    private val topLeft = points[0]
    private val topRight = points[1]
    private val bottomRight = points[2]
    private val bottomLeft = points[3]

    val path = Path().apply {
        moveTo(topLeft.x, topLeft.y)
        lineTo(topRight.x, topRight.y)
        moveTo(topRight.x, topRight.y)
        lineTo(bottomRight.x, bottomRight.y)
        moveTo(bottomRight.x, bottomRight.y)
        lineTo(bottomLeft.x, bottomLeft.y)
        moveTo(bottomLeft.x, bottomLeft.y)
        lineTo(topLeft.x, topLeft.y)
    }
}
