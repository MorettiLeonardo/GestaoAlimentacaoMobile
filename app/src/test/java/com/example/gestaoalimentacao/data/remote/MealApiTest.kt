package com.example.gestaoalimentacao.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class MealApiTest {
    @Test
    fun converterRefeicao_juntaMedidaEIngrediente_eIgnoraVazios() {
        val refeicao = converterRefeicao(
            mapOf(
                "idMeal" to "1",
                "strMeal" to "Frango",
                "strIngredient1" to "Chicken",
                "strMeasure1" to "1 kg ",
                "strIngredient2" to "Salt",
                "strMeasure2" to null,
                "strIngredient3" to "",
                "strMeasure3" to "2 tsp"
            )
        )

        assertEquals("Frango", refeicao.name)
        assertEquals(listOf("1 kg Chicken", "Salt"), refeicao.ingredients)
    }
}
