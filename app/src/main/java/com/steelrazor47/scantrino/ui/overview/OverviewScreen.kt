package com.steelrazor47.scantrino.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OverviewScreen(viewModel: OverviewViewModel = hiltViewModel()) {
    val receipts by viewModel.receiptsDao.getReceipts().collectAsState(listOf())
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(receipts) { receipt ->
            Text("${receipt.id} - ${receipt.date}")
            LazyColumn(modifier = Modifier.height(128.dp)) {
                items(receipt.items) { item ->
                    Text("--- ${item.name}\t\t${item.price}")
                }
            }
        }
    }
}
