package com.steelrazor47.scantrino.model

import com.steelrazor47.scantrino.utils.similarity
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class ReceiptsRepo @Inject constructor(private val receiptsDao: ReceiptsDao) {
    fun getReceipt(id: Long) = receiptsDao.getReceipt(id)

    fun getReceiptsWithMonth(month: YearMonth) =
        receiptsDao.getReceipts().map { list ->
            list.filter { YearMonth.from(it.date) == month }
        }

    fun getItemPriceAverages(id: Long, startDate: LocalDate, endDate: LocalDate) =
        receiptsDao.getItemsPriceAverage(id, startDate.toString(), endDate.toString())

    fun getItemPriceAverages(id: Long) =
        receiptsDao.getItemsPriceAverage(id)

    fun getPreviousItemPrices(receiptId: Long) = receiptsDao.getPreviousItemPrices(receiptId)

    suspend fun getMostSimilarItem(name: String) =
        receiptsDao.getItemNames()
            .maxByOrNull { it.name.similarity(name) }
            ?.takeIf { it.name.similarity(name) > 0.90 }

    suspend fun getSimilarItems(name: String, count: Int) =
        receiptsDao.getItemNames().sortedBy { it.name.similarity(name) }.take(count)

    suspend fun insertReceipt(receipt: Receipt) = receiptsDao.insertReceipt(receipt)

    suspend fun addItemName(name: ReceiptItemName) = receiptsDao.setReceiptItemName(name)

    suspend fun deleteReceipt(id: Long) = receiptsDao.deleteReceipt(id)
}
