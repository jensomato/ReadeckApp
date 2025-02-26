package de.readeckapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import de.readeckapp.R

@Composable
fun AccountSettingsScreen(
    navHostController: NavHostController
) {
    val viewModel: AccountSettingsViewModel = hiltViewModel()
    val settingsUiState = viewModel.uiState.collectAsState().value
    val navigationEvent = viewModel.navigationEvent.collectAsState()
    val onUrlChanged: (String) -> Unit = { url -> viewModel.onUrlChanged(url)}
    val onUsernameChanged: (String) -> Unit = { username -> viewModel.onUsernameChanged(username)}
    val onPasswordChanged: (String) -> Unit = { password -> viewModel.onPasswordChanged(password)}
    val onLoginClicked: () -> Unit = { viewModel.login() }
    val onClickBack: () -> Unit = { viewModel.onClickBack() }

    LaunchedEffect(key1 = navigationEvent.value) {
        navigationEvent.value?.let { event ->
            when (event) {
                is AccountSettingsViewModel.NavigationEvent.NavigateBack -> {
                    navHostController.popBackStack()
                }
            }
            viewModel.onNavigationEventConsumed() // Consume the event
        }
    }
    AccountSettingsView(
        modifier = Modifier,
        settingsUiState = settingsUiState,
        onUrlChanged = onUrlChanged,
        onUsernameChanged = onUsernameChanged,
        onPasswordChanged = onPasswordChanged,
        onLoginClicked = onLoginClicked,
        onClickBack = onClickBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsView(
    modifier: Modifier = Modifier,
    settingsUiState: AccountSettingsUiState,
    onUrlChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClicked: () -> Unit,
    onClickBack: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.accountsettings_topbar_title)) },
                        navigationIcon = {
                    IconButton(
                        onClick = onClickBack,
                        modifier = Modifier.testTag(AccountSettingsScreenTestTags.BACK_BUTTON)
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = settingsUiState.url ?: "",
                placeholder = { Text(stringResource(R.string.account_settings_url_placeholder))},
                onValueChange = { onUrlChanged(it) },
                label = { Text(stringResource(R.string.account_settings_url_label)) },
                modifier = Modifier.fillMaxWidth(),
                isError = settingsUiState.urlError != null,
                supportingText = {
                    settingsUiState.urlError?.let {
                        Text(text = stringResource(it))
                    }
                }
            )
            OutlinedTextField(
                value = settingsUiState.username ?: "",
                placeholder = { Text(stringResource(R.string.account_settings_username_placeholder))},
                onValueChange = { onUsernameChanged(it) },
                label = { Text(stringResource(R.string.account_settings_username_label)) },
                modifier = Modifier.fillMaxWidth(),
                isError = settingsUiState.usernameError != null,
                supportingText = {
                    settingsUiState.usernameError?.let {
                        Text(text = stringResource(it))
                    }
                }
            )
            OutlinedTextField(
                value = settingsUiState.password ?: "",
                placeholder = { Text(stringResource(R.string.account_settings_password_placeholder))},
                onValueChange = { onPasswordChanged(it) },
                label = { Text(stringResource(R.string.account_settings_password_label)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                isError = settingsUiState.passwordError != null,
                supportingText = {
                    settingsUiState.passwordError?.let {
                        Text(text = stringResource(it))
                    }
                }
            )
            Button(
                onClick = onLoginClicked,
                enabled = settingsUiState.loginEnabled
            ) {
                Text(stringResource(R.string.account_settings_login))
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun AccountSettingsScreenViewPreview() {
    val settingsUiState = AccountSettingsUiState(
        url = "https://example.com",
        username = "user",
        password = "pass",
        loginEnabled = true,
        urlError = R.string.account_settings_url_error,
        usernameError = null,
        passwordError = null
    )
    AccountSettingsView(
        modifier = Modifier,
        settingsUiState = settingsUiState,
        onUrlChanged = {},
        onUsernameChanged = {},
        onPasswordChanged = {},
        onLoginClicked = {},
        onClickBack = {}
    )
}

object AccountSettingsScreenTestTags {
    const val BACK_BUTTON = "AccountSettingsScreenTestTags.BackButton"
    const val TOPBAR = "AccountSettingsScreenTestTags.TopBar"
    const val SETTINGS_ITEM = "AccountSettingsScreenTestTags.SettingsItem"
    const val SETTINGS_ITEM_TITLE = "AccountSettingsScreenTestTags.SettingsItem.Title"
    const val SETTINGS_ITEM_SUBTITLE = "AccountSettingsScreenTestTags.SettingsItem.Subtitle"
    const val SETTINGS_ITEM_ACCOUNT = "Account"
}
