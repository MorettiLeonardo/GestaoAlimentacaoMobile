package com.example.gestaoalimentacao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestaoalimentacao.ui.viewmodel.RecipeViewModel

@Composable
fun AddEditRecipeScreen(
    recipeViewModel: RecipeViewModel,
    recipeId: String? = null,
    voltar: () -> Unit
) {
    LaunchedEffect(recipeId) {
        if (recipeId != null) recipeViewModel.buscarPorId(recipeId)
    }
    LaunchedEffect(recipeViewModel.salvo) {
        if (recipeViewModel.salvo) {
            recipeViewModel.consumirSalvo()
            voltar()
        }
    }

    val existente = recipeViewModel.receita

    // Os campos são preenchidos de novo quando a receita chega do banco.
    var titulo by remember(existente) { mutableStateOf(existente?.title ?: "") }
    var descricao by remember(existente) { mutableStateOf(existente?.description ?: "") }
    var categoria by remember(existente) { mutableStateOf(existente?.category ?: "") }
    var tempoPreparo by remember(existente) { mutableStateOf(existente?.prepTime ?: "") }
    var calorias by remember(existente) { mutableStateOf(existente?.calories?.toString() ?: "") }
    var ingredientes by remember(existente) { mutableStateOf(existente?.ingredients?.joinToString("\n") ?: "") }
    var modoPreparo by remember(existente) { mutableStateOf(existente?.instructions?.joinToString("\n") ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (recipeId == null) "Nova Receita" else "Alterar Receita",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        OutlinedTextField(
            value = titulo,
            onValueChange = { if (it.length <= 100) titulo = it },
            label = { Text("Nome da receita") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = descricao,
            onValueChange = { if (it.length <= 300) descricao = it },
            label = { Text("Descrição") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = categoria,
            onValueChange = { if (it.length <= 50) categoria = it },
            label = { Text("Categoria") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = tempoPreparo,
                onValueChange = { if (it.length <= 30) tempoPreparo = it },
                label = { Text("Tempo de preparo") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = calorias,
                onValueChange = { calorias = it.filter { c -> c.isDigit() }.take(5) },
                label = { Text("Calorias (kcal)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = ingredientes,
            onValueChange = { ingredientes = it },
            label = { Text("Ingredientes (um por linha)") },
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = modoPreparo,
            onValueChange = { modoPreparo = it },
            label = { Text("Modo de preparo (um passo por linha)") },
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        recipeViewModel.mensagem?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
        if (recipeViewModel.carregando) Text("Aguarde...")

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = voltar,
                enabled = !recipeViewModel.carregando,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(text = "Cancelar", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = {
                    recipeViewModel.salvar(
                        titulo = titulo,
                        descricao = descricao,
                        tempoPreparo = tempoPreparo,
                        caloriasTexto = calorias,
                        categoria = categoria,
                        ingredientesTexto = ingredientes,
                        modoPreparoTexto = modoPreparo
                    )
                },
                enabled = !recipeViewModel.carregando && !recipeViewModel.salvo,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D52CE)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(text = "Salvar", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
