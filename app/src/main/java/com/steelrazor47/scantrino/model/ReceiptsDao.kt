package com.steelrazor47.scantrino.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime

@Dao
interface ReceiptsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setReceiptInfo(receiptInfo: ReceiptInfo): Long // Row Id

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun setReceiptItemNames(receiptItems: List<ReceiptItemName>): List<Long>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun setReceiptItemName(name: ReceiptItemName): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun setReceiptMappings(receiptRefs: List<ReceiptCrossRef>)

    @Transaction
    suspend fun insertReceipt(receipt: Receipt) {
        val receiptId = setReceiptInfo(
            ReceiptInfo(
                receipt.id,
                receipt.store,
                receipt.date
            )
        )
        val newItems = receipt.items.filter { it.itemId == 0L }
        val itemIds = setReceiptItemNames(newItems.map {
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
    @Query("SELECT * FROM receipts WHERE receipts.id = :id")
    fun getReceipt(id: Long): Flow<Receipt?>

    @Transaction
    @Query("SELECT * FROM receipts")
    fun getReceipts(): Flow<List<Receipt>>

    @Query("SELECT * FROM receipt_item_names")
    fun getItemNamesFlow(): Flow<List<ReceiptItemName>>

    @Query("SELECT * FROM receipt_item_names")
    suspend fun getItemNames(): List<ReceiptItemName>
}

class ReceiptsDaoMock : ReceiptsDao {
    override suspend fun setReceiptInfo(receiptInfo: ReceiptInfo): Long = TODO()
    override suspend fun setReceiptItemNames(receiptItems: List<ReceiptItemName>) = TODO()
    override suspend fun setReceiptItemName(name: ReceiptItemName): Long = TODO()
    override suspend fun setReceiptMappings(receiptRefs: List<ReceiptCrossRef>) = TODO()

    override fun getReceipt(id: Long): Flow<Receipt?> = flowOf(receipt)
    override fun getReceipts() = flowOf(listOf(receipt))
    override fun getItemNamesFlow() = flowOf(itemNames)
    override suspend fun getItemNames() = itemNames

    companion object {
        val itemNames = listOf(
            ReceiptItemName(1, "Apple"),
            ReceiptItemName(2, "Banana"),
            ReceiptItemName(3, "Orange"),
            ReceiptItemName(4, "Pear")
        )
        val items = itemNames.zip(listOf(23, 532, 57, 23))
            .map { (item, price) -> ReceiptItem(12, item.itemId, item.name, price) }
        val receipt = Receipt(12, "Conad", LocalDateTime.parse("2022-04-17T16:53:00"), items)
        val receipts =
            listOf(
                receipt,
                receipt.copy(id = 13, store = "Coop"),
                receipt.copy(id = 14, store = "Penny")
            )
        val invalidReceipt =
            receipt.copy(items = items + listOf(ReceiptItem(12, 0, "Strawberry", -1)))
    }
}
