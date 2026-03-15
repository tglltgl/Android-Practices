package com.example.praktica3

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerViewModel : ViewModel() {
    private val _players = MutableStateFlow(mockPlayers)
    val players = _players.asStateFlow()

    fun getPlayerById(id: Int) = mockPlayers.find { it.id == id }
}