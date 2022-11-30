package com.steelrazor47.scantrino.ui.camera

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Euro
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.steelrazor47.scantrino.model.DataMock
import com.steelrazor47.scantrino.model.ReceiptItem
import com.steelrazor47.scantrino.model.ReceiptItemName
import com.steelrazor47.scantrino.ui.theme.ScantrinoTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReviewItem(
    item: ReceiptItem,
    itemsList: Flow<List<ReceiptItemName>>,
    onNameChanged: (ReceiptItemName) -> Unit = {},
    onPriceChanged: (Int) -> Unit = {},
    onAddName: (ReceiptItemName) -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Row {
        var priceText by remember { mutableStateOf((item.price / 100.0f).toString()) }
        var expanded by remember { mutableStateOf(false) }
        val list by itemsList.collectAsState(listOf())

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1.5f)
        ) {
            TextField(
                value = item.name,
                onValueChange = { onNameChanged(ReceiptItemName(itemId = 0, name = it)) },
                isError = item.itemId == 0L,
                singleLine = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                list.forEach {
                    DropdownMenuItem(onClick = { onNameChanged(it); expanded = false }) {
                        Text("${it.itemId}: ${it.name}")
                    }
                }
                if (item.itemId == 0L)
                    DropdownMenuItem(onClick = {
                        onAddName(ReceiptItemName(name = item.name))
                        expanded = false
                    }) {
                        Text("Add: ${item.name}")
                        Icon(Icons.Filled.Add, contentDescription = "")
                    }
            }
        }
        TextField(
            value = priceText,
            onValueChange = { input ->
                try {
                    // Converts string of currency into cents
                    val price = input.toFloat().times(100).toInt()
                    onPriceChanged(price)
                } catch (e: Exception) {
                    onPriceChanged(-1)
                } finally {
                    priceText = input
                }
            },
            isError = item.price == -1,
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Euro, "") },
            trailingIcon = {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .weight(1.0f)
                .wrapContentWidth(Alignment.End)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReviewItemPreview() {
    ScantrinoTheme {
        ReviewItem(item = DataMock.items[0], itemsList = flowOf(DataMock.itemNames))
    }
}
