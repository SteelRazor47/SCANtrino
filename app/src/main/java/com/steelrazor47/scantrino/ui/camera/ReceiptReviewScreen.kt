package com.steelrazor47.scantrino.ui.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.steelrazor47.scantrino.model.ReceiptItem
import com.steelrazor47.scantrino.utils.set

@Composable
fun ReceiptReviewScreen(
    viewModel: ReceiptReviewViewModel = hiltViewModel(),
    onReceiptSaved: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val items = viewModel.receiptReview.items.toMutableStateList()
        LazyColumn {
            item { Text("${viewModel.receiptReview.id} - ${viewModel.receiptReview.date}") }
            items(items = items) { item ->
                ReviewItem(
                    item = item,
                    itemsList = viewModel.getSimilarItems(item.name, 10),
                    onNameChanged = { name -> items[item] = item.with(name) },
                    onPriceChanged = { items[item] = item.with(price = it) },
                    onAddName = {
                        viewModel.addItemName(it) { name -> items[item] = item.with(name) }
                    },
                    onDelete = { items.remove(item) }
                )
            }
            item {
                TextButton(onClick = { items.add(ReceiptItem()) }) {
                    Text("Add item")
                }
            }
        }

        if (items.all { it.itemId != 0L && it.price != -1 })
            FloatingActionButton(
                onClick = {
                    viewModel.receiptReview = viewModel.receiptReview.copy(items = items)
                    viewModel.saveReviewReceipt()
                    onReceiptSaved()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Check, "")
            }
    }
}
