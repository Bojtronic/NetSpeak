package com.securitysoftware.netspeak

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.securitysoftware.netspeak.ui.theme.NetSpeakTheme
import android.view.MotionEvent
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.ExperimentalComposeUiApi
import com.securitysoftware.netspeak.speech.SpeechManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import android.Manifest
import android.content.pm.PackageManager
import android.speech.SpeechRecognizer
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.securitysoftware.netspeak.data.repository.BranchRepository
import com.securitysoftware.netspeak.login.LoginActivity
import com.securitysoftware.netspeak.sound.MicSoundPlayer
import com.securitysoftware.netspeak.speech.TextToSpeechManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



class MainActivity : ComponentActivity() {

    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                println("Permiso de micrófono denegado")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            NetSpeakTheme {
                NetSpeakMainScreen()
            }
        }
    }


}

fun formatDevices(devices: List<com.securitysoftware.netspeak.data.model.Device>): String {
    if (devices.isEmpty()) {
        return "No se encontró información para esa sucursal.\nIntente nuevamente."
    }

    val builder = StringBuilder()

    devices.forEach { device ->
        builder.append("${device.name}: ${device.ip}\n")
    }

    return builder.toString()
}

fun formatDevicesForSpeech(
    devices: List<com.securitysoftware.netspeak.data.model.Device>
): String {
    if (devices.isEmpty()) {
        return "No se encontró información para esa sucursal. Intente nuevamente."
    }

    val builder = StringBuilder()

    devices.forEach { device ->
        builder.append("${device.name}, dirección IP ${device.ip.replace(".", " punto ")}. ")
    }

    return builder.toString()
}

fun isFatalError(error: Int): Boolean =
    error == SpeechRecognizer.ERROR_NETWORK ||
            error == SpeechRecognizer.ERROR_SERVER

fun isIgnorableError(error: Int): Boolean =
    error == SpeechRecognizer.ERROR_CLIENT ||
            error == SpeechRecognizer.ERROR_NO_MATCH ||
            error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT

enum class SpeechState {
    IDLE,        // No escucha
    KEYWORD,     // Esperando palabra clave
    COMMAND,     // Escuchando comando
    PROCESSING,  // Procesando resultado
    SPEAKING     // TTS activo
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun NetSpeakMainScreen() {

    val keyWord_ = "net"
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ─────────────────── UI STATE ───────────────────
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var networkResult by remember {
        mutableStateOf("Diga \"net\" para activar el micrófono")
    }

    val scale by animateFloatAsState(
        targetValue = if (isListening) 1.3f else 1f,
        animationSpec = tween(150),
        label = "MicScale"
    )

    // ─────────────────── SPEECH STATE ───────────────────
    var speechState by remember { mutableStateOf(SpeechState.KEYWORD) }
    var commandTimeoutJob by remember { mutableStateOf<Job?>(null) }
    var isSpeechActive by remember { mutableStateOf(false) }

    // ─────────────────── SERVICES ───────────────────
    val repository = remember { BranchRepository(context) }
    val ttsManager = remember { TextToSpeechManager(context) }
    val micSoundPlayer = remember { MicSoundPlayer(context) }

    // ─────────────────── SPEECH MANAGER ───────────────────
    var speechManagerRef by remember { mutableStateOf<SpeechManager?>(null) }

    val speechManager = remember {
        SpeechManager(
            context = context,
            onResult = { text ->
                when (speechState) {
                    SpeechState.KEYWORD -> {
                        if (text.contains(keyWord_, ignoreCase = true)) {

                            // 🔥 ahora sí es seguro
                            speechManagerRef?.stop()
                            isSpeechActive = false

                            speechState = SpeechState.COMMAND
                            isListening = true
                            micSoundPlayer.playMicOn()

                            commandTimeoutJob?.cancel()
                            commandTimeoutJob = scope.launch {
                                delay(5_000)
                                if (speechState == SpeechState.COMMAND) {
                                    isListening = false
                                    micSoundPlayer.playMicOff()
                                    speechState = SpeechState.KEYWORD
                                }
                            }
                        } else {
                            isSpeechActive = false
                            speechState = SpeechState.KEYWORD
                        }
                    }

                    SpeechState.COMMAND -> { /* igual que antes */ }
                    else -> Unit
                }
            },

            onError = { errorCode ->
                commandTimeoutJob?.cancel()
                commandTimeoutJob = null

                isListening = false
                micSoundPlayer.playMicOff()

                // 🔥 CLAVE
                isSpeechActive = false

                if (isIgnorableError(errorCode)) {
                    speechState = SpeechState.KEYWORD
                } else {
                    speechState = SpeechState.IDLE
                    networkResult = "Error de reconocimiento"
                    ttsManager.speak("Error de reconocimiento")
                }
            }
        )
    }


    // ─────────────────── START KEYWORD LISTENING ───────────────────
    LaunchedEffect(speechManager) {
        speechManagerRef = speechManager
    }

    LaunchedEffect(speechState) {
        when (speechState) {

            SpeechState.KEYWORD,
            SpeechState.COMMAND -> {
                if (!isSpeechActive) {
                    speechManager.start()
                    isSpeechActive = true
                }
            }

            SpeechState.SPEAKING,
            SpeechState.PROCESSING,
            SpeechState.IDLE -> {
                if (isSpeechActive) {
                    speechManager.stop()
                    isSpeechActive = false
                }
            }
        }
    }



    // ─────────────────── UI ───────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NetSpeak") },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(
                            Intent(context, LoginActivity::class.java)
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Login"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Texto reconocido:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .padding(12.dp)
            ) {
                Text(recognizedText.ifEmpty { "—" })
            }

            Spacer(Modifier.height(24.dp))

            Text("Resultado:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE3F2FD))
                    .padding(12.dp)
            ) {
                Text(networkResult, color = Color.Black)
            }

            Spacer(Modifier.weight(1f))

            FloatingActionButton(
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                containerColor = if (isListening) Color.Red
                else MaterialTheme.colorScheme.primary,
                onClick = {} // solo decorativo
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Micrófono",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ─────────────────── CLEANUP ───────────────────
    DisposableEffect(Unit) {
        onDispose {
            commandTimeoutJob?.cancel()

            if (isSpeechActive) {
                speechManager.stop()
            }

            speechManager.destroy()
            ttsManager.shutdown()
            micSoundPlayer.release()
        }
    }
}




@Preview(showBackground = true)
@Composable
fun NetSpeakPreview() {
    NetSpeakTheme {
        NetSpeakMainScreen()
    }
}
