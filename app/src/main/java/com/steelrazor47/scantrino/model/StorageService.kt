package com.steelrazor47.scantrino.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.steelrazor47.scantrino.ui.receipt.TimeFilter
import com.steelrazor47.scantrino.utils.similarity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject

class StorageService
@Inject
constructor(private val firestore: FirebaseFirestore, private val auth: AccountService) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getReceipts(): Flow<List<Receipt>> = auth.currentUser.flatMapLatest { user ->
        receiptsCollection(user.id).snapshots().map { snapshot -> snapshot.toObjects() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getReceipt(receiptId: String): Flow<Receipt?> = auth.currentUser.flatMapLatest { user ->
        receiptsCollection(user.id).document(receiptId).snapshots().map { it.toObject() }
    }

    suspend fun getReceiptsWithMonth(month: YearMonth): List<Receipt> {
        val startTime = month.atDay(1).atStartOfDay()
        return receiptsInRange(startTime, startTime.plusMonths(1)).get().await().toObjects()
    }

    suspend fun getMostSimilarName(name: String): ItemName? =
        namesCollection().get().await().toObjects<ItemName>()
            .maxByOrNull { it.name.similarity(name) }
            ?.takeIf { it.name.similarity(name) > 0.9 }

    suspend fun getSimilarNames(name: String, count: Int): List<ItemName> =
        namesCollection().get().await().toObjects<ItemName>()
            .sortedByDescending { it.name.similarity(name) }.take(count)

    suspend fun getItemPriceVariations(
        receiptId: String,
        filter: TimeFilter
    ): Map<String, Double?> {
        val receipt = receiptsCollection(auth.currentUserId).document(receiptId)
            .get().await().toObject<Receipt>() ?: return mapOf()
        val date = receipt.date
        val itemNames = receipt.items.map { it.name }
        val previousReceipts = when (filter) {
            TimeFilter.OneWeek, TimeFilter.OneMonth -> receiptsInRange(date - filter.period, date)
            TimeFilter.Always, TimeFilter.LastSeen -> receiptsInRange(
                LocalDateTime.ofEpochSecond(
                    0,
                    0,
                    ZoneOffset.UTC
                ), date
            )
        }.get().await().toObjects<Receipt>()
            .filter { it.items.any { itemNames.contains(it.name) } }

        val averages = when (filter) {
            TimeFilter.LastSeen -> previousReceipts.flatMap { r ->
                r.items.map { it to r.timestamp }
            }.groupBy { (item, _) -> item.name }
                .filter { (name, _) -> itemNames.contains(name) }
                .mapValues { (_, list) -> list.maxBy { it.second }.first.price }

            else -> previousReceipts.flatMap { it.items }
                .groupBy { it.name }
                .filter { (name, _) -> itemNames.contains(name) }
                .mapValues { (_, items) -> items.sumOf { it.price } }
        }.mapKeys { (name, _) -> name.id }

        return averages.mapValues { (nameId, avg) ->
            val price = receipt.items.find { it.name.id == nameId }?.price?.toDouble()
            price?.div(avg)?.minus(1)
        }
    }


    suspend fun addReceipt(receipt: Receipt) =
        receiptsCollection().add(receipt).await().id

    suspend fun addItemName(name: ItemName) =
        namesCollection().add(name).await().id

    suspend fun deleteReceipt(receiptId: String) {
        receiptsCollection().document(receiptId).delete().await()
    }


    private fun receiptsCollection(uid: String = auth.currentUserId) =
        firestore.collection(USER_COLLECTION).document(uid).collection(RECEIPT_COLLECTION)

    private fun receiptsInRange(start: LocalDateTime, endExclusive: LocalDateTime) =
        receiptsCollection(auth.currentUserId)
            .whereGreaterThanOrEqualTo(
                "timestamp",
                Timestamp(Date.from(start.atZone(ZoneId.systemDefault()).toInstant()))
            )
            .whereLessThan(
                "timestamp",
                Timestamp(Date.from(endExclusive.atZone(ZoneId.systemDefault()).toInstant()))
            )

    private fun namesCollection() = firestore.collection(NAME_COLLECTION)

    companion object {
        private const val USER_COLLECTION = "users"
        private const val RECEIPT_COLLECTION = "receipts"
        private const val NAME_COLLECTION = "itemNames"
    }
}
