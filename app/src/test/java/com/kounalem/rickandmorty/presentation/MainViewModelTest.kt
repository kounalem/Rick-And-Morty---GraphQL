package com.kounalem.rickandmorty.presentation

import app.cash.turbine.test
import com.kounalem.rickandmorty.CoroutineTestRule
import com.kounalem.rickandmorty.domain.RickAndMortyRepo
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import kotlin.test.Test

class MainViewModelTest {
    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var repo: RickAndMortyRepo

    private val viewModel by lazy {
        MainViewModel(repo)
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun `GIVEN chars THEN update the state with mapped info`() =
        runTest {
            coEvery { repo.characters } returns
                    flowOf(
                        listOf("Rick", "Morty"),
                    )

            viewModel.uiState.test {
                kotlin.test.assertEquals(
                    actual = awaitItem(),
                    expected =
                    MainViewModel.State.Success(
                        characters = listOf("Rick", "Morty"),
                        isRefreshing = false,
                        endReached = false,
                        fetchingNewChars = false
                    ),
                )
            }
        }

}