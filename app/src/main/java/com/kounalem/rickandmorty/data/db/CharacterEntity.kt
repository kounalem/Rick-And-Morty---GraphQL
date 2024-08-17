package com.kounalem.rickandmorty.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "character")
internal data class CharacterEntity(
    @PrimaryKey
    val name: String,
    val page: Int,
)
