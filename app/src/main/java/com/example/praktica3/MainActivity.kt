package com.example.praktica3

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.praktica3.ui.theme.Praktica3Theme
import com.example.profile.ProfileRepository
import com.example.profile.ProfileViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        createChannel(this)

        val profileRepo = ProfileRepository(applicationContext)
        val profileViewModel = ProfileViewModel(profileRepo)

        setContent {
            Praktica3Theme {
                val playerViewModel: PlayerViewModel = viewModel()
                AppNavigation(playerViewModel, profileViewModel)
            }
        }
    }


    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pair_channel",
                "Пары",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о начале любимых пар"
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}