package de.readeckapp.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.readeckapp.domain.usecase.AuthenticationResult
import de.readeckapp.ui.components.AdaptiveReadeckIcon
import timber.log.Timber

@Composable
fun LoginScreen(navHostController: NavHostController) {

    val horizontalSpacing = 16.dp

    val viewModel: LoginScreenViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsState().value

    val snackbarHostState = remember { SnackbarHostState() }

    /**
     * Define events
     */
    val onUrlChanged: (String) -> Unit = { viewModel.onUrlChanged(it) }
    val onUsernameChanged: (String) -> Unit = { viewModel.onUsernameChanged(it) }
    val onPasswordChanged: (String) -> Unit = { viewModel.onPasswordChanged(it) }
    val onToggleShowPassword: () -> Unit = { viewModel.onToggleShowPassword() }
    val onToggleUseApiToken: () -> Unit = { viewModel.onToggleUseApiToken() }
    val onToggleAllowUnencryptedConnection: () -> Unit = { viewModel.onToggleUseUnencryptedConnection() }
    val onClickLogin: () -> Unit = { viewModel.onClickLogin() }
    val onAuthenticationResultConsumed: () -> Unit = { viewModel.onAuthenticationResultConsumed() }

    val passwordVisual = if(uiState.showPassword) VisualTransformation.None else PasswordVisualTransformation()
    val urlPrefix = if(uiState.useUnencryptedConnection) "http://" else "https://"
    val authTokenText = if(uiState.useApiToken) "API-Token *" else "Password *"

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
                text = { Text("Login") },
                icon = { Icon(Icons.AutoMirrored.Default.Login, "Login") },
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
                text = "Welcome to ReadeckApp",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = "Please log in to your Readeck instance.",
                style = MaterialTheme.typography.bodyLarge,
            )

            Column(){

                TextField(
                    value = uiState.url,
                    onValueChange = { onUrlChanged(it) },
                    label = { Text("Instance URL *") },
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
                    modifier = Modifier.fillMaxWidth()
                )

                if(!uiState.useApiToken){
                    TextField(
                        value = uiState.username,
                        onValueChange = { onUsernameChanged(it) },
                        label = { Text("Username *") },
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
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TextField(
                    value = uiState.password,
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
                    isError = uiState.passwordError != null,
                    supportingText = {
                        uiState.passwordError?.let {
                            Text(stringResource(it))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ){
                Text(
                    text = "Use API-token",
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
                    text = "Use unencrypted connection",
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
                        text = "Unencrypted connections via HTTP are not recommended. Your credentials may be exposed!",
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
    LoginScreen(navHostController = NavHostController(LocalContext.current))
}