package com.kounalem.rickandmorty.data

import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.ApolloClient
import com.kounalem.CharactersQuery
import com.kounalem.rickandmorty.data.db.LocalDataSource
import com.kounalem.rickandmorty.domain.RickAndMortyRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RickAndMortyClientImp(
    private val apolloClient: ApolloClient,
    private val local: LocalDataSource
) : RickAndMortyRepo {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO).also {
        it.launch {
            awaitCancellation()
        }
    }

    override val characters: Flow<List<String>>
        get() =
            combine(_characters, errors) { result, e ->
                if (result.isEmpty() && !e.isNullOrEmpty()) {
                    throw (Throwable(e))
                } else {
                    result
                }
            }

    private val pageFlow: MutableSharedFlow<Int> = MutableSharedFlow(replay = 1)
    private val errors: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _characters = MutableStateFlow(mutableListOf<String>())

    init {
        pageFlow.flatMapLatest { pageNo ->
            local.getCharacters(pageNo = pageNo).distinctUntilChanged().map { chars ->
                if (chars.isNotEmpty()) {
                    _characters.value = chars.toMutableList()
                } else {
                    getNextChars(pageNo = pageNo)
                }
            }
        }.launchIn(coroutineScope)
    }

    private suspend fun getNextChars(pageNo: Int) {

        coroutineScope.launch {
            val chars = apolloClient.query(CharactersQuery(pageNo))
                .execute()
                .data
                ?.characters?.results?.mapNotNull {
                    it?.name
                } ?: emptyList()
            if (chars.isNotEmpty()) {
                local.saveCharacterList(chars, pageNo)
                errors.value = null
            } else {
                errors.value = "Could not retrieve chars"
            }
        }
    }

    override suspend fun getCharacters(pageNo: Int) {
        pageFlow.tryEmit(pageNo)
    }


    override suspend fun clearLocalInfo() = local.clearAllChars()
}