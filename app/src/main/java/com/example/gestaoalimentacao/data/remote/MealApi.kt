package com.example.gestaoalimentacao.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class MealSearchResponse(
    @SerializedName("meals") val meals: List<MealDto>?
)

data class MealDto(
    @SerializedName("idMeal") val id: String,
    @SerializedName("strMeal") val name: String,
    @SerializedName("strCategory") val category: String?,
    @SerializedName("strArea") val area: String?,
    @SerializedName("strInstructions") val instructions: String?
)

interface MealApi {
    @GET("search.php")
    suspend fun buscar(@Query("s") nome: String): MealSearchResponse
}
