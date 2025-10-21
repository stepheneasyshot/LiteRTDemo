package com.example.litertdemo

import android.content.res.AssetFileDescriptor
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tflite.java.TfLite
import org.json.JSONObject
import org.tensorflow.lite.InterpreterApi
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object LiteRTServiceManager {

    const val TAG = "LiteRTServiceManager"
    val initializeTask: Task<Void> by lazy { TfLite.initialize(appContext) }
    private lateinit var interpreter: InterpreterApi
    val vocabulary = loadVocabulary("vocab.json")
    val inverseVocabulary = createInverseVocabulary(vocabulary)

    fun init() {
        initializeTask.addOnSuccessListener {
            val interpreterOption =
                InterpreterApi.Options()
                    .setRuntime(InterpreterApi.Options.TfLiteRuntime.FROM_SYSTEM_ONLY)
            interpreter = InterpreterApi.create(
                loadModelFile("gpt2-64-8bits.tflite"),
                interpreterOption
            )
            Log.i(TAG, "Interpreter initialized")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Cannot initialize interpreter", e)
        }
    }

    /**
     * 从 assets 文件夹中加载词汇表 JSON 文件
     */
    fun loadVocabulary(filePath: String): Map<String, Int> {
        val jsonString = appContext.assets.open(filePath).bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val vocabMap = mutableMapOf<String, Int>()
        jsonObject.keys().forEach { key ->
            vocabMap[key] = jsonObject.getInt(key)
        }
        return vocabMap
    }

    // 假设你还需要一个反向映射来解码
    fun createInverseVocabulary(vocab: Map<String, Int>): Map<Int, String> {
        return vocab.entries.associate { (key, value) -> value to key }
    }

    fun generateText(startText: String, maxOutputLength: Int = 20): String {
        var currentText = startText
        val generatedTokenIds = mutableListOf<Int>()

        for (i in 0 until maxOutputLength) {
            // 1. 运行一次推理，得到下一个 token id
            val nextTokenId = runInference(currentText)

            // 如果模型预测了结束符 (EOS token)，则停止
            // if (nextTokenId == EOS_TOKEN_ID) break

            // 2. 将 id 转换回单词
            val nextWord = inverseVocabulary[nextTokenId] ?: "<unk>"

            // 3. 将新生成的单词追加到当前文本中，用于下一次推理
            currentText += " $nextWord"
            generatedTokenIds.add(nextTokenId)
        }

        return currentText
    }

    fun tokenize(text: String, vocabulary: Map<String, Int>): IntArray {
        // 简单的按空格分割，实际的 Tokenizer 会更复杂 (e.g., BPE)
        val tokens = text.lowercase().split(" ")
        val tokenIds = tokens.map {
            vocabulary[it] ?: 0 // 如果词不在词汇表中，用 0 (UNK) 代替
        }.toIntArray()

        // 填充或截断到固定的序列长度
        val paddedTokenIds = IntArray(20) { 0 } // 用 0 来填充
        tokenIds.copyInto(paddedTokenIds, 0, 0, minOf(tokenIds.size, 20))

        return paddedTokenIds
    }

    const val VOCAB_SIZE = 50257

    fun runInference(inputText: String): Int {
        // 1. Tokenize 并准备输入
        val tokenIds = tokenize(inputText, vocabulary)
        Log.i(TAG, "size: ${tokenIds.size}, tokenIds: ${tokenIds.joinToString(", ")}")
        val inputBuffer = prepareInputBuffer(tokenIds)

        // 2. 准备输出
        // 输出形状: [1, SEQUENCE_LENGTH, VOCAB_SIZE], FLOAT32
        // 我们只关心最后一个 token 的 logits 来预测下一个词
        val outputBuffer = ByteBuffer.allocateDirect(12865792) // 4 bytes for FLOAT32
        outputBuffer.order(ByteOrder.nativeOrder())

        // 3. 运行推理
        interpreter.run(inputBuffer, outputBuffer)

        // 4. 处理输出
        return postprocessOutput(outputBuffer, tokenIds.size)
    }


    fun postprocessOutput(outputBuffer: ByteBuffer, inputLength: Int): Int {
        outputBuffer.rewind()
        val logits = FloatArray(20 * VOCAB_SIZE)
        outputBuffer.asFloatBuffer().get(logits)

        // 我们只关心输入序列最后一个词后面的那个预测
        // 定位到最后一个有效 token 对应的 logits
        // 注意：索引从 0 开始，所以是 inputLength - 1
        val lastTokenLogitsStartIndex = (inputLength - 1) * VOCAB_SIZE

        var maxLogit = -Float.MAX_VALUE
        var predictedNextTokenId = -1

        // 在词汇表中寻找概率最高的 token
        for (i in 0 until VOCAB_SIZE) {
            val currentLogit = logits[lastTokenLogitsStartIndex + i]
            if (currentLogit > maxLogit) {
                maxLogit = currentLogit
                predictedNextTokenId = i
            }
        }

        return predictedNextTokenId
    }


    fun prepareInputBuffer(tokenIds: IntArray): ByteBuffer {
        // 假设输入形状是 [1, SEQUENCE_LENGTH]，数据类型是 INT32
        // INT32 占用 4 字节
        val buffer = ByteBuffer.allocateDirect(256)
        buffer.order(ByteOrder.nativeOrder())

        // 将 IntArray 写入 ByteBuffer
        buffer.asIntBuffer().put(tokenIds)

        return buffer
    }

    /** 将 assets 文件夹中的模型文件加载到内存映射的 ByteBuffer 中 */
    private fun loadModelFile(modelPath: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = appContext.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}