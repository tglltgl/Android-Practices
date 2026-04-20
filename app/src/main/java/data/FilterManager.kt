package com.example.praktica3.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "filters")

class FilterManager(private val context: Context) {

    private val SELECTED_FILTER = stringPreferencesKey("selected_filter")


    private val ONLY_VETERANS = booleanPreferencesKey("only_veterans")

    val selectedFilter: Flow<String> = context.dataStore.data.map {
        it[SELECTED_FILTER] ?: "Все"
    }


    val onlyVeterans: Flow<Boolean> = context.dataStore.data.map {
        it[ONLY_VETERANS] ?: false
    }

    suspend fun saveFilter(filter: String) {
        context.dataStore.edit { it[SELECTED_FILTER] = filter }
    }


    suspend fun saveVeteransFilter(value: Boolean) {
        context.dataStore.edit { it[ONLY_VETERANS] = value }
    }
}