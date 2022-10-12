package com.steelrazor47.scantrino.model

import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class ReceiptsRepo @Inject constructor(private val receiptsDao: ReceiptsDao) {

    fun getSimilarItems(name: String, count: Int) =
        receiptsDao.getItemNamesFlow()
            .map { list -> list.sortedBy { levenshtein(it.name, name) }.take(count) }

    suspend fun getMostSimilarItem(name: String) =
        receiptsDao.getItemNames()
            .maxByOrNull { similarity(it.name, name) }
            ?.takeIf { similarity(it.name, name) > 0.90 }


    suspend fun insertReceipt(receipt: Receipt) = receiptsDao.insertReceipt(receipt)
    suspend fun addItemName(name: ReceiptItemName): ReceiptItemName {
        val id = receiptsDao.setReceiptItemNames(listOf(name)).first()
        return ReceiptItemName(id, name.name)
    }


    private fun similarity(s: String, t: String) =
        1 - levenshtein(s.uppercase(), t.uppercase()) / max(s.length, t.length).toFloat()

    private fun levenshtein(s: String, t: String): Int {
        // degenerate cases
        if (s == t) return 0
        if (s == "") return t.length
        if (t == "") return s.length

        // create two integer arrays of distances and initialize the first one
        val v0 = IntArray(t.length + 1) { it }  // previous
        val v1 = IntArray(t.length + 1)         // current

        var cost: Int
        for (i in s.indices) {
            // calculate v1 from v0
            v1[0] = i + 1
            for (j in t.indices) {
                cost = if (s[i] == t[j]) 0 else 1
                v1[j + 1] = min(v1[j] + 1, min(v0[j + 1] + 1, v0[j] + cost))
            }
            // copy v1 to v0 for next iteration
            for (j in 0..t.length) v0[j] = v1[j]
        }
        return v1[t.length]
    }
}
