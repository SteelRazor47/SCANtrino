package com.steelrazor47.scantrino.ui.camera

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ReviewReceiptScreen(viewModel: ReceiptReviewViewModel = viewModel()) {
    LazyColumn {
        items(viewModel.text) { item ->
            Text(item)
        }
    }
}