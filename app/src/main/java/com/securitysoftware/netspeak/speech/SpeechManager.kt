package com.securitysoftware.netspeak.speech

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class SpeechManager(
    private val context: Context,
    private val onCommandDetected: (String) -> Unit,
    private val onError: (String) -> Unit
) {

    private val speechRecognizer: SpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(context)

    private val handler = Handler(Looper.getMainLooper())

    private var wakeWordDetected = false
    private var isCoolingDown = false
    private var isListening = false

    private val listenDurationMs = 5_000L
    private val cooldownDurationMs = 20_000L
    private val wakeWord = "net"

    private val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
    }

    init {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {

            override fun onResults(results: android.os.Bundle?) {
                val matches = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                val text = matches?.firstOrNull()?.lowercase()?.trim()

                if (!text.isNullOrEmpty()) {
                    processText(text)
                }

                stopListeningInternal()
                scheduleNextCycle()
            }

            override fun onError(error: Int) {
                handleSpeechError(error)
                stopListeningInternal()
                scheduleNextCycle()
            }

            override fun onReadyForSpeech(params: android.os.Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: android.os.Bundle?) {}
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        })
    }

    private fun processText(text: String) {
        if (!wakeWordDetected) {
            if (text.contains(wakeWord)) {
                wakeWordDetected = true
            }
        } else {
            onCommandDetected(text)
            wakeWordDetected = false
            startCooldown()
        }
    }

    private fun startCooldown() {
        isCoolingDown = true
        handler.postDelayed({
            isCoolingDown = false
        }, cooldownDurationMs)
    }

    fun start() {
        if (isListening) return
        scheduleNextCycle()
    }

    private fun scheduleNextCycle() {
        if (isCoolingDown) return

        handler.postDelayed({
            startListeningInternal()
        }, 500)
    }

    private fun startListeningInternal() {
        if (isListening || isCoolingDown) return

        try {
            isListening = true
            speechRecognizer.startListening(recognizerIntent)

            handler.postDelayed({
                stopListeningInternal()
            }, listenDurationMs)

        } catch (e: Exception) {
            isListening = false
            onError("Error iniciando reconocimiento: ${e.message}")
        }
    }

    private fun stopListeningInternal() {
        if (!isListening) return

        try {
            speechRecognizer.stopListening()
        } catch (_: Exception) {
        } finally {
            isListening = false
        }
    }

    private fun handleSpeechError(error: Int) {
        when (error) {
            SpeechRecognizer.ERROR_NO_MATCH,
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                // Silencio o nada reconocido → ignorar
            }

            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_SERVER,
            SpeechRecognizer.ERROR_CLIENT -> {
                onError("Error de reconocimiento ($error)")
            }

            else -> {
                onError("Error desconocido ($error)")
            }
        }
    }

    fun destroy() {
        handler.removeCallbacksAndMessages(null)
        speechRecognizer.destroy()
    }
}
