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

    @MapInfo(keyColumn = "receiptItemId", valueColumn = "average")
    @Query(
        """SELECT receiptItemId, SUM(price)/COUNT(receiptItemId) AS average
            FROM receipt_cross_ref CR JOIN receipts R ON CR.receiptId = R.id
            WHERE CR.receiptItemId IN (SELECT receiptItemId FROM receipt_cross_ref WHERE receiptId = :receiptId)
            AND date(R.date) <= :endDate AND  date(R.date) >= :startDate 
            AND R.date < (SELECT date from receipts WHERE id = :receiptId)
            GROUP BY receiptItemId"""
    )
    fun getItemsPriceAverage(
        receiptId: Long,
        startDate: String,
        endDate: String
    ): Flow<Map<Long, Double>>

    @MapInfo(keyColumn = "receiptItemId", valueColumn = "price")
    @Query(
        """
        SELECT CR2.receiptItemId, CR2.price * 1.0 AS price
        FROM receipt_cross_ref CR1 JOIN receipts R1 ON R1.id = CR1.receiptId
        JOIN receipt_cross_ref CR2 JOIN receipts R2 ON R2.id = CR2.receiptId
        WHERE CR1.receiptId = :receiptId AND CR1.receiptItemId = CR2.receiptItemId 
        AND R2.date < R1.date 
        AND R2.date = (SELECT MAX(R.date) 
                        FROM receipt_cross_ref CR JOIN receipts R ON R.id = CR.receiptId
                        WHERE CR.receiptItemId = CR2.receiptItemId AND R.date < R1.date)
    """
    )
    fun getPreviousItemPrices(receiptId: Long): Flow<Map<Long, Double>>


    @Query("SELECT * FROM receipt_item_names")
    fun getItemNamesFlow(): Flow<List<ReceiptItemName>>

    @Query("SELECT * FROM receipt_item_names")
    suspend fun getItemNames(): List<ReceiptItemName>

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteReceipt(id: Long)
}

class ReceiptsDaoMock : ReceiptsDao {
    override suspend fun setReceiptInfo(receiptInfo: ReceiptInfo): Long = TODO()
    override suspend fun setReceiptItemNames(receiptItems: List<ReceiptItemName>) = TODO()
    override suspend fun setReceiptItemName(name: ReceiptItemName): Long = TODO()
    override suspend fun setReceiptMappings(receiptRefs: List<ReceiptCrossRef>) = TODO()
    override suspend fun deleteReceipt(id: Long) = TODO()

    override fun getReceipt(id: Long): Flow<Receipt?> = flowOf(receipt)
    override fun getReceipts() = flowOf(listOf(receipt))
    override fun getItemNamesFlow() = flowOf(itemNames)
    override suspend fun getItemNames() = itemNames

    override fun getItemsPriceAverage(
        receiptId: Long,
        startDate: String,
        endDate: String
    ): Flow<Map<Long, Double>> = flowOf(itemAverages)

    override fun getPreviousItemPrices(receiptId: Long): Flow<Map<Long, Double>> = TODO()

    companion object {
        val itemNames = listOf(
            ReceiptItemName(1, "Apple"),
            ReceiptItemName(2, "Banana"),
            ReceiptItemName(3, "Orange"),
            ReceiptItemName(4, "Pear")
        )
        val items = itemNames.zip(listOf(23, 532, 57, 23))
            .map { (item, price) -> ReceiptItem(12, item.itemId, item.name, price) }
        val itemAverages = mapOf(1L to 20.0, 2L to 550.0, 3L to 90.0, 4L to 10.0)
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
