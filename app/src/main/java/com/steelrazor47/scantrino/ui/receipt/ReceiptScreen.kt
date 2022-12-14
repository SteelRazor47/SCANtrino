package com.steelrazor47.scantrino.ui.receipt

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.steelrazor47.scantrino.model.*
import com.steelrazor47.scantrino.ui.theme.ScantrinoTheme
import com.steelrazor47.scantrino.utils.currencyFormatter
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ReceiptScreen(
    viewModel: ReceiptViewModel = hiltViewModel(),
    onReceiptDeleted: () -> Unit = {},
    onReceiptItemClicked: (receiptId: Long, itemNameId: Long) -> Unit
) {
    val receipt = viewModel.receipt.collectAsState(null).value ?: return

    val averages by viewModel.itemPriceAverages.collectAsState(mapOf())
    val filter by viewModel.timeFilter.collectAsState()
    ReceiptScreen(
        receipt,
        averages,
        filter,
        onTimeFilterChanged = { viewModel.timeFilter.value = it },
        onReceiptDeleted = { viewModel.deleteReceipt(); onReceiptDeleted() },
        onReceiptItemClicked = { onReceiptItemClicked(receipt.id, it) }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReceiptScreen(
    receipt: Receipt,
    priceVariations: Map<Long, Double?>,
    timeFilter: TimeFilter,
    onTimeFilterChanged: (TimeFilter) -> Unit,
    onReceiptDeleted: () -> Unit,
    onReceiptItemClicked: (itemNameId: Long) -> Unit
) {

    Column(modifier = Modifier.padding(8.dp)) {
        val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column {
                Text(text = receipt.store, style = typography.h4)
                Text(text = receipt.date.format(dateFormatter), style = typography.subtitle2)
            }
            TextButton(onClick = onReceiptDeleted) {
                Text("Delete")
            }
        }
        Spacer(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .height(1.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colors.onBackground)
        )

        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            TimeFilter.values().forEach {
                FilterChip(selected = timeFilter == it, onClick = { onTimeFilterChanged(it) }) {
                    Text(it.displayName)
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(receipt.items) { item ->
                ItemCard(item, priceVariations[item.itemNameId], onReceiptItemClicked)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun ItemCard(
    item: ReceiptItem,
    priceVar: Double?,
    onReceiptItemClicked: (id: Long) -> Unit
) {
    Card(onClick = { onReceiptItemClicked(item.itemNameId) }, modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = item.name,
                style = typography.h6,
                modifier = Modifier.weight(3f), textAlign = TextAlign.Start
            )
            priceVar?.let {
                Text(
                    text = NumberFormat.getPercentInstance().format(it),
                    color = when {
                        it < 0.0 -> MaterialTheme.colors.secondary
                        it > 0.0 -> MaterialTheme.colors.error
                        else -> Color.Unspecified
                    },
                    style = typography.h6,
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center
                )
            }
            Text(
                text = currencyFormatter.format(item.price / 100.0f),
                style = typography.h6,
                modifier = Modifier.weight(1f), textAlign = TextAlign.End
            )
        }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun ReceiptScreenPreview() {
    ScantrinoTheme {
        Surface {
            ReceiptScreen(
                receipt = DataMock.receipt,
                priceVariations = DataMock.itemVariations,
                timeFilter = TimeFilter.OneWeek,
                onTimeFilterChanged = {},
                onReceiptDeleted = {},
                onReceiptItemClicked = {}
            )
        }
    }
}
