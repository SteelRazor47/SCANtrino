package com.steelrazor47.scantrino.model

import com.steelrazor47.scantrino.utils.similarity
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class ReceiptsRepo @Inject constructor(private val receiptsDao: ReceiptsDao) {
    fun getReceipt(id: Long) = receiptsDao.getReceipt(id)

    fun getReceiptsWithMonth(month: YearMonth) =
        receiptsDao.getReceipts().map { list ->
            list.filter { YearMonth.from(it.date) == month }
        }

    fun getSimilarItems(name: String, count: Int) =
        receiptsDao.getItemNamesFlow()
            .map { list -> list.sortedBy { it.name.similarity(name) }.take(count) }

    suspend fun getMostSimilarItem(name: String) =
        receiptsDao.getItemNames()
            .maxByOrNull { it.name.similarity(name) }
            ?.takeIf { it.name.similarity(name) > 0.90 }


    suspend fun insertReceipt(receipt: Receipt) = receiptsDao.insertReceipt(receipt)

    suspend fun addItemName(name: ReceiptItemName): ReceiptItemName {
        val id = receiptsDao.setReceiptItemName(name)
        return ReceiptItemName(id, name.name)
    }
}
