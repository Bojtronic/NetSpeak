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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.securitysoftware.netspeak.data.repository.BranchRepository
import com.securitysoftware.netspeak.login.LoginActivity
import com.securitysoftware.netspeak.sound.MicSoundPlayer
import com.securitysoftware.netspeak.speech.TextToSpeechManager


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



@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun NetSpeakMainScreen() {

    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var networkResult by remember {
        mutableStateOf("Presione el micrófono y diga el nombre de la red")
    }
    val scale by animateFloatAsState(
        targetValue = if (isListening) 1.3f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "MicScale"
    )
    val repository = remember {
        BranchRepository(context)
    }
    val ttsManager = remember {
        TextToSpeechManager(context)
    }
    val speechManager = remember {
        SpeechManager(
            context = context,
            onResult = { text ->
                recognizedText = text

                val devices = repository.findDevicesByBranchName(text)

                networkResult = formatDevices(devices)

                // 🔊 HABLAR RESULTADO
                ttsManager.speak(
                    formatDevicesForSpeech(devices)
                )
            },
            onError = { error ->
                networkResult = "Error de reconocimiento. Intente nuevamente."
                ttsManager.speak("Error de reconocimiento. Intente nuevamente.")
            }
        )
    }

    val micSoundPlayer = remember {
        MicSoundPlayer(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NetSpeak") },
                actions = {
                    val context = LocalContext.current

                    IconButton(onClick = {
                        val intent = Intent(context, LoginActivity::class.java)
                        context.startActivity(intent)
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

            Text(
                text = "Texto reconocido:",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .padding(12.dp)
            ) {
                Text(text = recognizedText.ifEmpty { "—" })
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Resultado:",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE3F2FD))
                    .padding(12.dp)
            ) {
                Text(
                    text = networkResult,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            FloatingActionButton(
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .pointerInteropFilter { event ->
                        when (event.action) {

                            MotionEvent.ACTION_DOWN -> {
                                isListening = true
                                networkResult = "Escuchando..."
                                micSoundPlayer.playMicOn()
                                speechManager.startListening()
                                true
                            }

                            MotionEvent.ACTION_UP,
                            MotionEvent.ACTION_CANCEL -> {
                                isListening = false
                                micSoundPlayer.playMicOff()
                                speechManager.stopListening()
                                true
                            }

                            else -> false
                        }
                    },
                containerColor = if (isListening)
                    Color.Red
                else
                    MaterialTheme.colorScheme.primary,
                onClick = {} // obligatorio pero vacío
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Micrófono",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    DisposableEffect(Unit) {
        onDispose {
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
