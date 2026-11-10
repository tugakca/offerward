package io.github.tugakca.offerward

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.tugakca.offerward.core.designsystem.theme.OfferwardTheme
import io.github.tugakca.offerward.ui.OfferwardApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            OfferwardTheme {
                OfferwardApp()
            }
        }
    }
}
