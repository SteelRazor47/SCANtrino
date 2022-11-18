package com.steelrazor47.scantrino.ui.overview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steelrazor47.scantrino.model.Receipt
import com.steelrazor47.scantrino.model.ReceiptsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(private val receiptsRepo: ReceiptsRepo) : ViewModel() {
    var uiState by mutableStateOf(OverviewUiState())
        private set

    init {
        changeMonth(uiState.month)
    }

    fun changeMonth(month: YearMonth) {
        viewModelScope.launch {
            val receipts = receiptsRepo.getReceiptsWithMonth(month).first()
            uiState = OverviewUiState(month, receipts)
        }
    }
}


data class OverviewUiState(
    val month: YearMonth = YearMonth.now(),
    val receipts: List<Receipt> = listOf()
) {
    val monthlyTotal = receipts.sumOf { it.total }
}
