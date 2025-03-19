package de.readeckapp.ui.settings

import de.readeckapp.io.prefs.SettingsDataStore
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settingsDataStore = mockk()
        coEvery { settingsDataStore.usernameFlow } returns MutableStateFlow("testUser")
        viewModel = SettingsViewModel(settingsDataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load username from data store`() = runTest {
        val list = viewModel.uiState.take(2).toList()
        assertNull(list[0].username)
        assertEquals("testUser", list[1].username)
    }

    @Test
    fun `onClickAccount should navigate to account settings`() = runTest {
        viewModel.onClickAccount()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(SettingsViewModel.NavigationEvent.NavigateToAccountSettings, viewModel.navigationEvent.value)
    }

    @Test
    fun `onClickOpenSourceLibraries should navigate to open source libraries screen`() = runTest {
        viewModel.onClickOpenSourceLibraries()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(SettingsViewModel.NavigationEvent.NavigateToOpenSourceLibraries, viewModel.navigationEvent.value)
    }

    @Test
    fun `onClickBack should navigate back`() = runTest {
        viewModel.onClickBack()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(SettingsViewModel.NavigationEvent.NavigateBack, viewModel.navigationEvent.value)
    }

    @Test
    fun `onNavigationEventConsumed should reset navigation event`() = runTest {
        viewModel.onClickAccount()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onNavigationEventConsumed()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(null, viewModel.navigationEvent.value)
    }
}
