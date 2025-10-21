package com.example.litertdemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.litertdemo.ui.theme.LiteRTDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiteRTDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    startGenerateText(innerPadding)
                }
            }
        }
    }
}

@Composable
fun startGenerateText(paddingValues: PaddingValues) {

    LaunchedEffect(Unit) {
        LiteRTServiceManager.init()
    }

    Box(
        modifier = Modifier.padding(paddingValues).fillMaxSize(1f),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "生成文本", fontSize = 40.sp, modifier = Modifier.clickable {
            val result = LiteRTServiceManager.generateText("The weather is so good")
            Log.d(LiteRTServiceManager.TAG, "generateText: $result")
        })
    }
}
