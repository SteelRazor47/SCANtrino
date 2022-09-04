package com.steelrazor47.scantrino

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity
data class Receipt(@PrimaryKey(autoGenerate = true) val id: Int, val name: String, val price: Int)

@Dao
interface ReceiptDao{
    @Query("SELECT * FROM receipt")
    suspend fun getReceipts(): List<Receipt>
}
