package com.steelrazor47.scantrino.model

import javax.inject.Inject

class ReceiptsRepository @Inject constructor(private val receiptsDao: ReceiptsDao) {
    suspend fun getReceipts() = receiptsDao.getReceipts()
}