package com.example.praktica3.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class FilterCache {
    private val _isDefaultState = MutableStateFlow(true)
    val isDefaultState = _isDefaultState.asStateFlow()

    fun updateStatus(filter: String) {
        _isDefaultState.value = (filter == "Все")
    }
}