package com.yuta.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yuta.ai.data.ChatViewModel
import com.yuta.ai.ui.screens.ChatScreen
import com.yuta.ai.ui.screens.ImageScreen
import com.yuta.ai.ui.screens.VoiceScreen
import com.yuta.ai.ui.theme.YutaBackground
import com.yuta.ai.ui.theme.YutaSurface
import com.yuta.ai.ui.theme.YutaTextMuted
import com.yuta.ai.ui.theme.YutaTheme

private enum class YutaTab(val label: String) { CHAT("Chat"), CREATE("Create"), VOICE("Voice") }

class MainActivity : ComponentActivity() {
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YutaTheme {
                YutaRoot(chatViewModel)
            }
        }
    }
}

@Composable
private fun YutaRoot(viewModel: ChatViewModel) {
    var selectedTab by remember { mutableStateOf(YutaTab.CHAT) }

    Scaffold(
        containerColor = YutaBackground,
        bottomBar = {
            NavigationBar(containerColor = YutaSurface, tonalElevation = 0.dp) {
                NavigationBarItem(
                    selected = selectedTab == YutaTab.CHAT,
                    onClick = { selectedTab = YutaTab.CHAT },
                    icon = { Icon(Icons.Filled.Chat, contentDescription = "Chat") },
                    label = { Text("Chat") },
                    colors = yutaNavColors()
                )
                NavigationBarItem(
                    selected = selectedTab == YutaTab.CREATE,
                    onClick = { selectedTab = YutaTab.CREATE },
                    icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = "Create") },
                    label = { Text("Create") },
                    colors = yutaNavColors()
                )
                NavigationBarItem(
                    selected = selectedTab == YutaTab.VOICE,
                    onClick = { selectedTab = YutaTab.VOICE },
                    icon = { Icon(Icons.Filled.Mic, contentDescription = "Voice") },
                    label = { Text("Voice") },
                    colors = yutaNavColors()
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            when (selectedTab) {
                YutaTab.CHAT -> ChatScreen(viewModel)
                YutaTab.CREATE -> ImageScreen()
                YutaTab.VOICE -> VoiceScreen(viewModel)
            }
        }
    }
}

@Composable
private fun yutaNavColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = androidx.compose.ui.graphics.Color.White,
    selectedTextColor = androidx.compose.ui.graphics.Color.White,
    unselectedIconColor = YutaTextMuted,
    unselectedTextColor = YutaTextMuted,
    indicatorColor = com.yuta.ai.ui.theme.YutaAccentViolet
)
