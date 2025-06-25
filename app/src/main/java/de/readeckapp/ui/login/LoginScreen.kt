package de.readeckapp.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.readeckapp.R
import de.readeckapp.domain.usecase.AuthenticationResult
import de.readeckapp.ui.components.AdaptiveReadeckIcon

@Composable
fun LoginScreen() {

    val horizontalSpacing = 16.dp

    val viewModel: LoginScreenViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsState().value

    val snackbarHostState = remember { SnackbarHostState() }

    /**
     * Define events
     */
    val onUrlChanged: (String) -> Unit = { viewModel.onUrlChanged(it) }
    val onUsernameChanged: (String) -> Unit = { viewModel.onUsernameChanged(it) }
    val onPasswordChanged: (String) -> Unit = { viewModel.onPasswordOrApiTokenChanged(it) }
    val onToggleShowPassword: () -> Unit = { viewModel.onToggleShowPasswordOrApiToken() }
    val onToggleUseApiToken: () -> Unit = { viewModel.onToggleUseApiToken() }
    val onToggleAllowUnencryptedConnection: () -> Unit = { viewModel.onToggleUseUnencryptedConnection() }
    val onClickLogin: () -> Unit = { viewModel.onClickLogin() }
    val onAuthenticationResultConsumed: () -> Unit = { viewModel.onAuthenticationResultConsumed() }

    val passwordVisual = if(uiState.showPassword) VisualTransformation.None else PasswordVisualTransformation()
    val urlPrefix = if(uiState.useUnencryptedConnection) "http://" else "https://"
    val authTokenText = if(uiState.useApiToken) stringResource(R.string.login_apitoken_label) else stringResource(R.string.login_password_label)

    LaunchedEffect(key1 = uiState.authenticationResult) {

        uiState.authenticationResult?.let { result ->
            if(result is AuthenticationResult.GenericError) {
                snackbarHostState.showSnackbar(
                    message = result.message,
                    duration = SnackbarDuration.Long,
                    withDismissAction = true
                )
                onAuthenticationResultConsumed()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onClickLogin() },
                icon = {
                    when(uiState.isLoading){
                        true -> CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        false -> Icon(Icons.AutoMirrored.Default.Login, null)
                    }
                },
                text = {
                    when(uiState.isLoading){
                        true -> {}
                        false -> Text(stringResource(R.string.login_button_label))
                    }
                },
                expanded = !uiState.isLoading
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(vertical = 32.dp, horizontal = 16.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            AdaptiveReadeckIcon(128.dp)

            Text(
                text = stringResource(R.string.login_welcome),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = stringResource(R.string.login_please_log_in),
                style = MaterialTheme.typography.bodyLarge,
            )

            Column {

                TextField(
                    value = uiState.url,
                    onValueChange = { onUrlChanged(it) },
                    label = { Text(stringResource(R.string.login_url_label)) },
                    prefix = { Text(urlPrefix) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Cloud,
                            contentDescription = null,
                        )
                    },
                    isError = uiState.urlError != null,
                    supportingText = {
                        uiState.urlError?.let {
                            Text(stringResource(it))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )

                if(!uiState.useApiToken){
                    TextField(
                        value = uiState.username,
                        onValueChange = { onUsernameChanged(it) },
                        label = { Text(stringResource(R.string.login_username_label)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                            )
                        },
                        isError = uiState.usernameError != null,
                        supportingText = {
                            uiState.usernameError?.let {
                                Text(stringResource(it))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentType = ContentType.Username }
                    )
                }

                TextField(
                    value = uiState.passwordOrApiToken,
                    onValueChange = { onPasswordChanged(it) },
                    label = { Text(authTokenText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = passwordVisual,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Password,
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = if(uiState.showPassword) {
                                Icons.Filled.Visibility
                            } else {
                                Icons.Outlined.VisibilityOff
                            },
                            contentDescription = null,
                            modifier = Modifier.clickable(
                                enabled = true,
                                onClick = { onToggleShowPassword() }
                            )
                        )
                    },
                    isError = uiState.passwordOrApiTokenError != null,
                    supportingText = {
                        uiState.passwordOrApiTokenError?.let {
                            Text(stringResource(it))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentType = ContentType.Password }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ){
                Text(
                    text = stringResource(R.string.login_use_apitoken_label),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = horizontalSpacing)
                )
                Switch(
                    checked = uiState.useApiToken,
                    onCheckedChange = { onToggleUseApiToken() }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.login_use_unencrypted_label),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = horizontalSpacing)
                )
                Switch(
                    checked = uiState.useUnencryptedConnection,
                    onCheckedChange = { onToggleAllowUnencryptedConnection() }
                )
            }

            if(uiState.useUnencryptedConnection) {
                Row(
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = stringResource(R.string.login_use_unencrypted_description),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun LoginScreenPreview() {
    LoginScreen()
}