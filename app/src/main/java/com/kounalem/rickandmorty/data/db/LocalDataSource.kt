package com.kounalem.rickandmorty.data.db

import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    fun getCharacters(pageNo: Int): Flow<List<String>>
    suspend fun saveCharacterList(characters: List<String>, page: Int)
    suspend fun clearAllChars()
}
