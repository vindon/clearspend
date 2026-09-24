package com.clearspend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.clearspend.presentation.navigation.ClearSpendNavGraph
import com.clearspend.presentation.theme.ClearSpendTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ClearSpendTheme(darkTheme = true) {
                ClearSpendNavGraph()
            }
        }
    }
}
