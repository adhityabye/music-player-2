package by.tutorials.musicapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.tutorials.musicapp.data.model.Song
import by.tutorials.musicapp.data.repository.SongRepository
import kotlinx.coroutines.launch

class SongViewModel : ViewModel() {

    private val repository = SongRepository()

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> get() = _songs

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    companion object {
        private const val INITIAL_SEARCH_TERM = "Neck Deep"
        private const val TAG = "SongViewModel"
    }

    init {
        searchSongs(INITIAL_SEARCH_TERM, isInitialLoad = true)
    }

    fun searchSongs(searchTerm: String, isInitialLoad: Boolean = false) {
        if (searchTerm.isBlank()) {
            _errorMessage.value = "Search can't empty"
            _songs.value = emptyList()
            return
        }

        _isLoading.value = true
        if (!isInitialLoad) {
            _errorMessage.value = null
        }

        viewModelScope.launch {
            try {
                val result = repository.searchSongs(searchTerm)
                _isLoading.value = false
                result.fold(
                    onSuccess = { songList ->
                        Log.d(TAG, "Songs fetched: ${songList.size}")
                        _songs.value = songList
                        if (songList.isEmpty()) {
                            _errorMessage.value = "No songs found for '$searchTerm'."
                        } else {
                            _errorMessage.value = null
                        }
                    },
                    onFailure = { exception ->
                        _songs.value = emptyList()
                        _errorMessage.value = "Error fetching songs: ${exception.message ?: "unknown error"}"
                    }
                )
            } catch (e: Exception) {
                _isLoading.value = false
                _songs.value = emptyList()
                _errorMessage.value = "An unexpected error: ${e.message}"
            }
        }
    }
}
