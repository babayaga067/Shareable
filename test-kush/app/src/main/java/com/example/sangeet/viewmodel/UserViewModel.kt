package com.example.sangeet.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sangeet.model.UserModel
import com.example.sangeet.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import android.util.Log

class UserViewModel(private val repo: UserRepository) : ViewModel() {

    private val _user = MutableLiveData<UserModel?>()
    val user: LiveData<UserModel?> get() = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            callback(false, "Email and password cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                repo.login(email, password) { success, message ->
                    _isLoading.value = false
                    if (!success) {
                        _errorMessage.value = message
                    }
                    callback(success, message)
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMsg = "Login error: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e("UserViewModel", errorMsg, e)
                callback(false, errorMsg)
            }
        }
    }

    fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        if (email.isEmpty() || password.isEmpty()) {
            callback(false, "Email and password cannot be empty", "")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                repo.register(email, password) { success, message, userId ->
                    _isLoading.value = false
                    if (!success) {
                        _errorMessage.value = message
                    }
                    callback(success, message, userId)
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMsg = "Registration error: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e("UserViewModel", errorMsg, e)
                callback(false, errorMsg, "")
            }
        }
    }

    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        if (email.isEmpty()) {
            callback(false, "Email cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                repo.forgetPassword(email) { success, message ->
                    _isLoading.value = false
                    if (!success) {
                        _errorMessage.value = message
                    }
                    callback(success, message)
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMsg = "Password reset error: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e("UserViewModel", errorMsg, e)
                callback(false, errorMsg)
            }
        }
    }

    fun updatePassword(
        userId: String,
        oldPass: String,
        newPass: String,
        callback: (Boolean, String) -> Unit
    ) {
        if (userId.isEmpty() || oldPass.isEmpty() || newPass.isEmpty()) {
            callback(false, "All fields are required")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                repo.updatePassword(userId, oldPass, newPass) { success, message ->
                    _isLoading.value = false
                    if (!success) {
                        _errorMessage.value = message
                    }
                    callback(success, message)
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMsg = "Password update error: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e("UserViewModel", errorMsg, e)
                callback(false, errorMsg)
            }
        }
    }

    fun logout(callback: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _user.value = null
                _errorMessage.value = null

                repo.logout {
                    _isLoading.value = false
                    callback()
                }
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e("UserViewModel", "Logout error: ${e.message}", e)
                callback()
            }
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return try {
            repo.getCurrentUser()
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error getting current user: ${e.message}", e)
            null
        }
    }

    fun addUserToDatabase(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        if (userId.isEmpty()) {
            callback(false, "User ID cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                repo.addUserToDatabase(userId, model) { success, message ->
                    _isLoading.value = false
                    if (success) {
                        getUserById(userId)
                    } else {
                        _errorMessage.value = message
                    }
                    callback(success, message)
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMsg = "Error adding user to database: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e("UserViewModel", errorMsg, e)
                callback(false, errorMsg)
            }
        }
    }

    fun updateProfile(
        userId: String,
        data: Map<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        if (userId.isEmpty()) {
            callback(false, "User ID cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                repo.updateProfile(userId, data) { success, message ->
                    _isLoading.value = false
                    if (success) {
                        getUserById(userId)
                    } else {
                        _errorMessage.value = message
                    }
                    callback(success, message)
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMsg = "Error updating profile: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e("UserViewModel", errorMsg, e)
                callback(false, errorMsg)
            }
        }
    }

    fun getUserById(userId: String) {
        if (userId.isEmpty()) {
            _user.postValue(null)
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                repo.getUserById(userId) { success, message, fetchedUser ->
                    _isLoading.value = false
                    if (success) {
                        _user.postValue(fetchedUser)
                    } else {
                        _user.postValue(null)
                        _errorMessage.value = message
                        Log.e("UserViewModel", "Error getting user: $message")
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _user.postValue(null)
                val errorMsg = "Error fetching user: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e("UserViewModel", errorMsg, e)
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearUserData() {
        _user.value = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("UserViewModel", "ViewModel cleared")
    }
}
