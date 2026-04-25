package com.stephen.litertnewdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.stephen.litertnewdemo.ui.LiteRtlmManagerScreen
import com.stephen.litertnewdemo.ui.theme.LiteRTNewDemoTheme

class MainActivity : ComponentActivity() {

    val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiteRTNewDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LiteRtlmManagerScreen(innerPadding)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.closeLiteRtEngine()
    }
}
