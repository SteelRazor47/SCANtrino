package com.steelrazor47.scantrino.ui.camera

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Euro
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.steelrazor47.scantrino.model.DataMock
import com.steelrazor47.scantrino.ui.theme.ScantrinoTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReviewItem(
    reviewItemUiState: ReviewItemUiState,
    onItemChanged: (new: ReviewItemUiState) -> Unit = { },
    onDelete: () -> Unit = {}
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        var expanded by remember { mutableStateOf(false) }
        val item = reviewItemUiState.item
        val suggestedItemsList = reviewItemUiState.suggestedNames
        val isNameConfirmed = reviewItemUiState.confirmed

        Checkbox(
            checked = isNameConfirmed,
            onCheckedChange = { onItemChanged(reviewItemUiState.copy(confirmed = it)) }
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1.5f)
        ) {
            TextField(
                value = item.name,
                onValueChange = {
                    val new = reviewItemUiState.copy(
                        item = item.copy(name = it),
                        confirmed = false
                    )
                    onItemChanged(new)
                },
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            if (suggestedItemsList.isNotEmpty())
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    suggestedItemsList.forEach {
                        DropdownMenuItem(onClick = {
                            val new = reviewItemUiState.copy(
                                item = item.copy(name = it),
                                confirmed = true
                            )
                            onItemChanged(new)
                            expanded = false
                        }) {
                            Text("${it.id}: ${it.name}")
                        }
                    }
                }
        }

        /* The price field should manage its own text, while sending either the correct value
         or an error value upstream. Since item.price cannot represent invalid values it
         cannot be used as the TextField state, which must be stored locally */
        var priceText by remember { mutableStateOf((item.price / 100.0f).toString()) }
        TextField(
            value = priceText,
            onValueChange = { input ->
                val price = input.toFloatOrNull()?.times(100)?.toInt() ?: Int.MIN_VALUE
                val new = reviewItemUiState.copy(item = item.copy(price = price))
                priceText = input
                onItemChanged(new)
            },
            isError = item.price == Int.MIN_VALUE,
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

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun ReviewItemPreview() {
    ScantrinoTheme {
        Surface {
            ReviewItem(ReviewItemUiState(DataMock.items[0], listOf()))
        }
    }
}
