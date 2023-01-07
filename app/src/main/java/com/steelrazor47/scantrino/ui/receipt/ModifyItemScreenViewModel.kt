package com.steelrazor47.scantrino.ui.receipt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.steelrazor47.scantrino.model.ReceiptsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ModifyItemScreenViewModel @Inject constructor(
    receiptsRepo: ReceiptsRepo, savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val receiptId: Long = checkNotNull(savedStateHandle["receiptId"])
    private val itemId: Long = checkNotNull(savedStateHandle["receiptItemId"])
    val item = receiptsRepo.getItem(receiptId, itemId)
}
