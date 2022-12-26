package com.steelrazor47.scantrino.ui.receipt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steelrazor47.scantrino.model.StorageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Period
import javax.inject.Inject


@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val storageService: StorageService,
    savedStateHandle: SavedStateHandle
) :
    ViewModel() {
    fun deleteReceipt() = viewModelScope.launch { storageService.deleteReceipt(id) }

    private val id: String = checkNotNull(savedStateHandle["receiptId"])

    val receipt = storageService.getReceipt(id)
    val timeFilter = MutableStateFlow(TimeFilter.OneWeek)

    @OptIn(ExperimentalCoroutinesApi::class)
    val itemPriceAverages = timeFilter.mapLatest {
        storageService.getItemPriceVariations(id, it)
    }


}


enum class TimeFilter(val displayName: String, val period: Period?) {
    LastSeen("Last seen", null),
    Always("Always", null),
    OneWeek("One week", Period.ofWeeks(1)),
    OneMonth("One month", Period.ofMonths(1))
}
