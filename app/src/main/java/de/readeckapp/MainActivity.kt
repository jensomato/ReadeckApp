package de.readeckapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import de.readeckapp.ui.theme.ReadeckAppTheme
import timber.log.Timber
import android.os.Bundle
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.readeckapp.ui.list.BookmarkListScreen
import de.readeckapp.ui.settings.AccountSettingsScreen
import de.readeckapp.ui.detail.BookmarkDetailScreen
import de.readeckapp.ui.settings.SettingsScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadeckAppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "bookmarkList") {
                    composable("bookmarkList") { BookmarkListScreen(navController) }
                    composable("settings") { SettingsScreen(navController) }
                    composable("accountSettings") { AccountSettingsScreen(navController) }
                    composable("bookmarkDetail/{bookmarkId}") { backStackEntry ->
                        BookmarkDetailScreen(navController, backStackEntry.arguments?.getString("bookmarkId"))
                    }
                }
            }
        }
    }
}
