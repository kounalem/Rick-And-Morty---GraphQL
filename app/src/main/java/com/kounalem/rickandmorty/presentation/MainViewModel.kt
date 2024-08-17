package com.kounalem.rickandmorty.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kounalem.rickandmorty.domain.RickAndMortyRepo
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(private val repo: RickAndMortyRepo) :
    ViewModel() {
    private var endReached: Boolean = false
    private val isRefreshing = MutableStateFlow(false)
    private val fetchingNewChars: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isLoading = MutableStateFlow(false)
    private val error: MutableStateFlow<String?> = MutableStateFlow(null)
    private val characters: MutableStateFlow<MutableSet<String>> = MutableStateFlow(mutableSetOf())

    private val paginator: Paginator<Int> =
        Paginator(initialKey = 1, onRequest = { nextPage ->
            fetchingNewChars.value = true
            repo.getCharacters(nextPage)
        }, getNextKey = { currentKey ->
            currentKey + 1
        })

    @OptIn(ExperimentalCoroutinesApi::class)
    private val results: StateFlow<List<String>>
        get() = repo.characters.onEach {
            isRefreshing.value = true
            isLoading.value = true
        }.flatMapLatest {
            flowOf(it)
        }.catch {
            error.value = it.message
        }.onEach {
            isLoading.value = false
            error.value = null
            isRefreshing.value = false
            fetchingNewChars.value = false
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun loadNextItems(refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) {
                repo.clearLocalInfo()
            }
            paginator.loadNextItems()
        }
    }

    fun refreshElements() {
        paginator.reset()
        loadNextItems(refresh = true)
    }

    val uiState: StateFlow<State>
        get() =
            combineTuple(
                results,
                isLoading,
                error,
                isRefreshing,
                fetchingNewChars,
            ).map { (_results, _isLoading, _error, _isRefreshing, _fetchingNewChars) ->
                characters.value.addAll(_results.toMutableSet())
                if (_error != null) {
                    State.Error(_error)
                } else if (_isLoading) {
                    if (_results.toList().isEmpty()
                    ) {
                        State.Loading
                    } else {
                        State.Success(
                            characters =  characters.value.toList(),
                            isRefreshing = _isRefreshing,
                            endReached = endReached,
                            fetchingNewChars = true,
                        )
                    }
                } else {
                    State.Success(
                        characters =  characters.value.toList(),
                        isRefreshing = _isRefreshing,
                        endReached = endReached,
                        fetchingNewChars = _fetchingNewChars,
                    )
                }
            }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5_000),
                    State.Loading,
                )


    sealed interface State {
        data object Loading : State

        @JvmInline
        value class Error(val message: String) : State

        data class Success(
            val characters: List<String>,
            val isRefreshing: Boolean,
            val endReached: Boolean,
            val fetchingNewChars: Boolean,
        ) : State
    }

}
