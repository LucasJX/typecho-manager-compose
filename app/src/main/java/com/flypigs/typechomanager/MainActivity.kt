package com.flypigs.typechomanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.flypigs.typechomanager.ui.navigation.NavGraph
import com.flypigs.typechomanager.ui.theme.TypechoManagerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TypechoManagerTheme {
                NavGraph()
            }
        }
    }
}
