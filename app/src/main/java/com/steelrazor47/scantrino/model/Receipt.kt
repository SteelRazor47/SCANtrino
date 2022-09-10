package com.steelrazor47.scantrino.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

data class Receipt(
//    @Embedded
//    val info: ReceiptInfo,
    val id: Int = 0,
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
    val items: List<ReceiptItem>
)

@Dao
interface ReceiptsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setReceiptInfo(receiptInfo: ReceiptInfo): Long // Row Id

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setReceiptItemInfos(receiptItems: List<ReceiptItemInfo>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setReceiptMappings(receiptRefs: List<ReceiptCrossRef>)

    @Transaction
    @Query("SELECT * FROM receipts")
    fun getReceipts(): Flow<List<Receipt>>

    @Transaction
    @Query("SELECT * FROM receipts WHERE receipts.id = :id")
    fun getReceipt(id: Long): Flow<Receipt?>

}
