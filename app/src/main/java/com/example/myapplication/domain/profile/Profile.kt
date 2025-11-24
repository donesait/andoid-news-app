package com.example.myapplication.domain.profile

data class Profile(
    val fullName: String = "",
    val position: String = "",
    val avatarUri: String = "",
    val resumeUrl: String = "",
    val favoritePairTime: String = ""
) {
    val hasResume: Boolean get() = resumeUrl.isNotBlank()
    val hasReminder: Boolean get() = favoritePairTime.isNotBlank()
}

