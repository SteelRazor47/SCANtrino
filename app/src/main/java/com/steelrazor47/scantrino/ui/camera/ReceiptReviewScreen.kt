package com.steelrazor47.scantrino.ui.camera

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.steelrazor47.scantrino.model.DataMock
import com.steelrazor47.scantrino.ui.theme.ScantrinoTheme

@Composable
fun ReceiptReviewScreen(
    viewModel: ReceiptReviewViewModel = hiltViewModel(),
    onReceiptSaved: () -> Unit = {}
) {
    ReceiptReviewScreen(viewModel.items,
        onItemChanged = { old, new -> viewModel.changeItem(old, new) },
        onItemAdded = { viewModel.addItem() },
        onItemRemoved = { viewModel.removeItem(it) },
        onReceiptSaved = { viewModel.saveReviewReceipt(); onReceiptSaved() }
    )
}

@Composable
fun ReceiptReviewScreen(
    items: List<ReviewItemUiState>,
    onItemChanged: (old: ReviewItemUiState, new: ReviewItemUiState) -> Unit = { _, _ -> },
    onItemRemoved: (ReviewItemUiState) -> Unit = {},
    onItemAdded: () -> Unit = {},
    onReceiptSaved: () -> Unit = {}
) {

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(items = items) { item ->
                ReviewItem(
                    reviewItemUiState = item,
                    onItemChanged = { newItem -> onItemChanged(item, newItem) },
                    onDelete = { onItemRemoved(item) }
                )
            }
            item {
                TextButton(onClick = onItemAdded) {
                    Text("Add item")
                }
            }
        }

        if (items.all { it.confirmed && it.item.price != Int.MIN_VALUE })
            FloatingActionButton(
                onClick = onReceiptSaved,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Check, "")
            }
    }
}

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ReceiptReviewScreenPreview() {
    ScantrinoTheme {
        Surface {
            ReceiptReviewScreen(items = DataMock.items.map { ReviewItemUiState(it, listOf()) })
        }
    }

}
