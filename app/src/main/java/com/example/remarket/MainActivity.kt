package com.example.remarket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.remarket.ui.product.create.CreateProductScreen
import com.example.remarket.ui.theme.ReMarketTheme
//import com.example.remarket.ui.navigation.NavGraph
import dagger.hilt.android.AndroidEntryPoint

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReMarketTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = androidx.compose.ui.Modifier.fillMaxSize()
                ) {

                    CreateProductScreen(
                        onNext = {

                        }
                    )
                }
            }
        }
    }
}

