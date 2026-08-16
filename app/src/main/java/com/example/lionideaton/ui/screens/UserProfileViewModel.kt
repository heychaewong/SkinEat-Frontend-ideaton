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

// Activity-scoped, shared by Auth (signs in/up), Onboarding (writes it once), Home/My (read
// skin type + tags), and Basket (would filter out allergy/dislike ingredients — see user_constraint table).
class UserProfileViewModel : ViewModel() {

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    // In-memory only — there's no backend auth endpoint wired yet, so this just simulates
    // account storage for the duration of the app process (see NetworkModule for the one
    // real API call that exists so far).
    private val registeredAccounts = mutableMapOf<String, Pair<String, String>>() // email -> (password, nickname)

    fun signUp(email: String, password: String, nickname: String): Boolean {
        if (registeredAccounts.containsKey(email)) return false
        registeredAccounts[email] = password to nickname
        _profile.update { it.copy(email = email, nickname = nickname) }
        _isLoggedIn.value = true
        return true
    }

    fun login(email: String, password: String): Boolean {
        val account = registeredAccounts[email] ?: return false
        if (account.first != password) return false
        _profile.update { it.copy(email = email, nickname = account.second) }
        _isLoggedIn.value = true
        return true
    }

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
