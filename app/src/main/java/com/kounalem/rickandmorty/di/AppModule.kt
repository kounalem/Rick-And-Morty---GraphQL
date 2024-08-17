package com.kounalem.rickandmorty.di

import android.app.Application
import androidx.room.Room
import com.apollographql.apollo3.ApolloClient
import com.kounalem.rickandmorty.data.RickAndMortyClientImp
import com.kounalem.rickandmorty.data.db.AppDatabase
import com.kounalem.rickandmorty.data.db.CharactersDao
import com.kounalem.rickandmorty.data.db.LocalDataSource
import com.kounalem.rickandmorty.data.db.LocalDataSourceImpl
import com.kounalem.rickandmorty.domain.RickAndMortyRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApolloClient(): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl("https://rickandmortyapi.com/graphql")
            .build()
    }

    @Provides
    @Singleton
    internal fun provideLocalDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            AppDatabase.NAME,
        ).build()
    }

    @Provides
    @Singleton
    internal fun provideCharactersDao(localDatabase: AppDatabase): CharactersDao {
        return localDatabase.dao
    }

    @Provides
    internal fun provideLocalDataSource(
        dao: CharactersDao,
    ): LocalDataSource = LocalDataSourceImpl(dao)

    @Provides
    fun provideApolloRickAndMortyClient(apolloClient: ApolloClient, localDataSource: LocalDataSource): RickAndMortyRepo {
        return RickAndMortyClientImp(apolloClient, localDataSource)
    }

}