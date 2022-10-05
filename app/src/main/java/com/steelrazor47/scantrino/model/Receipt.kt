package com.steelrazor47.scantrino.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

data class Receipt(
//    @Embedded
//    val info: ReceiptInfo,
    val id: Long = 0,
    val store: String = "",
    val date: LocalDateTime = LocalDateTime.now(),
    @Relation(
        parentColumn = "id", entityColumn = "itemId",
        associateBy = Junction(
            value = ReceiptCrossRef::class,
            parentColumn = "receiptId",
            entityColumn = "receiptItemId"
        )
    )
    val items: List<ReceiptItem> = listOf()
)

@Dao
interface ReceiptsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setReceiptInfo(receiptInfo: ReceiptInfo): Long // Row Id

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setReceiptItemInfos(receiptItems: List<ReceiptItemInfo>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setReceiptMappings(receiptRefs: List<ReceiptCrossRef>)

    suspend fun insertReceipt(receipt: Receipt) {
        val receiptId = setReceiptInfo(
            ReceiptInfo(
                receipt.id,
                receipt.store,
                receipt.date
            )
        )
        val itemIds = setReceiptItemInfos(receipt.items.map {
            ReceiptItemInfo(
                it.itemId,
                it.name
            )
        })
        val mappings = itemIds.zip(receipt.items.map { it.price })
            .map { (id, price) -> ReceiptCrossRef(receiptId, id, price) }
        setReceiptMappings(mappings)
    }

    @Transaction
    @Query("SELECT * FROM receipts")
    fun getReceipts(): Flow<List<Receipt>>

    @Transaction
    @Query("SELECT * FROM receipts WHERE receipts.id = :id")
    fun getReceipt(id: Long): Flow<Receipt?>

}
