package com.yuta.ai.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.yuta.ai.data.ChatViewModel
import com.yuta.ai.ui.theme.*
import java.util.Locale

@Composable
fun VoiceScreen(viewModel: ChatViewModel) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Mic dabao aur Yuta se baat karo") }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val tts = remember { TextToSpeech(context) { } }
    DisposableEffect(Unit) {
        onDispose { tts.stop(); tts.shutdown() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasMicPermission = granted }

    val recognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) SpeechRecognizer.createSpeechRecognizer(context) else null
    }

    fun startListening() {
        if (recognizer == null) {
            statusText = "Is device par voice recognition available nahi hai."
            return
        }
        val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle?) { statusText = "Sun rahi hu…" }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false; statusText = "Soch rahi hu…" }
            override fun onError(error: Int) {
                isListening = false
                statusText = "Kuch sunai nahi diya, phir se try karo."
            }
            override fun onResults(results: android.os.Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!text.isNullOrBlank()) {
                    viewModel.sendMessage(text)
                }
            }
            override fun onPartialResults(partialResults: android.os.Bundle?) {}
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        })
        recognizer.startListening(intent)
        isListening = true
    }

    // Speak the latest assistant reply aloud
    LaunchedEffect(viewModel.messages.size, viewModel.isTyping.value) {
        val last = viewModel.messages.lastOrNull()
        if (!viewModel.isTyping.value && last != null && last.role == "assistant" && !last.isError) {
            statusText = last.text
            tts.speak(last.text, TextToSpeech.QUEUE_FLUSH, null, "yuta_reply")
        }
    }

    DisposableEffect(Unit) {
        onDispose { recognizer?.destroy() }
    }

    Scaffold(containerColor = YutaBackground) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Voice chat", style = MaterialTheme.typography.headlineMedium, color = YutaTextPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = YutaTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(if (isListening) YutaBrandGradient else androidx.compose.ui.graphics.Brush.linearGradient(listOf(YutaSurfaceElevated, YutaSurfaceElevated)))
                    .clickable {
                        if (!hasMicPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else if (!isListening) {
                            startListening()
                        } else {
                            recognizer?.stopListening()
                            isListening = false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                    contentDescription = "Mic",
                    tint = if (isListening) Color.White else YutaAccentViolet,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(Modifier.height(32.dp))
            if (!hasMicPermission) {
                Text("Mic permission allow karo baat karne ke liye", color = YutaTextMuted, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
