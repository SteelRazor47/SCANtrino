package com.steelrazor47.scantrino.model

import androidx.room.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Entity(tableName = "receipts")
data class ReceiptInfo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val store: String = "",
    val date: LocalDateTime = LocalDateTime.now()
)


@Entity(tableName = "receipt_items")
data class ReceiptItemInfo(
    @PrimaryKey(autoGenerate = true) val itemId: Int = 0,
    val name: String = "",
)

@DatabaseView("""SELECT * from receipt_items JOIN receipt_cross_ref as cr ON cr.receiptItemId = receipt_items.itemId""")
data class ReceiptItem(
//    @Embedded
//    val info: ReceiptItemInfo,
    val itemId: Int = 0,
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
            entity = ReceiptItemInfo::class,
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
