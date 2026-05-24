package com.example.praktica3

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.praktica3.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class VideoState {
    object Loading : VideoState()
    data class Success(val videos: List<VideoItem>) : VideoState()
    data class Error(val message: String) : VideoState()
}

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val filterManager = FilterManager(application)
    private val dao = AppDatabase.getDatabase(application).favoriteDao()
    private val filterCache = FilterCache()


    val savedFilter: StateFlow<String> = filterManager.selectedFilter
        .onEach { filter ->

            filterCache.updateStatus(filter)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Все"
        )


    val onlyVeterans: StateFlow<Boolean> = filterManager.onlyVeterans
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )


    val showBadge: StateFlow<Boolean> = filterCache.isDefaultState
        .map { isDefault -> !isDefault }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _allPlayers = MutableStateFlow(mockPlayers)


    val filteredPlayers: StateFlow<List<Player>> = combine(
        _allPlayers,
        savedFilter,
        onlyVeterans
    ) { players, positionFilter, isVetOnly ->
        players.filter { player ->
            val matchesPosition = if (positionFilter == "Все") true else player.position == positionFilter
            val matchesAge = if (isVetOnly) player.age >= 30 else true

            matchesPosition && matchesAge
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), mockPlayers)



    val favoritePlayers: StateFlow<List<FavoritePlayer>> = dao.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun isPlayerFavorite(playerId: Int): Flow<Boolean> = dao.isFavorite(playerId)

    fun toggleFavorite(player: Player, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            val favorite = FavoritePlayer(
                id = player.id,
                name = player.name,
                position = player.position,
                photoRes = player.photoRes,
                number = player.number
            )
            if (isCurrentlyFavorite) {
                dao.removeFavorite(favorite)
            } else {
                dao.addFavorite(favorite)
            }
        }
    }


    fun updateFilter(newFilter: String) {
        viewModelScope.launch {
            filterManager.saveFilter(newFilter)
        }
    }


    fun updateVeteransFilter(value: Boolean) {
        viewModelScope.launch {
            filterManager.saveVeteransFilter(value)
        }
    }


    private val _videoState = MutableStateFlow<VideoState>(VideoState.Loading)
    val videoState = _videoState.asStateFlow()

    fun getPlayerById(id: Int) = mockPlayers.find { it.id == id }

    fun loadVideos() {
        viewModelScope.launch {
            _videoState.value = VideoState.Loading
            try {
                val response = Network.api.getLatestVideos()
                _videoState.value = VideoState.Success(response.tvshows ?: emptyList())
            } catch (e: Exception) {
                _videoState.value = VideoState.Error("Не удалось загрузить обзоры.")
            }
        }
    }
}