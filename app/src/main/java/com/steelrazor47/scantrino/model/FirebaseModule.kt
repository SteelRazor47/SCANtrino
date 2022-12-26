package com.steelrazor47.scantrino.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    fun auth(): FirebaseAuth = Firebase.auth

    @Provides
    fun firestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    fun accountService(auth: FirebaseAuth): AccountService = AccountService(auth)

    @Provides
    fun storageService(firestore: FirebaseFirestore, account: AccountService): StorageService =
        StorageService(firestore, account)
}