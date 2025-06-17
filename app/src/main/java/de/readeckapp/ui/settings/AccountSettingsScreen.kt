package de.readeckapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
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
    val onLogoutClicked: () -> Unit = { viewModel.logout() }
    val onClickBack: () -> Unit = { viewModel.onClickBack() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
        onLogoutClicked = onLogoutClicked,
        onClickBack = onClickBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsView(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    settingsUiState: AccountSettingsUiState,
    onLogoutClicked: () -> Unit,
    onClickBack: () -> Unit,
) {
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onLogoutClicked() },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = "Logout"
                    )
                },
                text = {
                    Text(stringResource(R.string.account_settings_logout))
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ){
                Icon(
                    imageVector = Icons.Outlined.Cloud,
                    contentDescription = "URL"
                )
                Column(){
                    Text(
                        text = stringResource(R.string.account_settings_url_label),
                        style = MaterialTheme.typography.titleMedium,)
                    Text(
                        text = settingsUiState.url ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if(!settingsUiState.useApiToken) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ){
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Username"
                    )
                    Column(){
                        Text(
                            text = stringResource(R.string.account_settings_username_label),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = settingsUiState.username ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ){
                Icon(
                    imageVector = Icons.Outlined.Password,
                    contentDescription = "Password"
                )
                Column(){
                    Text(
                        text = stringResource(
                            when(settingsUiState.useApiToken) {
                                true -> R.string.account_settings_apitoken_label
                                false -> R.string.account_settings_username_label
                            }
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = (settingsUiState.password ?: "").let {
                            when(settingsUiState.useApiToken) {
                                true -> it
                                false -> it.replace(Regex("."), "\u2022")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.account_settings_use_apitoken),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                )
                Switch(
                    checked = settingsUiState.useApiToken,
                    onCheckedChange = null,
                    enabled = false
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.account_settings_allow_unencrypted),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                )
                Switch(
                    checked = settingsUiState.allowUnencryptedConnection,
                    onCheckedChange = null,
                    enabled = false
                )
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
        passwordError = null,
        authenticationResult = null
    )
    AccountSettingsView(
        modifier = Modifier,
        snackbarHostState = SnackbarHostState(),
        settingsUiState = settingsUiState,
        onLogoutClicked = {},
        onClickBack = {},
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
