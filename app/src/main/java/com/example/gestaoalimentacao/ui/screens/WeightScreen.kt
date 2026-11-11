package com.example.gestaoalimentacao.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestaoalimentacao.data.model.WeightEntry
import com.example.gestaoalimentacao.ui.viewmodel.WeightViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

fun formatarKg(valor: Double): String = String.format(Locale("pt", "BR"), "%.1f kg", valor)

/** Lê a foto reduzida para ~[ladoMaximo] px, sem carregar a imagem inteira na memória. */
private fun decodificarFoto(caminho: String, ladoMaximo: Int): ImageBitmap? {
    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(caminho, limites)
    if (limites.outWidth <= 0 || limites.outHeight <= 0) return null

    var amostra = 1
    while (limites.outWidth / (amostra * 2) >= ladoMaximo && limites.outHeight / (amostra * 2) >= ladoMaximo) {
        amostra *= 2
    }
    val opcoes = BitmapFactory.Options().apply { inSampleSize = amostra }
    return BitmapFactory.decodeFile(caminho, opcoes)?.asImageBitmap()
}

@Composable
fun FotoLocal(caminho: String, modifier: Modifier = Modifier) {
    // Decodificar na composição trava a rolagem da lista; fazemos em segundo plano.
    val carregada by produceState<ImageBitmap?>(initialValue = null, caminho) {
        value = withContext(Dispatchers.IO) {
            try {
                decodificarFoto(caminho, ladoMaximo = 512)
            } catch (e: Exception) {
                null
            } catch (e: OutOfMemoryError) {
                null
            }
        }
    }
    val imagem = carregada
    if (imagem != null) {
        Image(
            bitmap = imagem,
            contentDescription = "Foto do registro",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}

@Composable
fun WeightScreen(
    weightViewModel: WeightViewModel,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit
) {
    LaunchedEffect(Unit) { weightViewModel.listar() }

    val registros = weightViewModel.registros
    var excluirId by rememberSaveable { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Peso",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Acompanhe sua evolução",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E7FF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Peso atual", fontSize = 13.sp, color = Color(0xFF475569))
                    Text(
                        text = registros.firstOrNull()?.let { formatarKg(it.weightKg) } ?: "--",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D52CE)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    weightViewModel.mensagem?.let { Text(it, color = Color(0xFF64748B)) }
                    if (registros.isEmpty()) Text("Nenhum registro de peso ainda", color = Color(0xFF64748B))
                }
                items(registros, key = { it.id }) { registro ->
                    WeightCardItem(
                        registro = registro,
                        enabled = !weightViewModel.carregando,
                        onEditClick = { onEditClick(registro.id) },
                        onDeleteClick = { excluirId = registro.id }
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = Color(0xFF0D52CE),
            contentColor = Color.White
        ) {
            Text(
                text = "Novo registro",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }

    val selecionado = registros.firstOrNull { it.id == excluirId }
    if (selecionado != null) {
        AlertDialog(
            onDismissRequest = { excluirId = null },
            title = { Text("Excluir registro?") },
            text = { Text("Excluir o registro de ${formatarKg(selecionado.weightKg)}? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        excluirId = null
                        weightViewModel.excluir(selecionado.id)
                    },
                    enabled = !weightViewModel.carregando
                ) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { excluirId = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun WeightCardItem(
    registro: WeightEntry,
    enabled: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (registro.photoPath.isNotBlank()) {
                    FotoLocal(
                        caminho = registro.photoPath,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatarKg(registro.weightKg),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = registro.dateText,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEditClick, enabled = enabled) { Text("Alterar") }
                TextButton(onClick = onDeleteClick, enabled = enabled) {
                    Text("Excluir", color = Color(0xFFEF4444))
                }
            }
        }
    }
}
