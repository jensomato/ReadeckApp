package de.readeckapp.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import de.readeckapp.R
import de.readeckapp.domain.model.AutoSyncTimeframe
import de.readeckapp.domain.model.Theme
import de.readeckapp.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun UiSettingsScreen(
    navHostController: NavHostController
) {
    val viewModel: UiSettingsViewModel = hiltViewModel()
    val settingsUiState = viewModel.uiState.collectAsState().value
    val navigationEvent = viewModel.navigationEvent.collectAsState()
    val onClickBack: () -> Unit = { viewModel.onClickBack() }
    val onClickTheme: () -> Unit = { viewModel.onClickTheme() }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = navigationEvent.value) {
        navigationEvent.value?.let { event ->
            when (event) {
                is UiSettingsViewModel.NavigationEvent.NavigateBack -> {
                    navHostController.popBackStack()
                }
            }
            viewModel.onNavigationEventConsumed() // Consume the event
        }
    }

    if (settingsUiState.showDialog) {
        ThemeDialog(
            themeOptions = settingsUiState.themeOptions,
            onDismissRequest = { viewModel.onDismissDialog() },
            onElementSelected = { viewModel.onThemeSelected(it) }
        )
    }

    UiSettingsView(
        modifier = Modifier,
        snackbarHostState = snackbarHostState,
        onClickBack = onClickBack,
        onClickTheme = onClickTheme,
        settingsUiState = settingsUiState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UiSettingsView(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    settingsUiState: UiSettingsUiState,
    onClickTheme: () -> Unit,
    onClickBack: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ui_settings_topbar_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onClickBack,
                        modifier = Modifier.testTag(UiSettingsScreenTestTags.BACK_BUTTON)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(enabled = true, onClick = onClickTheme)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.ui_settings_theme_title))
                    Text(
                        text = stringResource(settingsUiState.themeLabel),
                        style = Typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UiSettingsScreenViewPreview() {
    val settingsUiState = UiSettingsUiState(
        theme = Theme.SYSTEM,
        themeOptions = listOf(),
        showDialog = false,
        themeLabel = Theme.SYSTEM.toLabelResource(),
    )
    UiSettingsView(
        modifier = Modifier,
        snackbarHostState = SnackbarHostState(),
        onClickBack = {},
        onClickTheme = {},
        settingsUiState = settingsUiState
    )
}

object UiSettingsScreenTestTags {
    const val BACK_BUTTON = "AccountSettingsScreenTestTags.BackButton"
}
