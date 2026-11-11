package com.example.gestaoalimentacao.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// A API manda os ingredientes em 20 campos separados (strIngredient1..20 e strMeasure1..20),
// por isso cada receita é lida como mapa e depois convertida em MealDto.
data class MealSearchResponse(
    @SerializedName("meals") val meals: List<Map<String, String?>>?
)

data class MealDto(
    val id: String,
    val name: String,
    val category: String?,
    val area: String?,
    val instructions: String?,
    val ingredients: List<String>
)

fun converterRefeicao(dados: Map<String, String?>) = MealDto(
    id = dados["idMeal"].orEmpty(),
    name = dados["strMeal"].orEmpty(),
    category = dados["strCategory"],
    area = dados["strArea"],
    instructions = dados["strInstructions"],
    ingredients = (1..20).mapNotNull { i ->
        val ingrediente = dados["strIngredient$i"]?.trim()
        if (ingrediente.isNullOrEmpty()) null
        else "${dados["strMeasure$i"].orEmpty().trim()} $ingrediente".trim()
    }
)

interface MealApi {
    @GET("search.php")
    suspend fun buscar(@Query("s") nome: String): MealSearchResponse
}
