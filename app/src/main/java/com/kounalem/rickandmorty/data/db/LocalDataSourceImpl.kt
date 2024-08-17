package com.kounalem.rickandmorty.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

internal class LocalDataSourceImpl(
    private val dao: CharactersDao,
) : LocalDataSource {

    override fun getCharacters(pageNo: Int): Flow<List<String>> {
        return dao.getCharactersForPage(pageNo).mapNotNull {
            it.mapNotNull {
                it.name
            }
        }
    }

    override suspend fun saveCharacterList(characters: List<String>, page: Int) {
        characters.map {
            dao.saveCharacter(
                CharacterEntity(it, page)
            )
        }
    }

    override suspend fun clearAllChars() = dao.deleteAll()
}
