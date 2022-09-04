package com.steelrazor47.scantrino

import javax.inject.Inject

class ReceiptsRepository @Inject constructor(private val receiptDao: ReceiptDao) {
    suspend fun getReceipts() = receiptDao.getReceipts()
}