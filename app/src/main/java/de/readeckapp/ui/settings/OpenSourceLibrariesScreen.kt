package de.readeckapp.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import de.readeckapp.R

@Composable
fun OpenSourceLibrariesScreen(navHostController: NavHostController) {
    val viewModel: OpenSourceLibrariesViewModel = hiltViewModel()
    val navigationEvent = viewModel.navigationEvent.collectAsState()

    LaunchedEffect(key1 = navigationEvent.value) {
        navigationEvent.value?.let { event ->
            when (event) {
                OpenSourceLibrariesViewModel.NavigationEvent.NavigateBack -> {
                    navHostController.popBackStack()
                }
            }
            viewModel.onNavigationEventConsumed()
        }
    }

    OpenSourceLibrariesScreenContent(onBackClick = { viewModel.onClickBack() })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLibrariesScreenContent(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_open_source_libraries)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LibrariesContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}
