package com.stephen.litertnewdemo.usecase

import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.SamplerConfig
import com.stephen.litertnewdemo.utils.infoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class LiteRtLoadEngineUsecase {
    private val engineScope = CoroutineScope(Dispatchers.IO)
    private var loadEngineJob: Job? = null
    private var conversationJob: Job? = null
    private var engine: Engine? = null
    private val conversationList = mutableListOf<Conversation>()

    /**
     * 初始化路径，engine配置
     */
    fun setLiteRtLmFilePath(filePath: String) {
        if (filePath.isBlank()) {
            return
        }
        val engineConfig = EngineConfig(
            modelPath = filePath, // Replace with your model path
            backend = Backend.CPU(), // Or Backend.NPU(nativeLibraryDir = "...")
        )
//        engineConfig = EngineConfig(
//            modelPath = filePath,
//            backend = Backend.NPU(nativeLibraryDir = appContext.applicationInfo.nativeLibraryDir)
//        )
        engine = Engine(engineConfig)
    }

    /**
     * 加载模型
     */
    fun initializeModel(onFinished: () -> Unit) {
        loadEngineJob = engineScope.launch {
            engine?.initialize()
            onFinished()
        }
    }

    fun startConversationTest() {
        infoLog()
        val conversationConfig = ConversationConfig(
            systemInstruction = Contents.of("You are a helpful assistant."),
            initialMessages = listOf(
                Message.user("What is the capital city of the United States?"),
                Message.model("Washington, D.C."),
            ),
            samplerConfig = SamplerConfig(topK = 10, topP = 0.95, temperature = 0.8),
        )

        val conversation = engine?.createConversation(conversationConfig)
        conversation?.let {
            conversationList.add(conversation)
            conversationJob = engineScope.launch {
                val askMsg = "你好，介绍一下你自己"
                infoLog("[startConversationTest] ask: $askMsg")
                conversation.sendMessageAsync(askMsg)
                    .catch { e ->
                        error("error: $e")
                    }
                    .collect { msg ->
                        infoLog("[startConversationTest] answer: $msg")
                    }
            }
        }
    }

    /**
     * 关闭回话和模型连接
     */
    fun close() {
        conversationList.forEach {
            it.close()
        }
        loadEngineJob?.cancel()
        loadEngineJob = null
        engine?.close()
    }
}