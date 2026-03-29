package com.example.praktica3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.praktica3.data.Network
import com.example.praktica3.data.VideoResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Состояния для экрана видео
sealed class VideoState {
    object Loading : VideoState()
    data class Success(val videos: List<VideoResponse>) : VideoState()
    data class Error(val message: String) : VideoState()
}

class PlayerViewModel : ViewModel() {
    // Твой текущий список игроков (для вкладки "Список")
    private val _players = MutableStateFlow(mockPlayers)
    val players = _players.asStateFlow()

    // Состояние для вкладки "Видео"
    private val _videoState = MutableStateFlow<VideoState>(VideoState.Loading)
    val videoState = _videoState.asStateFlow()

    fun getPlayerById(id: Int) = mockPlayers.find { it.id == id }

    // Функция для загрузки видео из сети
    fun loadVideos() {
        viewModelScope.launch {
            _videoState.value = VideoState.Loading
            try {
                // Делаем настраиваемые запросы по ID (требование практики)
                val v1 = Network.api.getVideo(1)
                val v2 = Network.api.getVideo(2)
                val v3 = Network.api.getVideo(3)
                _videoState.value = VideoState.Success(listOf(v1, v2, v3))
            } catch (e: Exception) {
                _videoState.value = VideoState.Error("Не удалось загрузить обзоры. Проверьте интернет.")
            }
        }
    }
}