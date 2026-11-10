package com.example.gestaoalimentacao.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestaoalimentacao.ui.viewmodel.WeightViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Recurso nativo: biblioteca de imagens (galeria). A foto escolhida é copiada para a pasta do app.
private fun copiarImagem(context: Context, uri: Uri): String? {
    return try {
        val arquivo = File(context.filesDir, "peso_${System.currentTimeMillis()}.jpg")
        val entrada = context.contentResolver.openInputStream(uri) ?: return null
        entrada.use { origem ->
            arquivo.outputStream().use { destino -> origem.copyTo(destino) }
        }
        arquivo.absolutePath
    } catch (e: Exception) {
        null
    }
}

@Composable
fun AddEditWeightScreen(
    weightViewModel: WeightViewModel,
    weightId: String? = null,
    voltar: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(weightId) {
        if (weightId != null) weightViewModel.buscarPorId(weightId)
        else weightViewModel.novoRegistro()
    }
    LaunchedEffect(weightViewModel.salvo) {
        if (weightViewModel.salvo) {
            weightViewModel.consumirSalvo()
            voltar()
        }
    }

    val existente = weightViewModel.registro

    var peso by remember(existente) { mutableStateOf(existente?.weightKg?.toString() ?: "") }
    var data by remember(existente) {
        mutableStateOf(
            existente?.dateText ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        )
    }
    var foto by remember(existente) { mutableStateOf(existente?.photoPath ?: "") }
    var erroFoto by remember { mutableStateOf(false) }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val caminho = copiarImagem(context, uri)
            if (caminho != null) {
                foto = caminho
                erroFoto = false
            } else {
                erroFoto = true
            }
        }
    }

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
            text = if (weightId == null) "Novo registro de peso" else "Alterar registro de peso",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        OutlinedTextField(
            value = peso,
            onValueChange = { peso = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(6) },
            label = { Text("Peso (kg)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = data,
            onValueChange = { data = it.filter { c -> c.isDigit() || c == '/' }.take(10) },
            label = { Text("Data (dd/MM/aaaa)") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (foto.isNotBlank()) {
            FotoLocal(
                caminho = foto,
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { galeria.launch("image/*") },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (foto.isBlank()) "Escolher foto da galeria" else "Trocar foto")
            }
            if (foto.isNotBlank()) {
                TextButton(onClick = { foto = "" }) { Text("Remover foto") }
            }
        }
        if (erroFoto) {
            Text("Não foi possível carregar a foto.", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        weightViewModel.mensagem?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
        if (weightViewModel.carregando) Text("Aguarde...")

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = voltar,
                enabled = !weightViewModel.carregando,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(text = "Cancelar", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = { weightViewModel.salvar(peso, data, foto) },
                enabled = !weightViewModel.carregando && !weightViewModel.salvo,
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
