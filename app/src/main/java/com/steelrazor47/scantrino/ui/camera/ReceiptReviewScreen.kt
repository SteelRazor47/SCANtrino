package com.steelrazor47.scantrino.ui.camera

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.steelrazor47.scantrino.model.ReceiptItem
import com.steelrazor47.scantrino.model.ReceiptsDaoMock
import com.steelrazor47.scantrino.model.ReceiptsRepo

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
                    onNameChanged = {
                        items[items.indexOf(item)] = item.copy(itemId = it.itemId, name = it.name)
                    },
                    onPriceChanged = { items[items.indexOf(item)] = item.copy(price = it) },
                    onAddName = {
                        viewModel.addItemName(it) { itemName ->
                            items[items.indexOf(item)] =
                                item.copy(itemId = itemName.itemId, name = itemName.name)
                        }
                    },
                    onDelete = { items.remove(item) }
                )
            }
            item {
                TextField(
                    value = "Click to add Item",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .clickable { items.add(ReceiptItem()) }
                        .fillMaxWidth()
                )
            }
        }


        FloatingActionButton(
            onClick = {
                if (items.any { it.itemId == 0L || it.price == -1 }) return@FloatingActionButton

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

@Preview(showBackground = true)
@Composable
fun ReceiptReviewScreenPreview() {
    ReceiptReviewScreen(
        viewModel = ReceiptReviewViewModel(ReceiptsRepo(ReceiptsDaoMock())).apply {
            receiptReview = ReceiptsDaoMock.invalidReceipt
        }
    )
}
