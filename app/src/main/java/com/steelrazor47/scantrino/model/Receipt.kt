package com.steelrazor47.scantrino.model

import java.time.LocalDateTime

data class ItemName(
    val id: String = "",
    val name: String = "",
)

data class ReceiptItem(
    val name: ItemName = ItemName(),
    val price: Int = 0
)

data class Receipt(
    val id: String = "",
    val store: String = "",
    val date: LocalDateTime = LocalDateTime.now(),
    val items: List<ReceiptItem> = listOf()
) {
    val total: Int = items.sumOf { it.price }
}

class DataMock {

    companion object {
        val itemNames = listOf(
            ItemName("a", "Apple"),
            ItemName("b", "Banana"),
            ItemName("c", "Orange"),
            ItemName("d", "Pear")
        )
        val items = itemNames.zip(listOf(23, 532, 57, 23))
            .map { (name, price) -> ReceiptItem(name, price) }
        val itemAverages = mapOf("a" to 20.0, "b" to 550.0, "c" to 90.0, "d" to 10.0)
        val receipt = Receipt(
            "1", "Conad", LocalDateTime.parse("2022-10-15T12:00:41"), items
        )
        val receipts =
            listOf(
                receipt,
                receipt.copy(id = "2", store = "Coop"),
                receipt.copy(id = "3", store = "Penny")
            )
        val invalidReceipt =
            receipt.copy(items = items + listOf(ReceiptItem(ItemName("e", "Strawberry"), -1)))
    }
}
