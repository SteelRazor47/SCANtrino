package com.steelrazor47.scantrino.ui.camera

import android.graphics.Point
import android.graphics.PointF
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Path
import androidx.core.graphics.toPointF
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.text.Text
import com.steelrazor47.scantrino.model.ItemName
import com.steelrazor47.scantrino.model.Receipt
import com.steelrazor47.scantrino.model.ReceiptItem
import com.steelrazor47.scantrino.model.service.StorageService
import com.steelrazor47.scantrino.utils.set
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ReceiptReviewViewModel @Inject constructor(private val storageService: StorageService) :
    ViewModel() {
    private var lines: List<Text.Line> = listOf()
    var boundingBoxes by mutableStateOf(listOf<BoundingBox>())
        private set
    private val _items = mutableStateListOf(ReviewItemUiState(ReceiptItem(), listOf()))
    val items: List<ReviewItemUiState> = _items

    fun setAnalyzedText(text: Text) {
        lines = text.textBlocks.flatMap { it.lines }.filter { it.boundingBox != null }
        boundingBoxes = lines.mapNotNull { line -> line.cornerPoints?.let { BoundingBox(it) } }
    }

    fun setReviewReceipt() {
        viewModelScope.launch {
            val average = lines.map { it.boundingBox!!.height() }.average()

            val newItems = lines.sortedBy { it.boundingBox!!.top }
                .groupBy { (it.boundingBox!!.centerY() / average).roundToInt() }
                .filter { (_, list) -> list.size >= 2 }
                .map { (_, list) ->
                    val sortedList = list.sortedBy { it.boundingBox!!.left }
                    val input = sortedList.first().text
                    val itemName =
                        storageService.getMostSimilarName(input) ?: ItemName(name = input)
                    val suggestedNames = storageService.getSimilarNames(input, 20)
                    val price = sortedList.last().text.replace("""[^\-\d]""".toRegex(), "")
                        .toIntOrNull() ?: 0
                    ReviewItemUiState(ReceiptItem(name = itemName, price = price), suggestedNames)
                }

            _items.clear()
            _items.addAll(newItems)
        }
    }

    fun saveReviewReceipt() {
        viewModelScope.launch {
            val savedItems = _items.map {
                if (it.item.name.id.isNotEmpty())
                    return@map it.item
                val id = storageService.addItemName(it.item.name)
                val newItemName = ItemName(id, it.item.name.name)
                it.item.copy(name = newItemName)
            }
            storageService.addReceipt(Receipt(items = savedItems))
        }
    }

    fun addItem(item: ReviewItemUiState = ReviewItemUiState(ReceiptItem(), listOf())) {
        viewModelScope.launch {
            _items.add(item)
        }
    }

    fun removeItem(item: ReviewItemUiState) {
        _items.remove(item)
    }

    fun changeItem(old: ReviewItemUiState, new: ReviewItemUiState) {
        viewModelScope.launch {
            _items[old] =
                if (old.item.name != new.item.name)
                    new.copy(
                        suggestedNames = storageService.getSimilarNames(
                            new.item.name.name,
                            20
                        )
                    )
                else
                    new
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

data class ReviewItemUiState(
    val item: ReceiptItem,
    val suggestedNames: List<ItemName>,
    val confirmed: Boolean = false
)
