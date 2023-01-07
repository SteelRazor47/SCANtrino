package com.steelrazor47.scantrino.model.service

import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.steelrazor47.scantrino.model.ItemName
import com.steelrazor47.scantrino.model.Receipt
import com.steelrazor47.scantrino.model.ReceiptItem
import com.steelrazor47.scantrino.ui.receipt.TimeFilter
import com.steelrazor47.scantrino.utils.similarity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
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
    fun getReceipt(receiptId: String): Flow<Receipt?> =
        auth.currentUserFlow.filterNotNull().flatMapLatest { user ->
            receiptsCollection(user.id).document(receiptId).snapshots().map { it.toReceipt() }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getReceiptsWithMonth(month: YearMonth): Flow<List<Receipt>> {
        val start = month.atDay(1).atStartOfDay()
        return auth.currentUserFlow.filterNotNull().flatMapLatest { user ->
            receiptsCollection(user.id).whereBetween(start, start.plusMonths(1))
                .snapshots().map { it.toReceipts() }
        }
    }

    suspend fun getMostSimilarName(name: String): ItemName? =
        namesCollection().get().await().toItemNames()
            .maxByOrNull { it.name.similarity(name) }
            ?.takeIf { it.name.similarity(name) > 0.9 }

    suspend fun getSimilarNames(name: String, count: Int): List<ItemName> =
        namesCollection().get().await().toItemNames()
            .sortedByDescending { it.name.similarity(name) }.take(count)

    suspend fun getItemPriceVariations(
        receiptId: String, filter: TimeFilter
    ): Map<String, Double?> {
        val receipt = receiptsCollection().document(receiptId)
            .get().await().toReceipt() ?: return mapOf()
        val date = receipt.date
        val variations = receipt.items.associate { item ->
            val id = item.name.id
            val start = when (filter) {
                TimeFilter.OneWeek, TimeFilter.OneMonth -> date - filter.period
                TimeFilter.Always, TimeFilter.LastSeen -> Timestamp(0, 0).toLocalDateTime()
            }
            var query = pricesCollection(id).whereBetween(start, date)
            if (filter == TimeFilter.LastSeen)
                query = query.orderBy("timestamp", Query.Direction.DESCENDING).limit(1)

            val items = query.get().await().toObjects<PriceDto>()
            // Equals NaN if items is empty
            val avg = items.map { it.price }.average()

            id to (item.price / avg - 1).takeIf { !it.isNaN() }
        }
        return variations
    }


    suspend fun addReceipt(receipt: Receipt): String {
        val dto = ReceiptDto(receipt)
        val id = receiptsCollection().add(dto).await().id
        // Data duplication for easier querying of statistics by ItemName
        Tasks.whenAll(dto.items.map { item ->
            pricesCollection(item.nameId).document(id).set(PriceDto(dto.timestamp, item.price))
        }).await()
        return id
    }

    suspend fun addItemName(name: ItemName) =
        namesCollection().add(NameDto(name.id, name.name)).await().id

    suspend fun deleteReceipt(receiptId: String) {
        val receiptRef = receiptsCollection().document(receiptId)
        val receipt = receiptRef.get().await().toReceipt() ?: return
        val items = receipt.items.map { it.name.id }

        items.forEach { nameId ->
            pricesCollection(nameId).document(receiptId).delete().await()
        }

        receiptRef.delete().await()
    }

    suspend fun deleteUserData() {
        val userDoc = firestore.collection(USER_COLLECTION).document(auth.currentUser!!.id)
        for (document in userDoc.collection(RECEIPT_COLLECTION).get().await().documents) {
            userDoc.collection(RECEIPT_COLLECTION).document(document.id).delete().await()
        }
        for (name in userDoc.collection(NAME_COLLECTION).get().await().documents) {
            val nameDoc = userDoc.collection(NAME_COLLECTION).document(name.id)
            for (price in nameDoc.collection("prices").get().await().documents) {
                nameDoc.collection("prices").document(price.id).delete().await()
            }
            nameDoc.delete().await()
        }
        userDoc.delete().await()
    }

    private fun receiptsCollection(uid: String = auth.currentUser!!.id) =
        firestore.collection(USER_COLLECTION).document(uid).collection(RECEIPT_COLLECTION)

    private fun pricesCollection(itemNameId: String) =
        namesCollection().document(itemNameId).collection("prices")

    private fun namesCollection(uid: String = auth.currentUser!!.id) =
        firestore.collection(USER_COLLECTION).document(uid).collection(NAME_COLLECTION)

    companion object {
        private const val USER_COLLECTION = "users"
        private const val RECEIPT_COLLECTION = "receipts"
        private const val NAME_COLLECTION = "itemNames"
    }
}

private fun DocumentSnapshot.toReceipt() = this.toObject<ReceiptDto>()?.toReceipt()
private fun QuerySnapshot.toReceipts() = this.toObjects<ReceiptDto>().map { it.toReceipt() }
private fun QuerySnapshot.toItemNames() = this.toObjects<NameDto>().map { it.toItemName() }

private data class ReceiptDto(
    @DocumentId val id: String = "",
    val store: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val items: List<ItemDto> = listOf()
) {
    constructor(r: Receipt) : this(r.id, r.store, r.date.toTimestamp(),
        r.items.map { ItemDto(it.name.id, it.name.name, it.price) })

    fun toReceipt() = Receipt(id, store, timestamp.toLocalDateTime(),
        items.map { ReceiptItem(ItemName(it.nameId, it.name), it.price) })
}

private data class NameDto(@DocumentId val id: String = "", val name: String = "") {
    fun toItemName() = ItemName(id, name)
}

private data class ItemDto(val nameId: String = "", val name: String = "", val price: Int = 0)

private data class PriceDto(val timestamp: Timestamp = Timestamp(0, 0), val price: Int = 0)

private fun Timestamp.toLocalDateTime() =
    LocalDateTime.ofInstant(this.toDate().toInstant(), ZoneId.of(ZoneOffset.UTC.id))

private fun LocalDateTime.toTimestamp() = Timestamp(Date.from(this.toInstant(ZoneOffset.UTC)))

private fun CollectionReference.whereBetween(start: Timestamp, endExclusive: Timestamp) =
    this.whereGreaterThanOrEqualTo("timestamp", start).whereLessThan("timestamp", endExclusive)


private fun CollectionReference.whereBetween(start: LocalDateTime, endExclusive: LocalDateTime) =
    this.whereBetween(start.toTimestamp(), endExclusive.toTimestamp())
