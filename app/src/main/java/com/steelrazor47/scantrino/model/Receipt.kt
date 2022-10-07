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
        parentColumn = "id", entityColumn = "receiptId",
    )
    val items: List<ReceiptItem> = listOf()
)

@Dao
interface ReceiptsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setReceiptInfo(receiptInfo: ReceiptInfo): Long // Row Id

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun setReceiptItemInfos(receiptItems: List<ReceiptItemName>): List<Long>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun setReceiptMappings(receiptRefs: List<ReceiptCrossRef>)

    suspend fun insertReceipt(receipt: Receipt) {
        val receiptId = setReceiptInfo(
            ReceiptInfo(
                receipt.id,
                receipt.store,
                receipt.date
            )
        )
        val newItems = receipt.items.filter { it.itemId == 0L }
        val itemIds = setReceiptItemInfos(newItems.map {
            ReceiptItemName(
                name = it.name
            )
        })
        val mappings =
            itemIds.zip(newItems.map { it.price }).map { (id, price) ->
                ReceiptCrossRef(
                    receiptId,
                    id,
                    price
                )
            } + receipt.items.filter { it.itemId != 0L }
                .map { ReceiptCrossRef(receiptId, it.itemId, it.price) }
        setReceiptMappings(mappings)
    }

    @Transaction
    @Query("SELECT * FROM receipts")
    fun getReceipts(): Flow<List<Receipt>>

    @Query("SELECT * FROM receipt_items")
    fun getItemNames(): Flow<List<ReceiptItemName>>

    @Transaction
    @Query("SELECT * FROM receipts WHERE receipts.id = :id")
    fun getReceipt(id: Long): Flow<Receipt?>

}
