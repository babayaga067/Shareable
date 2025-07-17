package com.example.sangeet.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sangeet.model.UserModel
import com.example.sangeet.repository.UserRepository
import com.google.firebase.auth.FirebaseUser

class UserViewModel(private val repo: UserRepository) : ViewModel() {

    // Auth
    fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        repo.login(email, password, callback)
    }

    fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        repo.register(email, password, callback)
    }

    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        repo.forgetPassword(email, callback)
    }

    fun updatePassword(userId: String, oldPass: String, newPass: String, callback: (Boolean, String) -> Unit) {
        repo.updatePassword(userId, oldPass, newPass, callback)
    }

    fun logout(callback: () -> Unit) {
        repo.logout(callback)
    }

    fun getCurrentUser(): FirebaseUser? = repo.getCurrentUser()

    // Database
    fun addUserToDatabase(userId: String, model: UserModel, callback: (Boolean, String) -> Unit) {
        repo.addUserToDatabase(userId, model, callback)
    }

    fun updateProfile(userId: String, data: Map<String, Any?>, callback: (Boolean, String) -> Unit) {
        repo.updateProfile(userId, data, callback)
    }

    // Retrieve user
    private val _user = MutableLiveData<UserModel?>()
    val user: LiveData<UserModel?> get() = _user

    fun getUserById(userId: String) {
        repo.getUserById(userId) { success, _, fetchedUser ->
            if (success) _user.postValue(fetchedUser)
            else _user.postValue(null)
        }
    }
}
