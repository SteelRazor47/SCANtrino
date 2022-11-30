package com.steelrazor47.scantrino.ui.overview

import androidx.lifecycle.ViewModel
import com.steelrazor47.scantrino.model.Receipt
import com.steelrazor47.scantrino.model.ReceiptsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(private val receiptsRepo: ReceiptsRepo) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    var uiState = month.flatMapLatest { month ->
        receiptsRepo.getReceiptsWithMonth(month).mapLatest { OverviewUiState(month, it) }
    }

    fun changeMonth(newMonth: YearMonth) {
        month.value = newMonth
    }
}


data class OverviewUiState(
    val month: YearMonth = YearMonth.now(),
    val receipts: List<Receipt> = listOf()
) {
    val monthlyTotal = receipts.sumOf { it.total }
}
