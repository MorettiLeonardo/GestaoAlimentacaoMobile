package com.example.gestaoalimentacao.data.repository

import com.example.gestaoalimentacao.data.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RecipeRepository {
    private val mockRecipes = listOf(
        Recipe(
            id = "1",
            title = "Frango Grelhado com Salada Verde",
            description = "Peito de frango temperado com ervas finas acompanhado de salada fresca.",
            prepTime = "20 min",
            calories = 320,
            category = "Proteína",
            ingredients = listOf(
                "200g de peito de frango",
                "1 xícara de alface e rúcula",
                "1/2 tomate picado",
                "1 colher de azeite de oliva",
                "Sal e pimenta a gosto"
            ),
            instructions = listOf(
                "Tempere o frango com sal, pimenta e ervas.",
                "Grelhe em frigideira antiaderente até dourar.",
                "Monte a salada e tempere com azeite e limão."
            )
        ),
        Recipe(
            id = "2",
            title = "Omelete de Ervas e Queijo Branco",
            description = "Omelete rápido, proteico e rico em nutrientes para um café reforçado.",
            prepTime = "10 min",
            calories = 250,
            category = "Low Carb",
            ingredients = listOf(
                "3 ovos inteiros",
                "30g de queijo minas frescal picado",
                "Salsinha e cebolinha a gosto",
                "1 pitada de sal"
            ),
            instructions = listOf(
                "Bata os ovos em uma tigela com sal e ervas.",
                "Despeje na frigideira aquecida e adicione o queijo.",
                "Dobre ao meio e sirva em seguida."
            )
        ),
        Recipe(
            id = "3",
            title = "Sopa Fit de Legumes com Frango",
            description = "Sopa leve, reconfortante e ideal para o jantar.",
            prepTime = "30 min",
            calories = 210,
            category = "Vegetal",
            ingredients = listOf(
                "150g de peito de frango desfiado",
                "1 cenoura picada",
                "1 chuchu e 1/2 abobrinha",
                "1 litro de caldo vegetal caseiro"
            ),
            instructions = listOf(
                "Cozinhe os legumes no caldo até ficarem macios.",
                "Adicione o frango desfiado e acerte o tempero.",
                "Deixe ferver por 5 minutos e sirva bem quente."
            )
        ),
        Recipe(
            id = "4",
            title = "Panqueca de Aveia e Banana",
            description = "Opção saudável para café da manhã ou lanche pré-treino.",
            prepTime = "12 min",
            calories = 280,
            category = "Fitness",
            ingredients = listOf(
                "1 banana madura",
                "2 colheres de sopa de aveia em flocos",
                "1 ovo",
                "Canela em pó a gosto"
            ),
            instructions = listOf(
                "Amasse a banana e misture com o ovo e a aveia.",
                "Despeje pequenas porções na frigideira untada.",
                "Doure dos dois lados e polvilhe canela."
            )
        ),
        Recipe(
            id = "5",
            title = "Suco Detox de Couve e Maçã",
            description = "Bebida refrescante, antioxidante e rica em fibras.",
            prepTime = "5 min",
            calories = 110,
            category = "Detox",
            ingredients = listOf(
                "1 folha de couve manteiga",
                "1 maçã verde com casca",
                "Suco de 1/2 limão",
                "200ml de água de coco"
            ),
            instructions = listOf(
                "Bata todos os ingredientes no liquidificador.",
                "Sirva gelado sem coar para preservar as fibras."
            )
        )
    )

    private val _recipes = MutableStateFlow<List<Recipe>>(mockRecipes)
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    fun getRecipeById(id: String): Recipe? {
        return _recipes.value.find { it.id == id }
    }
}
