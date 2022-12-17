package com.steelrazor47.scantrino.model

import androidx.room.*
import java.time.LocalDateTime

@Entity(tableName = "receipts")
data class Details(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val store: String = "",
    val date: LocalDateTime = LocalDateTime.now()
)


@Entity(tableName = "item_names")
data class ItemName(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
)

@DatabaseView(
    """
    SELECT receiptId, itemNameId, name, price FROM item_prices 
    JOIN item_names AS names ON names.id = item_prices.itemNameId
"""
)
data class ReceiptItem(
    val receiptId: Long = 0,
    val itemNameId: Long = 0,
    val name: String = "",
    val price: Int = 0
) {
    @Ignore
    val itemName = ItemName(itemNameId, name)

    constructor(receiptId: Long = 0, name: ItemName = ItemName(), price: Int = 0) :
            this(receiptId, name.id, name.name, price)

    fun copy(name: ItemName) = copy(itemNameId = name.id, name = name.name)
}

data class Receipt(
    @Embedded val details: Details = Details(),
    @Relation(
        parentColumn = "id", entityColumn = "receiptId",
    )
    val items: List<ReceiptItem> = listOf()
) {
    @Ignore
    val id = details.id

    @Ignore
    val date = details.date

    @Ignore
    val store = details.store

    @Ignore
    val total: Int = items.sumOf { it.price }

    constructor(
        id: Long = 0,
        store: String = "",
        date: LocalDateTime = LocalDateTime.now(),
        items: List<ReceiptItem> = listOf()
    ) : this(
        Details(id, store, date), items
    )

    fun copy(
        id: Long = this.id,
        store: String = this.store,
        date: LocalDateTime = this.date,
    ) = Receipt(id, store, date, items)
}


@Entity(
    tableName = "item_prices", primaryKeys = ["receiptId", "itemNameId"],
    indices = [Index(value = ["receiptId"], unique = false),
        Index(value = ["itemNameId"], unique = false)],
    foreignKeys = [
        ForeignKey(
            entity = Details::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ItemName::class,
            parentColumns = ["id"],
            childColumns = ["itemNameId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ItemPrice(
    val receiptId: Long,
    val itemNameId: Long,
    val price: Int
)
