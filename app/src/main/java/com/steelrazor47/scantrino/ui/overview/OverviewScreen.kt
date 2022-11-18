package com.steelrazor47.scantrino.ui.overview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.steelrazor47.scantrino.model.Receipt
import com.steelrazor47.scantrino.model.ReceiptsDaoMock
import com.steelrazor47.scantrino.ui.theme.ScantrinoTheme
import com.steelrazor47.scantrino.utils.currencyFormatter
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

@Composable
fun OverviewScreen(
    viewModel: OverviewViewModel = hiltViewModel(),
    onReceiptClicked: (Long) -> Unit = {}
) {
    OverviewScreen(viewModel.uiState, { viewModel.changeMonth(it) }, onReceiptClicked)
}


@Composable
fun OverviewScreen(
    uiState: OverviewUiState,
    onMonthChanged: (YearMonth) -> Unit = {},
    onReceiptClicked: (Long) -> Unit = {}
) {
    Column(modifier = Modifier.padding(8.dp)) {
        MonthSelector(month = uiState.month, onMonthChanged = onMonthChanged)
        MonthlyTotal(total = uiState.monthlyTotal)
        Spacer(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .height(1.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colors.onBackground)
        )
        ReceiptCardsList(receipts = uiState.receipts, onReceiptClicked = onReceiptClicked)
    }
}

@Composable
fun MonthSelector(month: YearMonth, onMonthChanged: (YearMonth) -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("MMMM uuuu") }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = { onMonthChanged(month.minusMonths(1)) }) {
            Icon(Icons.Filled.ChevronLeft, "")
        }
        Text(
            text = month.format(formatter).replaceFirstChar { it.titlecase() },
            style = MaterialTheme.typography.h6
        )
        IconButton(onClick = { onMonthChanged(month.plusMonths(1)) }) {
            Icon(Icons.Filled.ChevronRight, "")
        }

    }
}

@Composable
fun MonthlyTotal(total: Int) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Text(text = "Total:", style = MaterialTheme.typography.h5)
        Text(text = currencyFormatter.format(total / 100.0f), style = MaterialTheme.typography.h5)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReceiptCardsList(receipts: List<Receipt>, onReceiptClicked: (Long) -> Unit) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(receipts) { receipt ->
            Card(
                onClick = { onReceiptClicked(receipt.id) },
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = receipt.store,
                            style = MaterialTheme.typography.h5
                        )
                        Text(
                            text = receipt.date.format(dateFormatter),
                            style = MaterialTheme.typography.caption
                        )
                    }
                    Text(
                        text = currencyFormatter.format(receipt.total / 100.0f),
                        style = MaterialTheme.typography.h5
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OverviewScreenPreview() {
    ScantrinoTheme {
        Surface {
            OverviewScreen(
                OverviewUiState(
                    YearMonth.of(2022, Month.APRIL),
                    ReceiptsDaoMock.receipts
                )
            )
        }
    }
}
