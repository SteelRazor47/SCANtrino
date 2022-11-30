package com.steelrazor47.scantrino.ui.receipt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steelrazor47.scantrino.model.ReceiptsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject


@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val receiptsRepo: ReceiptsRepo,
    savedStateHandle: SavedStateHandle
) :
    ViewModel() {
    fun deleteReceipt() = viewModelScope.launch { receiptsRepo.deleteReceipt(id) }

    private val id: Long = checkNotNull(savedStateHandle["receiptId"])

    val receipt = receiptsRepo.getReceipt(id)
    val timeFilter = MutableStateFlow(TimeFilter.OneWeek)

    @OptIn(ExperimentalCoroutinesApi::class)
    val itemPriceAverages = timeFilter.flatMapLatest {
        when (it) {
            TimeFilter.LastSeen -> receiptsRepo.getPreviousItemPrices(id)
            TimeFilter.Always -> receiptsRepo.getItemPriceAverages(id)
            else -> receiptsRepo.getItemPriceAverages(
                id, LocalDate.now() - it.period, LocalDate.now()
            )
        }
    }


}


enum class TimeFilter(val displayName: String, val period: Period?) {
    LastSeen("Last seen", null),
    OneWeek("One week", Period.ofWeeks(1)),
    OneMonth("One month", Period.ofMonths(1)),
    Always("Always", null)
}
