package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainBirdRadarScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BirdViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {

    private val viewModel: BirdViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Request location permissions once on startup smoothly
                val locationPermissions = rememberMultiplePermissionsState(
                    permissions = listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )

                LaunchedEffect(Unit) {
                    if (!locationPermissions.allPermissionsGranted) {
                        locationPermissions.launchMultiplePermissionRequest()
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    MainBirdRadarScreen(viewModel = viewModel)
                }
            }
        }
    }
}
