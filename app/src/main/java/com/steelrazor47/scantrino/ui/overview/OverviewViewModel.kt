package com.steelrazor47.scantrino.ui.overview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steelrazor47.scantrino.model.Receipt
import com.steelrazor47.scantrino.model.service.AccountService
import com.steelrazor47.scantrino.model.service.StorageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val storageService: StorageService,
    private val accountService: AccountService
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    var uiState = month.flatMapLatest { month ->
        Log.d("testing", "storage")
        storageService.getReceiptsWithMonth(month).mapLatest {
            OverviewUiState(month, it)
        }
    }

    fun changeMonth(newMonth: YearMonth) {
        month.value = newMonth
    }

    fun signin(email: String, password: String) =
        viewModelScope.launch { accountService.linkAccount(email, password) }

    fun signOut() = viewModelScope.launch { accountService.signOut() }

    fun onStart() {
        runBlocking {
            if (!hasUser) accountService.createAnonymousAccount()
        }
    }

    val user = accountService.currentUser
    val hasUser get() = accountService.hasUser

}


data class OverviewUiState(
    val month: YearMonth = YearMonth.now(),
    val receipts: List<Receipt> = listOf()
) {
    val monthlyTotal = receipts.sumOf { it.total }
}
