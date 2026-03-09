package fr.isen.donnadieu.disney.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { _authState.value = AuthState.Success }
            .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Erreur") }
    }

    fun register(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { _authState.value = AuthState.Success }
            .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Erreur") }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun isLoggedIn() = auth.currentUser != null
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}