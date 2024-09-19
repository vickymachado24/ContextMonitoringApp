package com.example.contextmonitoringapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HealthViewModelFactory(private val healthDao: HealthDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthViewModel::class.java)) {
            return HealthViewModel(healthDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}