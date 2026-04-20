package com.example.praktica3

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("profile_prefs")

class ProfileRepository(private val context: Context) {
    private val NAME = stringPreferencesKey("name")
    private val PHOTO = stringPreferencesKey("photo")
    private val RESUME = stringPreferencesKey("resume")

    val profileData = context.dataStore.data.map { p ->
        ProfileState(
            name = p[NAME] ?: "",
            photoUri = p[PHOTO]?.let { Uri.parse(it) },
            resumeUrl = p[RESUME] ?: ""
        )
    }

    suspend fun saveProfile(name: String, photoUri: Uri?, resumeUrl: String) {
        context.dataStore.edit { p ->
            p[NAME] = name
            p[PHOTO] = photoUri?.toString() ?: ""
            p[RESUME] = resumeUrl
        }
    }
}