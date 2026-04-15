package com.example.praktica3

import android.net.Uri

data class ProfileState(
    val name: String = "",
    val photoUri: Uri? = null,
    val resumeUrl: String = "",
    val isEditing: Boolean = false
)