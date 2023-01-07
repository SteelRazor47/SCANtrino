package com.steelrazor47.scantrino.ui.receipt

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.steelrazor47.scantrino.model.DataMock
import com.steelrazor47.scantrino.model.ItemName
import com.steelrazor47.scantrino.model.ReceiptItem
import com.steelrazor47.scantrino.ui.theme.ScantrinoTheme

@Composable
fun ModifyItemScreen(viewModel: ModifyItemScreenViewModel = hiltViewModel()) {
    val item by viewModel.item.collectAsState(null)
    if (item == null) return
    ModifyItemScreen(item = item!!)
}

@Composable
fun ModifyItemScreen(
    item: ReceiptItem,
    onNameChanged: (ItemName) -> Unit = {},
    onPriceChanged: (Int) -> Unit = {}
) {
    Scaffold(topBar = {
        TopAppBar(
            title = {},
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = ""
                    )
                }
            })
    }) {
        Column {
            Text(item.itemNameId.toString())
            Text(item.name)
            Text(item.price.toString())
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun ModifyItemScreenPreview() {
    ScantrinoTheme {
        Surface {
            ModifyItemScreen(item = DataMock.items[0])
        }
    }
}
