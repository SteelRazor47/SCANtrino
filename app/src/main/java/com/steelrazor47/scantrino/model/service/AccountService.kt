package com.steelrazor47.scantrino.model.service

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.steelrazor47.scantrino.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountService @Inject constructor(private val auth: FirebaseAuth) {

    val currentUser: User?
        get() = auth.currentUser?.let { User(it.uid, it.isAnonymous) }

    val hasUser: Boolean
        get() = auth.currentUser != null

    val currentUserFlow: Flow<User?>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { auth ->
                    this.trySend(auth.currentUser?.let { User(it.uid, it.isAnonymous) })
                }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    suspend fun createAnonymousAccount() {
        auth.signInAnonymously().await()
    }

    suspend fun signIn(credential: AuthCredential) {
        auth.signInWithCredential(credential).await()
    }

    fun delete() {
        auth.currentUser!!.delete()
    }
}
