package com.steelrazor47.scantrino.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

data class ItemName(
    @DocumentId val id: String = "",
    val name: String = "",
)

data class ReceiptItem(
    val name: ItemName = ItemName(),
    val price: Int = 0
)

data class Receipt(
    @DocumentId val id: String = "",
    val store: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val items: List<ReceiptItem> = listOf()
) {
    constructor(
        id: String = "",
        store: String = "",
        date: LocalDateTime,
        items: List<ReceiptItem> = listOf()
    ) : this(
        id,
        store,
        Timestamp(Date.from(date.atZone(ZoneId.systemDefault()).toInstant())),
        items
    )

    @get:Exclude
    val total: Int
        get() = items.sumOf { it.price }

    @get:Exclude
    val date: LocalDateTime =
        timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
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
