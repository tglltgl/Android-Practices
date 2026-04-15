package com.example.praktica3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.praktica3.ui.theme.Praktica3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val profileRepo = ProfileRepository(applicationContext)
        val profileViewModel = ProfileViewModel(profileRepo)

        setContent {
            Praktica3Theme {
                val playerViewModel: PlayerViewModel = viewModel()
                AppNavigation(playerViewModel, profileViewModel)
            }
        }
    }
}