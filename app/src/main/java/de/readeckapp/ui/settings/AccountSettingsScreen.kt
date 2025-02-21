package de.readeckapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    navHostController: NavHostController
) {
    val viewModel: AccountSettingsViewModel = hiltViewModel()
    val settingsUiState = viewModel.uiState.collectAsState().value

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
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
                value = settingsUiState.url,
                placeholder = { Text("Readeck URL")},
                onValueChange = { viewModel.onUrlChanged(it) },
                label = { Text("Backend URL") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = settingsUiState.username,
                placeholder = { Text("Username")},
                onValueChange = { viewModel.onUsernameChanged(it) },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = settingsUiState.password,
                placeholder = { Text("Password")},
                onValueChange = { viewModel.onPasswordChanged(it) },
                label = { Text("Password") },
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    viewModel.saveSettings()
                }
            ) {
                Text("Save Settings")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountSettingsScreenPreview() {
    val navHostController = rememberNavController()
    AccountSettingsScreen(navHostController)
}
