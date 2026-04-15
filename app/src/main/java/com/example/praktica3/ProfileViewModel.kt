package com.example.praktica3

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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

    fun saveProfile() {
        viewModelScope.launch {
            repository.saveProfile(
                _state.value.name,
                _state.value.photoUri,
                _state.value.resumeUrl
            )
            _state.update { it.copy(isEditing = false) }
        }
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
}