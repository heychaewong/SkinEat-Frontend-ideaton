package com.example.lionideaton.data.model

// Mirrors `users.skin_type` enum.
enum class SkinType(val label: String) {
    OILY("지성"),
    DRY("건성"),
    COMBINATION("복합성")
}

// Mirrors `users.concerns` JSON array values.
enum class SkinConcern(val label: String) {
    ACNE("트러블"),
    DRYNESS("건조함"),
    SEBUM("유분"),
    PIGMENTATION("색소침착")
}

// Mirrors `user_constraint.type` — allergy is a hard exclude, dislike suggests alternates instead.
enum class ConstraintType { ALLERGY, DISLIKE }

data class UserConstraintItem(val type: ConstraintType, val ingredientName: String)

data class UserProfile(
    val nickname: String = "김민지",
    val skinType: SkinType? = null,
    val concerns: List<SkinConcern> = emptyList(),
    val constraints: List<UserConstraintItem> = emptyList()
)
