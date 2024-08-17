package com.kounalem.rickandmorty.domain

import kotlinx.coroutines.flow.Flow

interface RickAndMortyRepo {
    val characters: Flow<List<String>>
    suspend fun getCharacters(pageNo:Int)
    suspend fun clearLocalInfo()
}