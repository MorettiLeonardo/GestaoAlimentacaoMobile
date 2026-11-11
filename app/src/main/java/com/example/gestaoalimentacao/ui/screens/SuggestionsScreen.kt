package com.example.gestaoalimentacao.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestaoalimentacao.data.remote.MealDto
import com.example.gestaoalimentacao.ui.viewmodel.SuggestionsViewModel

// Integração com API (Retrofit): busca receitas na internet (TheMealDB).
@Composable
fun SuggestionsScreen(
    suggestionsViewModel: SuggestionsViewModel,
    voltar: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (suggestionsViewModel.resultados.isEmpty()) suggestionsViewModel.buscar()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sugestões",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            TextButton(onClick = voltar) { Text("Voltar") }
        }
        Text(
            text = "Receitas buscadas na internet (TheMealDB)",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = suggestionsViewModel.busca,
                onValueChange = suggestionsViewModel::mudarBusca,
                label = { Text("Buscar (ex.: chicken)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = suggestionsViewModel::buscar,
                enabled = !suggestionsViewModel.carregando,
                shape = RoundedCornerShape(12.dp)
            ) { Text("Buscar") }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (suggestionsViewModel.carregando) Text("Carregando...", color = Color(0xFF64748B))
        suggestionsViewModel.mensagem?.let { Text(it, color = Color(0xFF64748B)) }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(suggestionsViewModel.resultados, key = { it.id }) { refeicao ->
                SuggestionCard(refeicao)
            }
        }
    }
}

@Composable
fun SuggestionCard(refeicao: MealDto) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = refeicao.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Fechar receita" else "Ver receita completa",
                    tint = Color(0xFF64748B)
                )
            }
            Text(
                text = listOfNotNull(refeicao.category, refeicao.area).joinToString(" • "),
                fontSize = 12.sp,
                color = Color(0xFF15803D)
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (!expanded) {
                Text(
                    text = refeicao.instructions.orEmpty(),
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Ingredientes:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    refeicao.ingredients.forEach { ingrediente ->
                        Text(
                            text = "• $ingrediente",
                            fontSize = 13.sp,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Modo de Preparo:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = refeicao.instructions.orEmpty(),
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                }
            }
        }
    }
}
