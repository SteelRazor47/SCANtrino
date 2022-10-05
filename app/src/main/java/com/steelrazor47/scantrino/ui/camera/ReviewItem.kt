package com.steelrazor47.scantrino.ui.camera

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Euro
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.steelrazor47.scantrino.model.ReceiptItem
import com.steelrazor47.scantrino.ui.theme.ScantrinoTheme

@Composable
fun ReviewItem(
    item: ReceiptItem,
    onNameChanged: (String) -> Unit = {},
    onPriceChanged: (Int) -> Unit = {}
) {
    Row {
        var isPriceValid by remember { mutableStateOf(true) }
        var priceText by remember { mutableStateOf((item.price / 100.0f).toString()) }
        TextField(value = item.name, onValueChange = onNameChanged, singleLine = true)
        TextField(
            value = priceText, onValueChange = { input ->
                try {
                    // Converts string of currency into cents
                    val price = input.toFloat().times(100).toInt()
                    isPriceValid = true
                    onPriceChanged(price)
                } catch (e: Exception) {
                    isPriceValid = false
                } finally {
                    priceText = input
                }
            },
            isError = !isPriceValid,
            singleLine = true,
            trailingIcon = { Icon(Icons.Filled.Euro, "") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewItemPreview() {
    ScantrinoTheme {
        ReviewItem(item = ReceiptItem(name = "test", price = 170))
    }
}
