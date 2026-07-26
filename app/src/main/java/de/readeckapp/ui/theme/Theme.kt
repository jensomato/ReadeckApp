package de.readeckapp.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import de.readeckapp.domain.model.Theme
import de.readeckapp.ui.theme.sepia.SepiaColorScheme

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private val EInkColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDDDDD),
    onPrimaryContainer = Color.Black,
    secondary = Color.Black,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDDDDD),
    onSecondaryContainer = Color.Black,
    tertiary = Color.Black,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDDDDDD),
    onTertiaryContainer = Color.Black,
    error = Color.Black,
    onError = Color.White,
    errorContainer = Color(0xFFCCCCCC),
    onErrorContainer = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color.Black,
    outline = Color.Black,
    outlineVariant = Color(0xFFCCCCCC),
    scrim = Color.Black,
    inverseSurface = Color.Black,
    inverseOnSurface = Color.White,
    inversePrimary = Color.White,
    surfaceDim = Color(0xFFEEEEEE),
    surfaceBright = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFEEEEEE),
    surfaceContainer = Color(0xFFEEEEEE),
    surfaceContainerHigh = Color(0xFFDDDDDD),
    surfaceContainerHighest = Color(0xFFCCCCCC),
)

@Composable
fun ReadeckAppTheme(
    theme: Theme = Theme.LIGHT,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    eInkMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        eInkMode -> EInkColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && theme in listOf(
            Theme.DARK,
            Theme.LIGHT
        ) -> {
            val context = LocalContext.current
            if (theme == Theme.DARK) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        theme == Theme.DARK -> DarkColorScheme
        theme == Theme.SEPIA -> SepiaColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}