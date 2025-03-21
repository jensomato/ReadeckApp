package de.readeckapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import de.readeckapp.ui.detail.BookmarkDetailScreen
import de.readeckapp.ui.list.BookmarkListScreen
import de.readeckapp.ui.navigation.AccountSettingsRoute
import de.readeckapp.ui.navigation.BookmarkDetailRoute
import de.readeckapp.ui.navigation.BookmarkListRoute
import de.readeckapp.ui.navigation.OpenSourceLibrariesRoute
import de.readeckapp.ui.navigation.SettingsRoute
import de.readeckapp.ui.settings.AccountSettingsScreen
import de.readeckapp.ui.settings.OpenSourceLibrariesScreen
import de.readeckapp.ui.settings.SettingsScreen
import de.readeckapp.ui.theme.ReadeckAppTheme
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            ReadeckAppTheme {
                ReadeckNavHost(navController)
            }
        }
    }
}

@Composable
fun ReadeckNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = BookmarkListRoute) {
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
    }
}
