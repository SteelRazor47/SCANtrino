package com.steelrazor47.scantrino.model

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "receipts")
data class Receipt(@PrimaryKey(autoGenerate = true) val id: Int, val name: String, val price: Int)

@Dao
interface ReceiptsDao{
    @Query("SELECT * FROM receipts")
    suspend fun getReceipts(): List<Receipt>
}
