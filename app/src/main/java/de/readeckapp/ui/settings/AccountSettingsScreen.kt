package de.readeckapp.ui.settings

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
    val context = LocalContext.current
    val activity = context as? Activity
    val onUrlChanged: (String) -> Unit = { url -> viewModel.onUrlChanged(url) }
    val onUsernameChanged: (String) -> Unit = { username -> viewModel.onUsernameChanged(username) }
    val onPasswordChanged: (String) -> Unit = { password -> viewModel.onPasswordChanged(password) }
    val onLoginClicked: () -> Unit = { viewModel.login() }
    val onAllowUnencryptedConnectionChanged: (Boolean) -> Unit = { allow -> viewModel.onAllowUnencryptedConnectionChanged(allow) }
    val onClickBack: () -> Unit = { viewModel.onClickBack() }
    val onSelectCertificate: () -> Unit = { activity?.let { viewModel.onSelectCertificate(it) } }
    val onClearCertificate: () -> Unit = { viewModel.onClearCertificate() }
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
        onUrlChanged = onUrlChanged,
        onUsernameChanged = onUsernameChanged,
        onPasswordChanged = onPasswordChanged,
        onLoginClicked = onLoginClicked,
        onClickBack = onClickBack,
        onAllowUnencryptedConnectionChanged = onAllowUnencryptedConnectionChanged,
        onSelectCertificate = onSelectCertificate,
        onClearCertificate = onClearCertificate
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsView(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    settingsUiState: AccountSettingsUiState,
    onUrlChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClicked: () -> Unit,
    onClickBack: () -> Unit,
    onAllowUnencryptedConnectionChanged: (Boolean) -> Unit,
    onSelectCertificate: () -> Unit,
    onClearCertificate: () -> Unit
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
                supportingText = {
                    settingsUiState.urlError?.let {
                        Text(text = stringResource(it))
                    }
                }
            )
            OutlinedTextField(
                value = settingsUiState.username ?: "",
                placeholder = { Text(stringResource(R.string.account_settings_username_placeholder)) },
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
                placeholder = { Text(stringResource(R.string.account_settings_password_placeholder)) },
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
                    onCheckedChange = null
                )
                Text(text = stringResource(R.string.account_settings_allow_unencrypted))
            }
            
            // Client Certificate Section
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "Client Certificate (mTLS)",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            if (settingsUiState.clientCertificateAlias != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Certificate Selected",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = settingsUiState.clientCertificateAlias,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        TextButton(onClick = onClearCertificate) {
                            Text("Clear")
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onSelectCertificate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Client Certificate")
                }
                Text(
                    text = "Optional: Select a client certificate for mTLS authentication",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    keyboardController?.hide()
                    onLoginClicked.invoke()
                },
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
        passwordError = null,
        authenticationResult = null
    )
    AccountSettingsView(
        modifier = Modifier,
        snackbarHostState = SnackbarHostState(),
        settingsUiState = settingsUiState,
        onUrlChanged = {},
        onUsernameChanged = {},
        onPasswordChanged = {},
        onLoginClicked = {},
        onClickBack = {},
        onAllowUnencryptedConnectionChanged = {},
        onSelectCertificate = {},
        onClearCertificate = {}
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
