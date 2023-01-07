package com.steelrazor47.scantrino.model

import com.steelrazor47.scantrino.ui.receipt.TimeFilter
import com.steelrazor47.scantrino.utils.similarity
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

class ReceiptsRepo @Inject constructor(private val receiptsDao: ReceiptsDao) {
    fun getReceipt(id: Long) = receiptsDao.getReceipt(id)
    fun getItem(receiptId: Long, itemNameId: Long) = receiptsDao.getItem(receiptId, itemNameId)

    fun getReceiptsWithMonth(month: YearMonth) =
        receiptsDao.getReceipts().map { list ->
            list.filter { YearMonth.from(it.date) == month }
        }

    fun getItemPriceAverages(receipt: Receipt, timeFilter: TimeFilter) =
        receipt.items.map { it.itemNameId }.let {
            when (timeFilter) {
                TimeFilter.LastSeen ->
                    receiptsDao.getItemsPreviousPrice(it, receipt.date)
                TimeFilter.Always ->
                    receiptsDao.getItemsPriceAverage(it, LocalDateTime.MIN, receipt.date)
                else ->
                    receiptsDao.getItemsPriceAverage(
                        it,
                        receipt.date - timeFilter.period,
                        receipt.date
                    )

            }
        }


    suspend fun getMostSimilarItem(name: String) =
        receiptsDao.getItemNames()
            .maxByOrNull { it.name.similarity(name) }
            ?.takeIf { it.name.similarity(name) > 0.90 }

    suspend fun getSimilarItems(name: String, count: Int) =
        receiptsDao.getItemNames().sortedByDescending { it.name.similarity(name) }.take(count)

    suspend fun insertReceipt(receipt: Receipt) = receiptsDao.insertReceipt(receipt)

    suspend fun addItemName(name: ItemName) = receiptsDao.setReceiptItemName(name)

    suspend fun deleteReceipt(id: Long) = receiptsDao.deleteReceipt(id)
}
