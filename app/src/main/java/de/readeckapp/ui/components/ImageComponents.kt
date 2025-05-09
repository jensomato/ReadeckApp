package de.readeckapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ErrorPlaceholderImage(modifier: Modifier, imageContentDescription: String) {
    val placeholderBackgroundColor = Color.LightGray.copy(alpha = 0.5f) // Light gray, semi-transparent
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(placeholderBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.BrokenImage,
            contentDescription = imageContentDescription,
            modifier = Modifier.size(48.dp),
            tint = Color.Gray
        )
    }
}