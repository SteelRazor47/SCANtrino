package com.steelrazor47.scantrino.ui.overview

import androidx.lifecycle.ViewModel
import com.steelrazor47.scantrino.model.ReceiptsDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(val receiptsDao: ReceiptsDao) : ViewModel()
