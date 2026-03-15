package de.readeckapp.ui.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import de.readeckapp.R
import de.readeckapp.domain.usecase.AuthenticationResult

@Composable
fun AccountSettingsScreen(
    navHostController: NavHostController
) {
    val viewModel: AccountSettingsViewModel = hiltViewModel()
    val settingsUiState = viewModel.uiState.collectAsState().value
    val navigationEvent = viewModel.navigationEvent.collectAsState()
    val oAuthIntentEvent = viewModel.oAuthIntentEvent.collectAsState()
    val onUrlChanged: (String) -> Unit = { url -> viewModel.onUrlChanged(url) }
    val onLoginClicked: () -> Unit = { viewModel.login() }
    val onAllowUnencryptedConnectionChanged: (Boolean) -> Unit = { allow -> viewModel.onAllowUnencryptedConnectionChanged(allow) }
    val onClickBack: () -> Unit = { viewModel.onClickBack() }
    val snackbarHostState = remember { SnackbarHostState() }

    val oauthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK || result.resultCode == Activity.RESULT_CANCELED) {
            viewModel.handleOAuthResult(result.data)
        }
    }

    LaunchedEffect(key1 = navigationEvent.value) {
        navigationEvent.value?.let { event ->
            when (event) {
                is AccountSettingsViewModel.NavigationEvent.NavigateBack -> {
                    navHostController.popBackStack()
                }
            }
            viewModel.onNavigationEventConsumed()
        }
    }

    LaunchedEffect(key1 = oAuthIntentEvent.value) {
        oAuthIntentEvent.value?.let { intent ->
            oauthLauncher.launch(intent)
            viewModel.onOAuthIntentConsumed()
        }
    }

    LaunchedEffect(key1 = settingsUiState.authenticationResult) {
        settingsUiState.authenticationResult?.let { result ->
            when (result) {
                is AuthenticationResult.Success -> {
                    snackbarHostState.showSnackbar(
                        message = "Success",
                        duration = SnackbarDuration.Short
                    )
                }
                is AuthenticationResult.AuthenticationFailed -> {
                    snackbarHostState.showSnackbar(
                        message = result.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is AuthenticationResult.NetworkError -> {
                    snackbarHostState.showSnackbar(
                        message = result.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is AuthenticationResult.GenericError -> {
                    snackbarHostState.showSnackbar(
                        message = result.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    AccountSettingsView(
        modifier = Modifier,
        snackbarHostState = snackbarHostState,
        settingsUiState = settingsUiState,
        onUrlChanged = onUrlChanged,
        onLoginClicked = onLoginClicked,
        onClickBack = onClickBack,
        onAllowUnencryptedConnectionChanged = onAllowUnencryptedConnectionChanged
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsView(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    settingsUiState: AccountSettingsUiState,
    onUrlChanged: (String) -> Unit,
    onLoginClicked: () -> Unit,
    onClickBack: () -> Unit,
    onAllowUnencryptedConnectionChanged: (Boolean) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                placeholder = { Text(stringResource(R.string.account_settings_url_placeholder)) },
                onValueChange = { onUrlChanged(it) },
                label = { Text(stringResource(R.string.account_settings_url_label)) },
                modifier = Modifier.fillMaxWidth(),
                isError = settingsUiState.urlError != null,
                enabled = settingsUiState.isLoading.not(),
                supportingText = {
                    settingsUiState.urlError?.let {
                        Text(text = stringResource(it))
                    }
                }
            )
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .selectable(
                        selected = settingsUiState.allowUnencryptedConnection,
                        onClick = { onAllowUnencryptedConnectionChanged(settingsUiState.allowUnencryptedConnection.not()) },
                        role = Role.Checkbox
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Checkbox(
                    checked = settingsUiState.allowUnencryptedConnection,
                    enabled = settingsUiState.isLoading.not(),
                    onCheckedChange = null
                )
                Text(text = stringResource(R.string.account_settings_allow_unencrypted))
            }
            Button(
                onClick = {
                    keyboardController?.hide()
                    onLoginClicked.invoke()
                },
                enabled = settingsUiState.loginEnabled && settingsUiState.isLoading.not()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (settingsUiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    }
                    Text(stringResource(R.string.account_settings_login))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountSettingsScreenViewPreview() {
    val settingsUiState = AccountSettingsUiState(
        url = "https://example.com",
        loginEnabled = true,
        urlError = null,
        authenticationResult = null
    )
    AccountSettingsView(
        modifier = Modifier,
        snackbarHostState = SnackbarHostState(),
        settingsUiState = settingsUiState,
        onUrlChanged = {},
        onLoginClicked = {},
        onClickBack = {},
        onAllowUnencryptedConnectionChanged = {}
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
