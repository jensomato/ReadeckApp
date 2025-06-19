package de.readeckapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.readeckapp.R

@Composable
fun AdaptiveReadeckIcon(size: Dp = 128.dp) {
    val bgColor = colorResource(id = R.color.ic_launcher_background) // Use your background color
    val icon = painterResource(id = R.drawable.ic_launcher_foreground)

    Surface(
        modifier = Modifier.size(size),
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp
    ){
        Image(
            painter = icon,
            contentDescription = "App Launcher Foreground Icon",
            modifier = Modifier
                .fillMaxSize()
                .scale(1.5f)
        )

    }
}