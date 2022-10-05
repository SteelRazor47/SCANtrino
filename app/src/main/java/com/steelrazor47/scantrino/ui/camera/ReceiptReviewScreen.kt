package com.steelrazor47.scantrino.ui.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ReceiptReviewScreen(
    viewModel: ReceiptReviewViewModel = hiltViewModel(),
    onReceiptSaved: () -> Unit = {}
) {
    Box {
        val items = viewModel.receiptReview.items.toMutableStateList()
        LazyColumn {
            item { Text("${viewModel.receiptReview.id} - ${viewModel.receiptReview.date}") }
            items(items = items) { item ->
                ReviewItem(
                    item = item,
                    onNameChanged = { items[items.indexOf(item)] = item.copy(name = it) },
                    onPriceChanged = { items[items.indexOf(item)] = item.copy(price = it) },
                )
            }
        }


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
