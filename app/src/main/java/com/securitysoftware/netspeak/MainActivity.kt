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
import androidx.compose.ui.ExperimentalComposeUiApi
import com.securitysoftware.netspeak.speech.SpeechManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import com.securitysoftware.netspeak.data.repository.BranchRepository
import com.securitysoftware.netspeak.login.LoginActivity
import com.securitysoftware.netspeak.sound.MicSoundPlayer
import com.securitysoftware.netspeak.speech.TextToSpeechManager
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.RecordVoiceOver
import com.securitysoftware.netspeak.data.model.Branch
import com.securitysoftware.netspeak.data.model.Device
import com.securitysoftware.netspeak.speech.ListeningState

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

fun formatDevices(
    branch: Branch,
    devices: List<Device>
): String {
    if (devices.isEmpty()) {
        return "No se encontró información para la sucursal ${branch.name}."
    }

    val builder = StringBuilder()
    builder.append("Sucursal: ${branch.name}\n\n")

    devices.forEach { device ->
        builder.append("${device.name}: ${device.ip}\n")
    }

    return builder.toString()
}


fun formatDevicesForSpeech(
    branch: Branch,
    devices: List<Device>
): String {
    if (devices.isEmpty()) {
        return "No se encontró información para la sucursal ${branch.name}."
    }

    val builder = StringBuilder()

    builder.append("Información de la sucursal ${branch.name}. ")

    devices.forEach { device ->
        builder.append(
            "${device.name}, dirección IP ${device.ip.replace(".", " punto ")}. "
        )
    }

    return builder.toString()
}


@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun NetSpeakMainScreen() {

    val context = LocalContext.current

    // ─────────────── UI STATE ───────────────
    var listeningState: com.securitysoftware.netspeak.speech.ListeningState by remember {
        mutableStateOf(ListeningState.IDLE)
    }

    var recognizedText by remember { mutableStateOf("") }
    var networkResult by remember {
        mutableStateOf("Diga \"net\" para activar el micrófono")
    }

    val scale by animateFloatAsState(
        targetValue = if (listeningState != ListeningState.IDLE) 1.3f else 1f,
        animationSpec = tween(150),
        label = "StateIconScale"
    )

    // ─────────────── SERVICES ───────────────
    val repository = remember { BranchRepository(context) }
    val ttsManager = remember { TextToSpeechManager(context) }
    val micSoundPlayer = remember { MicSoundPlayer(context) }

    // ─────────────── SPEECH MANAGER ───────────────
    fun handleCommand(
        text: String,
        onSpeakingStarted: () -> Unit,
        onSpeakingFinished: () -> Unit
    ) {
        val result = repository.findBranchWithDevicesBySpokenText(text)

        listeningState = ListeningState.SPEAKING
        onSpeakingStarted()

        if (result == null) {
            recognizedText = text
            networkResult =
                "No se encontró información para la sucursal $text. Por favor, intente nuevamente"

            ttsManager.speak(
                networkResult,
                onDone = {
                    onSpeakingFinished()
                    listeningState = ListeningState.LISTENING_HOTWORD
                }
            )
            return
        }

        recognizedText = result.branch.name
        networkResult = formatDevices(result.branch, result.devices)

        ttsManager.speak(
            formatDevicesForSpeech(result.branch, result.devices),
            onDone = {
                onSpeakingFinished()
                listeningState = ListeningState.LISTENING_HOTWORD
            }
        )
    }


    val speechManagerRef = remember { mutableStateOf<SpeechManager?>(null) }

    val speechManager = remember {
        SpeechManager(
            context = context,

            onStateChanged = { state ->
                listeningState = state
                when (state) {
                    ListeningState.LISTENING_COMMAND -> micSoundPlayer.playMicOn()
                    ListeningState.SPEAKING -> micSoundPlayer.playMicOff()
                    else -> {}
                }
            },

            onCommandDetected = { text ->
                handleCommand(
                    text = text,
                    onSpeakingStarted = {
                        speechManagerRef.value?.onSpeakingStarted()
                    },
                    onSpeakingFinished = {
                        speechManagerRef.value?.onSpeakingFinished()
                    }
                )
            },

            onError = {}
        )
    }.also {
        speechManagerRef.value = it
    }


    // ─────────────── LIFECYCLE ───────────────
    LaunchedEffect(Unit) {
        //listeningState = ListeningState.LISTENING_HOTWORD
        speechManager.start()
    }

    DisposableEffect(Unit) {
        onDispose {
            speechManager.destroy()
            ttsManager.shutdown()
            micSoundPlayer.release()
        }
    }

    // ─────────────── UI ───────────────
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

            // ───────── ICONO ÚNICO SEGÚN ESTADO ─────────
            when (listeningState) {

                ListeningState.LISTENING_HOTWORD -> {
                    StateIcon(Icons.Default.Mic, scale, Color.Blue)
                }

                ListeningState.LISTENING_COMMAND -> {
                    StateIcon(Icons.Default.Hearing, scale, Color.Green)
                }

                ListeningState.SPEAKING -> {
                    StateIcon(Icons.Default.RecordVoiceOver, scale, Color.Red)
                }

                ListeningState.IDLE -> {
                    // Sin icono
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StateIcon(
    icon: ImageVector,
    scale: Float,
    color: Color
) {
    FloatingActionButton(
        modifier = Modifier
            .size(72.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        containerColor = color,
        onClick = {}
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = Color.White
        )
    }
}



@Preview(showBackground = true)
@Composable
fun NetSpeakPreview() {
    NetSpeakTheme {
        NetSpeakMainScreen()
    }
}
