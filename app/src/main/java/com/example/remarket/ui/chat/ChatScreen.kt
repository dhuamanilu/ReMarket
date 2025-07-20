package com.example.remarket.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.remarket.data.model.Message
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.navigationBarsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // --- ESTRUCTURA ORIGINAL DEL SCAFFOLD RESTAURADA ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.chatHeader, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        // Volvemos a colocar el input en el bottomBar para que quede fijo abajo
        bottomBar = {
            ChatInput(
                value = uiState.currentMessage,
                onValueChange = viewModel::onMessageChanged,
                onSend = viewModel::sendMessage
            )
        }
    ) { paddingValues ->
        // La lista de mensajes ocupa todo el espacio, con el padding correcto para
        // no quedar oculta por el TopBar y el BottomBar.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // El padding es importante aquí
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    val groupedMessages = uiState.messages.groupBy { formatDateHeader(it.timestamp) }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        groupedMessages.forEach { (date, messagesOnDay) ->
                            item {
                                DateSeparator(date = date)
                            }
                            items(messagesOnDay, key = { it.id }) { message ->
                                MessageItem(
                                    message = message,
                                    isSentByCurrentUser = message.senderId == viewModel.getCurrentUserId()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageItem(message: Message, isSentByCurrentUser: Boolean) {
    val horizontalArrangement = if (isSentByCurrentUser) Arrangement.End else Arrangement.Start
    val bubbleColor = if (isSentByCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColor = if (isSentByCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val shape = if (isSentByCurrentUser) {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = horizontalArrangement
    ) {
        Surface(
            shape = shape,
            color = bubbleColor,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatMessageTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSentByCurrentUser) textColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DateSeparator(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        tonalElevation = 4.dp,
        // ↓↓↓ ESTA ES LA ÚNICA LÍNEA QUE NECESITAS AÑADIR ↓↓↓
        modifier = Modifier.navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Escribe un mensaje...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilledIconButton(
                onClick = onSend,
                enabled = value.isNotBlank(),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar")
            }
        }
    }
}

private fun formatDateHeader(date: Date?): String {
    if (date == null) return "Fecha desconocida"
    val messageCalendar = Calendar.getInstance().apply { time = date }
    val now = Calendar.getInstance()

    return when {
        now.get(Calendar.YEAR) == messageCalendar.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == messageCalendar.get(Calendar.DAY_OF_YEAR) -> "Hoy"

        now.get(Calendar.YEAR) == messageCalendar.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - 1 == messageCalendar.get(Calendar.DAY_OF_YEAR) -> "Ayer"

        else -> SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES")).format(date)
    }
}

private fun formatMessageTime(date: Date?): String {
    if (date == null) return ""
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
}