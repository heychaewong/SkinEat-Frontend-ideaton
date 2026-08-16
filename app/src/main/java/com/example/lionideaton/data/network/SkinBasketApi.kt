package com.example.lionideaton.data.network

import com.example.lionideaton.data.model.FoodImageAnalysisResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// Draft interface based on the team's written API spec (section 16).
// TODO: confirm exact paths/field names once the backend teammate's implementation is live.
interface SkinBasketApi {
    @Multipart
    @POST("ai/food-image")
    suspend fun analyzeFoodImage(@Part image: MultipartBody.Part): FoodImageAnalysisResponse
}
