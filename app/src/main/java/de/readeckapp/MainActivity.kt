package de.readeckapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
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
import de.readeckapp.ui.navigation.AccountSettingsRoute
import de.readeckapp.ui.navigation.BookmarkDetailRoute
import de.readeckapp.ui.navigation.BookmarkListRoute
import de.readeckapp.ui.navigation.LogViewRoute
import de.readeckapp.ui.navigation.OpenSourceLibrariesRoute
import de.readeckapp.ui.navigation.SettingsRoute
import de.readeckapp.ui.navigation.SyncSettingsRoute
import de.readeckapp.ui.navigation.UiSettingsRoute
import de.readeckapp.ui.settings.AccountSettingsScreen
import de.readeckapp.ui.settings.LogViewScreen
import de.readeckapp.ui.settings.OpenSourceLibrariesScreen
import de.readeckapp.ui.settings.SettingsScreen
import de.readeckapp.ui.settings.SyncSettingsScreen
import de.readeckapp.ui.settings.UiSettingsScreen
import de.readeckapp.ui.theme.ReadeckAppTheme
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
            val eInkMode = viewModel.eInkMode.collectAsState()
            val navController = rememberNavController()
            intentState = remember { mutableStateOf(intent) }
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            val noValidUrlMessage = stringResource(id = R.string.not_valid_url)

            LaunchedEffect(intentState.value) {
                intentState.value?.let { newIntent ->
                    if (newIntent.action == Intent.ACTION_SEND && newIntent.type == "text/plain") {
                        val sharedText = newIntent.getStringExtra(Intent.EXTRA_TEXT)
                        if (sharedText.isNullOrBlank()) {
                            scope.launch {
                                Toast.makeText(context, noValidUrlMessage, Toast.LENGTH_LONG).show()
                            }
                        } else {
                            navController.navigate(BookmarkListRoute(sharedText = sharedText))
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

            val themeValue = when (theme.value) {
                Theme.SYSTEM -> if (isSystemInDarkTheme()) Theme.DARK else Theme.LIGHT
                else -> theme.value
            }

            ReadeckAppTheme(theme = themeValue, eInkMode = eInkMode.value) {
                ReadeckNavHost(navController, eInkMode = eInkMode.value)
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
fun ReadeckNavHost(navController: NavHostController, eInkMode: Boolean = false) {
    NavHost(
        navController = navController,
        startDestination = BookmarkListRoute(),
        enterTransition = { if (eInkMode) EnterTransition.None else fadeIn(animationSpec = tween(700)) },
        exitTransition = { if (eInkMode) ExitTransition.None else fadeOut(animationSpec = tween(700)) },
        popEnterTransition = { if (eInkMode) EnterTransition.None else fadeIn(animationSpec = tween(700)) },
        popExitTransition = { if (eInkMode) ExitTransition.None else fadeOut(animationSpec = tween(700)) },
    ) {
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
    }
}
