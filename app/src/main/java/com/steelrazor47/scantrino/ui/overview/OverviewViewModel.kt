package com.steelrazor47.scantrino.ui.overview

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
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
import kotlinx.coroutines.tasks.await
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
        storageService.getReceiptsWithMonth(month).mapLatest { OverviewUiState(month, it) }
    }

    fun changeMonth(newMonth: YearMonth) {
        month.value = newMonth
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            if (accountService.hasUser && accountService.currentUser!!.isAnonymous) {
                storageService.deleteUserData()
                AuthUI.getInstance().delete(context).await()
            }
            AuthUI.getInstance().signOut(context).await()

            accountService.createAnonymousAccount()
        }
    }

    fun onSignIn(result: FirebaseAuthUIAuthenticationResult, toast: (String) -> Unit) {
        val response: IdpResponse? = result.idpResponse
        if (result.resultCode == Activity.RESULT_OK) {
            toast("Logged in")
            return
        }

        val userPressedBackButton = (response == null)
        if (userPressedBackButton) {
            toast("Cancelled")
            return
        }

        when (response?.error?.errorCode) {
            ErrorCodes.NO_NETWORK -> toast("Network unavailable")
            ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT -> {
                toast("Merge/delete")
                viewModelScope.launch {
                    storageService.deleteUserData()
                    accountService.delete()
                    accountService.signIn(response.credentialForLinking!!)
                }
            }
            else -> toast("Login failed")
        }
    }

    fun onStart() {
        runBlocking {
            if (!accountService.hasUser) accountService.createAnonymousAccount()
        }
    }

    val user = accountService.currentUserFlow
    val hasUser get() = accountService.hasUser

}


data class OverviewUiState(
    val month: YearMonth = YearMonth.now(),
    val receipts: List<Receipt> = listOf()
) {
    val monthlyTotal = receipts.sumOf { it.total }
}
