package com.steelrazor47.scantrino.model

import com.steelrazor47.scantrino.utils.similarity
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class ReceiptsRepo @Inject constructor(private val receiptsDao: ReceiptsDao) {

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
