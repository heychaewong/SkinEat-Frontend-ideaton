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

    // In-memory only, keyed by email — there's no backend auth endpoint or local persistence
    // wired yet, so this only survives for the current app process (wiped on app restart).
    // Tracking onboardingCompleted per account (not as one global flag) is what makes the
    // "onboarding only right after signup" behavior work when switching accounts in one session.
    private data class Account(var password: String, var profile: UserProfile, var onboardingCompleted: Boolean = false)

    private val accounts = mutableMapOf<String, Account>()

    fun signUp(email: String, password: String, nickname: String): Boolean {
        val key = email.trim()
        if (accounts.containsKey(key)) return false
        val newProfile = UserProfile(email = key, nickname = nickname)
        accounts[key] = Account(password = password, profile = newProfile, onboardingCompleted = false)
        _profile.value = newProfile
        _onboardingCompleted.value = false
        _isLoggedIn.value = true
        return true
    }

    // Demo-friendly: since there's no real backend to check credentials against yet, an
    // unregistered (or blank) email just logs the tester in directly instead of failing.
    // Only a registered email with the WRONG password is actually rejected.
    fun login(email: String, password: String): Boolean {
        val key = email.trim().ifBlank { "guest@skinbasket.demo" }
        val existing = accounts[key]
        if (existing != null && existing.password != password) return false

        val account = existing ?: Account(
            password = password,
            profile = UserProfile(email = key, nickname = "게스트")
        ).also { accounts[key] = it }

        _profile.value = account.profile
        _onboardingCompleted.value = account.onboardingCompleted
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
        persistCurrentProfile(onboardingCompleted = true)
    }

    fun updateSkinTargets(concerns: List<SkinConcern>, allergies: List<String>, dislikes: List<String>) {
        _profile.update {
            it.copy(
                concerns = concerns,
                constraints = allergies.map { name -> UserConstraintItem(ConstraintType.ALLERGY, name) } +
                    dislikes.map { name -> UserConstraintItem(ConstraintType.DISLIKE, name) }
            )
        }
        persistCurrentProfile()
    }

    private fun persistCurrentProfile(onboardingCompleted: Boolean? = null) {
        val email = _profile.value.email
        accounts[email]?.let {
            it.profile = _profile.value
            if (onboardingCompleted != null) it.onboardingCompleted = onboardingCompleted
        }
    }
}
