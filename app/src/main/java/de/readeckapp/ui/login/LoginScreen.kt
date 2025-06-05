package de.readeckapp.ui.login

import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.readeckapp.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navHostController: NavHostController) {
    Scaffold(


    ){ padding ->

        Column(
            modifier = Modifier
                .padding(vertical = 64.dp, horizontal = 16.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AdaptiveIcon(150.dp)
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Readeck Instance URL") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AdaptiveIcon(size: Dp = 128.dp) {
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
@Composable
@Preview
fun LoginScreenPreview() {
    LoginScreen(navHostController = NavHostController(LocalContext.current))
}