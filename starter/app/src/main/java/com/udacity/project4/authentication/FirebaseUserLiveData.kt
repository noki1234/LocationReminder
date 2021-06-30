package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

enum class AuthenticationState{
    AUTHENTICATED,
    UNAUTHENTICATED,
}

class FirebaseUserLiveData: LiveData<FirebaseUser?>() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        value =  firebaseAuth.currentUser
    }

    val authenticationState = this.map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    override fun onActive() { firebaseAuth.addAuthStateListener(authStateListener) }
    override fun onInactive() { firebaseAuth.removeAuthStateListener(authStateListener) }
}