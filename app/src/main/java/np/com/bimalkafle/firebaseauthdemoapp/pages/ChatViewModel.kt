package np.com.bimalkafle.firebaseauthdemoapp.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ChatViewModel : ViewModel() {

    private val _messageList = MutableStateFlow<List<MessageModel>>(emptyList())
    val messageList = _messageList.asStateFlow()

    private val client = OkHttpClient()
    private val apiKey = "" // TODO: Replace with your actual key

    fun sendMessage(userMessage: String) {
        // Add user message to the list first (locally)
        val currentMessages = _messageList.value.toMutableList()
        currentMessages.add(MessageModel(role = "user", message = userMessage))
        _messageList.value = currentMessages

        // Prepare full conversation history
        val contentsArray = currentMessages.joinToString(separator = ",") { message ->
            """
        {
          "role": "${message.role}",
          "parts": [{ "text": "${message.message.replace("\"", "\\\"")}" }]
        }
        """
        }

        val jsonBody = """
        {
          "contents": [
            $contentsArray
          ]
        }
    """.trimIndent()

        val mediaType = "application/json".toMediaType()
        val body = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val reply = parseGeminiResponse(responseBody)
                        val updatedMessages = _messageList.value.toMutableList()
                        updatedMessages.add(MessageModel(role = "model", message = reply))
                        _messageList.value = updatedMessages
                    } else {
                        println("Request failed: ${response.code} - ${response.message}")
                    }
                }
            } catch (e: IOException) {
                println("Network error: ${e.localizedMessage}")
            }
        }
    }


    private fun parseGeminiResponse(responseBody: String?): String {
        if (responseBody == null) return "No response"

        return try {
            val json = JSONObject(responseBody)
            val text = json
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
            text
        } catch (e: Exception) {
            "Error parsing response"
        }
    }
}

