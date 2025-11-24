package com.example.myapplication.feature.profile.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.domain.profile.Profile
import com.example.myapplication.domain.profile.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore(name = "profile")

class ProfileRepositoryImpl(private val context: Context) : ProfileRepository {

    private object Keys {
        val FULL_NAME = stringPreferencesKey("full_name")
        val POSITION = stringPreferencesKey("position")
        val AVATAR_URI = stringPreferencesKey("avatar_uri")
        val RESUME_URL = stringPreferencesKey("resume_url")
        val FAVORITE_TIME = stringPreferencesKey("favorite_time")
    }

    override val profileFlow: Flow<Profile> = context.profileDataStore.data.map { prefs ->
        Profile(
            fullName = prefs[Keys.FULL_NAME].orEmpty(),
            position = prefs[Keys.POSITION].orEmpty(),
            avatarUri = prefs[Keys.AVATAR_URI].orEmpty(),
            resumeUrl = prefs[Keys.RESUME_URL].orEmpty(),
            favoritePairTime = prefs[Keys.FAVORITE_TIME].orEmpty()
        )
    }

    override suspend fun save(profile: Profile) {
        context.profileDataStore.edit { prefs ->
            prefs[Keys.FULL_NAME] = profile.fullName
            prefs[Keys.POSITION] = profile.position
            prefs[Keys.AVATAR_URI] = profile.avatarUri
            prefs[Keys.RESUME_URL] = profile.resumeUrl
            prefs[Keys.FAVORITE_TIME] = profile.favoritePairTime
        }
    }
}

