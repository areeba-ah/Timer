package com.example.timer

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timer.ui.theme.TimerTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimerTheme {
                TimerApp()
            }
        }
    }
}


@Composable
fun TimerApp() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFFE0E0E0),
            onPrimary = Color(0xFF000000),
            secondary = Color(0xFFE0E0E0),
            onSecondary = Color(0xFF000000),
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primary
        ) {
            StopwatchScreen()
        }
    }
}

@Composable
fun StopwatchScreen() {
    val context = LocalContext.current
    var time by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    val history = remember { mutableStateListOf<Long>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        history.addAll(loadSessionHistory(context))
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning) {
                delay(1000L)
                time += 1L
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatTime(time),
            fontSize = 48.sp,
            modifier = Modifier.padding(16.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NeumorphicButton(
                text = if (isRunning) "Pause" else "Start",
                onClick = { isRunning = !isRunning }
            )
            NeumorphicButton(
                text = "Reset",
                onClick = {
                    isRunning = false
                    time = 0L
                }
            )
            NeumorphicButton(
                text = "Stop",
                onClick = {
                    if (time != 0L) {
                        isRunning = false
                        history.add(time)
                        saveSessionHistory(context, time)
                        time = 0L
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HistoricalDataScreen(history)
    }
}

fun formatTime(time: Long): String {
    val hours = time / 3600
    val minutes = (time % 3600) / 60
    val seconds = time % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

@Composable
fun NeumorphicButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(text = text)
    }
}

@Composable
fun HistoricalDataScreen(history: MutableList<Long>) {
    val context = LocalContext.current

    LazyColumn {
        items(history) { sessionTime ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(sessionTime),
                    fontSize = 24.sp
                )
                IconButton(
                    onClick = {
                        history.remove(sessionTime)
                        saveUpdatedHistory(context, history)
                    }
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                }
            }
        }
    }
}

fun saveSessionHistory(context: Context, sessionTime: Long) {
    val sharedPref = context.getSharedPreferences("timer_history", Context.MODE_PRIVATE)
    val historyList = loadSessionHistory(context).toMutableList()
    historyList.add(sessionTime)

    with(sharedPref.edit()) {
        putString("history", historyList.joinToString(","))
        apply()
    }
}

fun saveUpdatedHistory(context: Context, history: List<Long>) {
    val sharedPref = context.getSharedPreferences("timer_history", Context.MODE_PRIVATE)

    with(sharedPref.edit()) {
        putString("history", history.joinToString(",")) // Save as a comma-separated string
        apply()
    }
}

fun loadSessionHistory(context: Context): List<Long> {
    val sharedPref = context.getSharedPreferences("timer_history", Context.MODE_PRIVATE)

    return try {
        sharedPref.getString("history", "")?.split(",")?.mapNotNull { it.toLongOrNull() } ?: listOf()
    } catch (e: ClassCastException) {
        // Handle old Set<String> format and migrate to new format
        val oldSet = sharedPref.getStringSet("history", null)
        val historyList = oldSet?.mapNotNull { it.toLongOrNull() } ?: listOf()

        // Save in new format
        with(sharedPref.edit()) {
            putString("history", historyList.joinToString(","))
            remove("history") // Remove old format
            apply()
        }

        historyList
    }
}

@Preview(showBackground = true)
@Composable
fun StopwatchPreview() {
    TimerApp()
}