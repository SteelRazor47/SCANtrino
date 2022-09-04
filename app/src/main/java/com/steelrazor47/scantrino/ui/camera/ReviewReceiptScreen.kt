package com.steelrazor47.scantrino.ui.camera

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ReviewReceiptScreen(viewModel: ReceiptReviewViewModel = hiltViewModel()) {
    LazyColumn {
        items(viewModel.previewReceipt.items) { item ->
            Text("${item.name}\t\t${item.price}")
        }
    }
    FloatingActionButton(onClick = { /*TODO*/ }) {
        Icon(Icons.Filled.Check, "")
    }
}