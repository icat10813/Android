package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferences
import com.example.domain.model.User
import com.example.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successUser: User? = null
)

class AuthViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String, rememberMe: Boolean) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Email dan password tidak boleh kosong.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            // Simulate API Network Delay
            kotlinx.coroutines.delay(1000)

            val role = when {
                email.contains("kasir", ignoreCase = true) -> UserRole.KASIR
                email.contains("gudang", ignoreCase = true) -> UserRole.STAFF_GUDANG
                else -> UserRole.ADMIN
            }

            val name = when (role) {
                UserRole.ADMIN -> "Administrator Utama"
                UserRole.KASIR -> "Rudi Kasir POS"
                UserRole.STAFF_GUDANG -> "Bambang Gudang"
            }

            val user = User(
                id = "usr_${System.currentTimeMillis()}",
                name = name,
                email = email,
                role = role,
                token = "jwt_token_sample_${System.currentTimeMillis()}"
            )

            if (rememberMe) {
                userPreferences.saveUserSession(user, user.token!!)
            }

            _uiState.value = AuthUiState(isLoading = false, successUser = user)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearSession()
        }
    }
}

class AuthViewModelFactory(private val userPreferences: UserPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
