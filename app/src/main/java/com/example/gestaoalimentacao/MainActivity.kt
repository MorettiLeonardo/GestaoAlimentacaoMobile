package com.example.gestaoalimentacao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.gestaoalimentacao.ui.navigation.AppNavigation
import com.example.gestaoalimentacao.ui.theme.GestaoAlimentacaoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestaoAlimentacaoTheme {
                AppNavigation()
            }
        }
    }
}
