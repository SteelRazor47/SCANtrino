package com.steelrazor47.scantrino.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ReceiptsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setReceiptInfo(details: Details): Long // Row Id

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun setReceiptItemNames(receiptItems: List<ItemName>): List<Long>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun setReceiptItemName(name: ItemName): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun setReceiptMappings(receiptRefs: List<ItemPrice>)

    @Transaction
    suspend fun insertReceipt(receipt: Receipt) {
        val receiptId = setReceiptInfo(receipt.details)
        val newItems = receipt.items.filter { it.itemNameId == 0L }
        val itemIds = setReceiptItemNames(newItems.map { it.itemName })
        val mappings =
            itemIds.zip(newItems.map { it.price }).map { (id, price) ->
                ItemPrice(
                    receiptId,
                    id,
                    price
                )
            } + receipt.items.filter { it.itemNameId != 0L }
                .map { ItemPrice(receiptId, it.itemNameId, it.price) }
        setReceiptMappings(mappings)
    }

    @Transaction
    @Query("SELECT * FROM receipts WHERE receipts.id = :id")
    fun getReceipt(id: Long): Flow<Receipt?>

    @Transaction
    @Query("SELECT * FROM receipts")
    fun getReceipts(): Flow<List<Receipt>>

    @Query(
        """SELECT * FROM item_prices I 
        JOIN item_names ON I.itemNameId = item_names.id
        WHERE I.itemNameId = :itemNameId AND I.receiptId = :receiptId"""
    )
    fun getItem(receiptId: Long, itemNameId: Long): Flow<ReceiptItem?>

    @MapInfo(keyColumn = "itemNameId", valueColumn = "average")
    @Query(
        """SELECT itemNameId, SUM(price)/COUNT(itemNameId) AS average
            FROM item_prices CR JOIN receipts R ON CR.receiptId = R.id
            WHERE CR.itemNameId IN (:itemNameIds) AND R.date < :endDate AND R.date >= :startDate
            GROUP BY itemNameId"""
    )
    fun getItemsPriceAverage(
        itemNameIds: List<Long>, startDate: LocalDateTime, endDate: LocalDateTime
    ): Flow<Map<Long, Double>>

    @MapInfo(keyColumn = "itemNameId", valueColumn = "price")
    @Query(
        """SELECT itemNameId, price * 1.0 as price 
            FROM(
                SELECT itemNameId, price, date
                FROM item_prices CR JOIN receipts R ON CR.receiptId = R.id
                WHERE R.date < :date AND itemNameId IN (:itemNameIds)
                ORDER BY itemNameId, date DESC)
            GROUP BY itemNameId"""
    )
    fun getItemsPreviousPrice(
        itemNameIds: List<Long>, date: LocalDateTime
    ): Flow<Map<Long, Double>>

    @Query("SELECT * FROM item_names")
    suspend fun getItemNames(): List<ItemName>

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteReceipt(id: Long)

}

class DataMock {

    companion object {
        val itemNames = listOf(
            ItemName(1, "Apple"),
            ItemName(2, "Banana"),
            ItemName(3, "Orange"),
            ItemName(4, "Pear")
        )
        val items = itemNames.zip(listOf(23, 532, 57, 23))
            .map { (item, price) -> ReceiptItem(12, item.id, item.name, price) }
        val itemVariations = mapOf(1L to 0.045, 2L to 0.23, 3L to -0.156, 4L to 3.2)
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
