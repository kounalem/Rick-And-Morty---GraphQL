package com.kounalem.rickandmorty.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface CharactersDao {
    @Query("SELECT * FROM character WHERE page = :pageNo")
    fun getCharactersForPage(pageNo: Int): Flow<List<CharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCharacter(character: CharacterEntity)

    @Query("DELETE FROM character")
    suspend fun deleteAll()
}
