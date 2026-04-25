package com.stephen.litertnewdemo.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.stephen.litertnewdemo.MainViewModel
import com.stephen.litertnewdemo.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.getKoin
import java.io.File

@Composable
fun LiteRtlmManagerScreen(
    paddingValues: PaddingValues,
    viewModel: MainViewModel = getKoin().get(),
) {
    val context = LocalContext.current
    var internalFiles by remember { mutableStateOf(listOf<File>()) }

    // 控制进度条显示的状态
    var isCopying by remember { mutableStateOf(false) }
    var copyProgress by remember { mutableFloatStateOf(0f) }

    // 获取协程作用域
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        internalFiles = viewModel.fetchInternalFiles(context)
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                // 切换到后台任务
                isCopying = true
                copyProgress = 0f

                // 启动协程处理 IO
                scope.launch(Dispatchers.IO) {
                    val success = viewModel.copyFileWithProgress(context, it) { progress ->
                        copyProgress = progress // 更新进度
                    }

                    isCopying = false
                    if (success) {
                        internalFiles = viewModel.fetchInternalFiles(context)
                        showToast("导入成功")
                    } else {
                        showToast("导入失败")
                    }
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // 进度展示区域
        AnimatedVisibility(visible = isCopying) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("正在复制文件: ${(copyProgress * 100).toInt()}%")
                LinearProgressIndicator(
                    progress = { copyProgress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (internalFiles.isEmpty() && !isCopying) {
                EmptyStateView(onSelectFile = { filePickerLauncher.launch(arrayOf("*/*")) })
            } else {
                FileListView(internalFiles) { name, path ->
                    viewModel.loadLiteRtEngine(path) {
                        showToast("模型 $name 加载成功")
                        viewModel.startConversation()
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(onSelectFile: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("未发现 .litertlm 配置文件", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSelectFile) {
            Text("从 SD 卡选取文件")
        }
    }
}

@Composable
fun FileListView(files: List<File>, onClickItem: (String, String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("应用内部文件列表", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(files) { file ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            onClickItem(file.name, file.absolutePath)
                        }
                ) {
                    ListItem(
                        headlineContent = { Text(file.name) },
                        supportingContent = { Text("${file.length() / 1024} KB") }
                    )
                }
            }
        }
    }
}
