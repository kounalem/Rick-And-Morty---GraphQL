package com.kounalem.rickandmorty.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        CharacterEntity::class,
    ],
    version = 1,
)
internal abstract class AppDatabase : RoomDatabase() {
    abstract val dao: CharactersDao

    companion object {
        const val NAME = "rickAndMorty.db"
    }
}
