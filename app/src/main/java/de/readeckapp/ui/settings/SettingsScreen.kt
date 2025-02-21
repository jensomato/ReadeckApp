package de.readeckapp.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import de.readeckapp.R

@Composable
fun SettingsScreen(
    navHostController: NavHostController
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val settingsUiState = viewModel.uiState.collectAsState().value
    val navigationEvent = viewModel.navigationEvent.collectAsState()
    val onClickAccount: () -> Unit = { viewModel.onClickAccount() }
    val onClickBack: () -> Unit = { viewModel.onClickBack() }
    LaunchedEffect(key1 = navigationEvent.value) {
        navigationEvent.value?.let { event ->
            when (event) {
                is SettingsViewModel.NavigationEvent.NavigateToAccountSettings -> {
                    navHostController.navigate("accountSettings")
                }

                is SettingsViewModel.NavigationEvent.NavigateBack -> {
                    navHostController.popBackStack()
                }
            }
            viewModel.onNavigationEventConsumed() // Consume the event
        }
    }
    SettingScreenView(
        settingsUiState = settingsUiState,
        onClickAccount = onClickAccount,
        onClickBack = onClickBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreenView(
    settingsUiState: SettingsUiState,
    onClickAccount: () -> Unit,
    onClickBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.testTag(SettingsScreenTestTags.TOPBAR),
                title = { Text(stringResource(R.string.settings_topbar_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onClickBack,
                        modifier = Modifier.testTag(SettingsScreenTestTags.BACK_BUTTON)
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
        ) {
            SettingItem(
                icon = Icons.Filled.AccountCircle,
                title = stringResource(R.string.settings_account_title),
                subtitle = settingsUiState.username
                    ?: stringResource(R.string.settings_account_subtitle_default),
                onClick = onClickAccount,
                testTag = SettingsScreenTestTags.SETTINGS_ITEM_ACCOUNT
            )
        }
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
            .testTag("${SettingsScreenTestTags.SETTINGS_ITEM}.$testTag"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = title, modifier = Modifier.padding(end = 16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag("${SettingsScreenTestTags.SETTINGS_ITEM_TITLE}.$testTag")
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("${SettingsScreenTestTags.SETTINGS_ITEM_SUBTITLE}.$testTag")
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingScreenViewPreview() {
    SettingScreenView(
        settingsUiState = SettingsUiState(
            username = "test",
        ),
        onClickAccount = {},
        onClickBack = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SettingItemPreview() {
    SettingItem(
        icon = Icons.Filled.Lock,
        title = "test",
        subtitle = "test1",
        onClick = {},
        testTag = "account"
    )
}

object SettingsScreenTestTags {
    const val BACK_BUTTON = "SettingsScreenTestTags.BackButton"
    const val TOPBAR = "SettingsScreenTestTags.TopBar"
    const val SETTINGS_ITEM = "SettingsScreenTestTags.SettingsItem"
    const val SETTINGS_ITEM_TITLE = "SettingsScreenTestTags.SettingsItem.Title"
    const val SETTINGS_ITEM_SUBTITLE = "SettingsScreenTestTags.SettingsItem.Subtitle"
    const val SETTINGS_ITEM_ACCOUNT = "Account"
}