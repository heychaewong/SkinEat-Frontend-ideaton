package com.example.lionideaton.ui.screens

import androidx.lifecycle.ViewModel
import com.example.lionideaton.data.model.ConstraintType
import com.example.lionideaton.data.model.SkinConcern
import com.example.lionideaton.data.model.SkinType
import com.example.lionideaton.data.model.UserConstraintItem
import com.example.lionideaton.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Activity-scoped, shared by Onboarding (writes it once), Home/My (read skin type + tags),
// and Basket (would filter out allergy/dislike ingredients — see user_constraint table).
class UserProfileViewModel : ViewModel() {

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    fun completeOnboarding(
        skinType: SkinType,
        concerns: List<SkinConcern>,
        allergies: List<String>,
        dislikes: List<String>
    ) {
        _profile.update {
            it.copy(
                skinType = skinType,
                concerns = concerns,
                constraints = allergies.map { name -> UserConstraintItem(ConstraintType.ALLERGY, name) } +
                    dislikes.map { name -> UserConstraintItem(ConstraintType.DISLIKE, name) }
            )
        }
        _onboardingCompleted.value = true
    }

    fun updateSkinTargets(concerns: List<SkinConcern>, allergies: List<String>, dislikes: List<String>) {
        _profile.update {
            it.copy(
                concerns = concerns,
                constraints = allergies.map { name -> UserConstraintItem(ConstraintType.ALLERGY, name) } +
                    dislikes.map { name -> UserConstraintItem(ConstraintType.DISLIKE, name) }
            )
        }
    }
}
