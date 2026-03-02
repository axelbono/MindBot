package com.bono.mentalbot.ui.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bono.mentalbot.data.remote.firebase.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(context: Context) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mindbot_prefs", Context.MODE_PRIVATE)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    private val _isNewUser = MutableStateFlow(false)
    val isNewUser: StateFlow<Boolean> = _isNewUser

    private val firestoreService = FirestoreService()

    private val _rememberSession = MutableStateFlow(
        prefs.getBoolean("remember_session", false)
    )
    val rememberSession: StateFlow<Boolean> = _rememberSession

    init {
        val remember = prefs.getBoolean("remember_session", false)
        if (auth.currentUser != null && remember) {
            _isAuthenticated.value = true
            loadUserName()
        } else {
            auth.signOut()
        }
    }

    fun toggleRememberSession() {
        val newValue = !_rememberSession.value
        _rememberSession.value = newValue
        prefs.edit().putBoolean("remember_session", newValue).apply()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _isNewUser.value = false
                _isAuthenticated.value = true
                loadUserName()
            } catch (e: Exception) {
                _error.value = when {
                    e.message?.contains("password") == true -> "Contraseña incorrecta"
                    e.message?.contains("user") == true -> "Usuario no encontrado"
                    e.message?.contains("email") == true -> "Correo inválido"
                    else -> "Error al iniciar sesión"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _isNewUser.value = true
                _isAuthenticated.value = true
            } catch (e: Exception) {
                _error.value = when {
                    e.message?.contains("email") == true -> "Correo ya registrado o inválido"
                    e.message?.contains("password") == true -> "La contraseña debe tener al menos 6 caracteres"
                    else -> "Error al registrarse"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        auth.signOut()
        _isAuthenticated.value = false
        _isNewUser.value = false
        _userName.value = ""
        _rememberSession.value = false
        prefs.edit().putBoolean("remember_session", false).apply()
    }

    fun saveUserName(name: String) {
        viewModelScope.launch {
            firestoreService.saveUserName(name)
            _userName.value = name
        }
    }

    fun loadUserName() {
        viewModelScope.launch {
            _userName.value = firestoreService.getUserName()
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return AuthViewModel(context) as T
                }
            }
        }
    }
}