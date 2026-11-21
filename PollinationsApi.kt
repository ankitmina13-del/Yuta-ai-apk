package com.yuta.ai.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Thin client around Pollinations.ai's free, keyless endpoints.
 * No API key, no account, no payment — this is what keeps Yuta AI free to run.
 * Docs: https://github.com/pollinations/pollinations/blob/master/APIDOCS.md
 */
object PollinationsApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    data class ChatMessage(val role: String, val content: String)

    /**
     * Sends the full conversation so far and returns Yuta's reply as plain text.
     * Uses the free, keyless POST endpoint: https://text.pollinations.ai/
     */
    suspend fun chat(history: List<ChatMessage>, systemPrompt: String): Result<String> =
        runCatching {
            val messages = JSONArray()
            messages.put(JSONObject().put("role", "system").put("content", systemPrompt))
            history.forEach {
                messages.put(JSONObject().put("role", it.role).put("content", it.content))
            }

            val body = JSONObject().apply {
                put("messages", messages)
                put("model", "openai")
                put("private", true)
            }.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url("https://text.pollinations.ai/")
                .post(body)
                .build()

            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    throw ApiException(resp.code, "Yuta ki request fail ho gayi (${resp.code}). Thoda ruk ke dobara try karo.")
                }
                val raw = resp.body?.string().orEmpty().trim()
                // The endpoint returns plain text; some deployments wrap it as JSON.
                runCatching { JSONObject(raw).optString("response", raw) }.getOrDefault(raw).ifBlank {
                    "Hmm, kuch samajh nahi aaya. Ek baar phir se poocho?"
                }
            }
        }

    /**
     * Builds a direct image URL from Pollinations' free keyless image endpoint.
     * Loading this URL (e.g. with Coil) generates the image on demand — no download step needed.
     */
    fun imageUrl(
        prompt: String,
        width: Int = 1024,
        height: Int = 1024,
        seed: Long = System.currentTimeMillis() % 1_000_000,
        model: String = "flux"
    ): String {
        val encoded = URLEncoder.encode(prompt, "UTF-8").replace("+", "%20")
        return "https://image.pollinations.ai/prompt/$encoded" +
            "?width=$width&height=$height&seed=$seed&model=$model&nologo=true"
    }

    class ApiException(val code: Int, message: String) : Exception(message)
}
