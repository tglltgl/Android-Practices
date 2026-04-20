package com.example.praktica3

import android.app.AlarmManager
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.profileData.collect { savedProfile ->
                _state.update { it.copy(
                    name = savedProfile.name,
                    photoUri = savedProfile.photoUri,
                    resumeUrl = savedProfile.resumeUrl,
                    // Достаем сохраненное время (если в репозитории есть эти поля)
                    // pairTime = savedProfile.pairTime,
                    isEditing = false
                ) }
            }
        }
    }

    fun onNameChange(newName: String) {
        _state.update { it.copy(name = newName) }
    }

    fun onResumeChange(newUrl: String) {
        _state.update { it.copy(resumeUrl = newUrl) }
    }

    fun setEditing(editing: Boolean) {
        _state.update { it.copy(isEditing = editing) }
    }

   
    fun saveProfile(context: Context) {
        val currentState = _state.value

        
        if (currentState.timeError == null && currentState.pairTime.isNotEmpty()) {
            viewModelScope.launch {
               
                repository.saveProfile(
                    currentState.name,
                    currentState.photoUri,
                    currentState.resumeUrl
                )

                
                scheduleNotification(context, currentState.pairTime, currentState.name)

                _state.update { it.copy(isEditing = false) }
            }
        }
    }

    private fun scheduleNotification(context: Context, time: String, name: String) {
        
        val parts = time.split(":")
        if (parts.size < 2) return

        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        val intent = Intent(context, PairReceiver::class.java).apply {
            putExtra("name", name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun handleImageSelection(context: Context, uri: Uri?) {
        uri?.let {
            val internalUri = saveImageToInternalStorage(context, it)
            _state.update { it.copy(photoUri = internalUri) }
        }
    }

    private fun saveImageToInternalStorage(context: Context, uri: Uri): Uri {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "avatar.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }
        return Uri.fromFile(file)
    }

    fun createPhotoUri(context: Context): Uri {
        val file = File(context.filesDir, "temp_camera.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun onTimeChange(time: String) {
        _state.value = _state.value.copy(
            pairTime = time,
            timeError = validateTime(time)
        )
    }

    private fun validateTime(time: String): String? {
        val regex = Regex("^([01]\\d|2[0-3]):([0-5]\\d)$")
        return if (time.matches(regex)) null else "Формат HH:mm"
    }

    fun downloadResume(context: Context, url: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Resume")
                .setMimeType("application/pdf")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "resume.pdf")
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
        } catch (e: Exception) { e.printStackTrace() }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(ProfileRepository(context)) as T
            }
        }
    }
}
