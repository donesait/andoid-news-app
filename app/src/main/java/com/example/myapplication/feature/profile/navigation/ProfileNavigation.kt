package com.example.myapplication.feature.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.myapplication.feature.profile.presentation.EditProfileRoute
import com.example.myapplication.feature.profile.presentation.ProfileRoute

const val PROFILE_ROUTE = "profile"
const val PROFILE_EDIT_ROUTE = "profile/edit"

fun NavGraphBuilder.profileGraph(navController: NavHostController) {
    composable(PROFILE_ROUTE) {
        ProfileRoute(onEditProfile = { navController.navigate(PROFILE_EDIT_ROUTE) })
    }
    composable(PROFILE_EDIT_ROUTE) {
        EditProfileRoute(onClose = { navController.popBackStack() })
    }
}

