package com.steelrazor47.scantrino.model

import androidx.room.*
import java.time.LocalDateTime

@Entity(tableName = "receipts")
data class ReceiptInfo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val store: String = "",
    val date: LocalDateTime = LocalDateTime.now()
)


@Entity(tableName = "receipt_items")
data class ReceiptItemName(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val name: String = "",
)

@DatabaseView(
    """
    SELECT receiptId, itemId, name, price from receipt_cross_ref 
    JOIN receipt_items 
    ON receipt_items.itemId = receipt_cross_ref.receiptItemId
"""
)
data class ReceiptItem(
    val receiptId: Long = 0,
    val itemId: Long = 0,
    val name: String = "",
    val price: Int = 0
)


@Entity(
    tableName = "receipt_cross_ref", primaryKeys = ["receiptId", "receiptItemId"],
    indices = [Index(value = ["receiptId"], unique = false),
        Index(value = ["receiptItemId"], unique = false)],
    foreignKeys = [
        ForeignKey(
            entity = ReceiptInfo::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ReceiptItemName::class,
            parentColumns = ["itemId"],
            childColumns = ["receiptItemId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReceiptCrossRef(
    val receiptId: Long,
    val receiptItemId: Long,
    val price: Int
)
