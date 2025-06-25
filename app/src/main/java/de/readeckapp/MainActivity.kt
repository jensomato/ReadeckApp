package de.readeckapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import de.readeckapp.domain.model.Theme
import de.readeckapp.ui.detail.BookmarkDetailScreen
import de.readeckapp.ui.list.BookmarkListScreen
import de.readeckapp.ui.login.LoginScreen
import de.readeckapp.ui.navigation.AccountSettingsRoute
import de.readeckapp.ui.navigation.BookmarkDetailRoute
import de.readeckapp.ui.navigation.BookmarkListRoute
import de.readeckapp.ui.navigation.LogViewRoute
import de.readeckapp.ui.navigation.OpenSourceLibrariesRoute
import de.readeckapp.ui.navigation.SettingsRoute
import de.readeckapp.ui.navigation.SyncSettingsRoute
import de.readeckapp.ui.navigation.UiSettingsRoute
import de.readeckapp.ui.navigation.LoginRoute
import de.readeckapp.ui.settings.AccountSettingsScreen
import de.readeckapp.ui.settings.LogViewScreen
import de.readeckapp.ui.settings.OpenSourceLibrariesScreen
import de.readeckapp.ui.settings.SettingsScreen
import de.readeckapp.ui.settings.SyncSettingsScreen
import de.readeckapp.ui.settings.UiSettingsScreen
import de.readeckapp.ui.theme.ReadeckAppTheme
import de.readeckapp.util.isValidUrl
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var intentState: MutableState<Intent?>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = hiltViewModel<MainViewModel>()
            val theme = viewModel.theme.collectAsState()
            val navController = rememberNavController()
            intentState = remember { mutableStateOf(intent) }
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            val noValidUrlMessage = stringResource(id = R.string.not_valid_url)

            val startDestination by viewModel.startDestination.collectAsState()
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()

            val darkTheme = when (theme.value) {
                Theme.LIGHT -> false
                Theme.DARK -> true
                Theme.SYSTEM -> isSystemInDarkTheme()
            }

            ReadeckAppTheme(darkTheme = darkTheme) {
                startDestination?.also {
                    LaunchedEffect(intentState.value) {
                        intentState.value?.let { newIntent ->
                            if (newIntent.action == Intent.ACTION_SEND && newIntent.type == "text/plain" && isLoggedIn) {
                                val sharedUrl = newIntent.getStringExtra(Intent.EXTRA_TEXT)
                                if (sharedUrl.isValidUrl()) {
                                    navController.navigate(BookmarkListRoute(sharedUrl = sharedUrl))
                                } else {
                                    scope.launch {
                                        Toast.makeText(context, noValidUrlMessage, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                            if (newIntent.hasExtra("navigateToAccountSettings")) {
                                Timber.d("Navigating to AccountSettingsScreen")
                                newIntent.removeExtra("navigateToAccountSettings") // Prevent re-navigation
                                navController.navigate(AccountSettingsRoute)
                            }
                            // Consume the intent after processing
                            intentState.value = null
                        }
                    }

                    ReadeckNavHost(
                        navController = navController,
                        startDestination = it
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intentState.value = intent
    }
}

@SuppressLint("WrongStartDestinationType")
@Composable
fun ReadeckNavHost(navController: NavHostController, startDestination: Any) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable<BookmarkListRoute> { BookmarkListScreen(navController) }
        composable<SettingsRoute> { SettingsScreen(navController) }
        composable<AccountSettingsRoute> { AccountSettingsScreen(navController) }
        composable<BookmarkDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<BookmarkDetailRoute>()
            BookmarkDetailScreen(
                navController,
                route.bookmarkId
            )
        }
        composable<OpenSourceLibrariesRoute> {
            OpenSourceLibrariesScreen(navHostController = navController)
        }
        composable<LogViewRoute> {
            LogViewScreen(navController = navController)
        }
        composable<SyncSettingsRoute> {
            SyncSettingsScreen(navHostController = navController)
        }
        composable<UiSettingsRoute> {
            UiSettingsScreen(navHostController = navController)
        }
        composable<LoginRoute> {
            LoginScreen()
        }
    }
}
