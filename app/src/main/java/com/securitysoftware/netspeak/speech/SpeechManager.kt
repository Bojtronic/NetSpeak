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
    private val onStateChanged: (ListeningState) -> Unit,
    private val onCommandDetected: (String) -> Unit,
    private val onError: (String) -> Unit
) {

    private val speechRecognizer: SpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(context)

    private val handler = Handler(Looper.getMainLooper())

    // ─────────────── STATE ───────────────
    private var wakeWordDetected = false
    private var isListening = false
    private var isCoolingDown = false
    private var isSpeaking = false


    // ─────────────── CONFIG ───────────────
    private val listenDurationMs = 5_000L
    private val cooldownDurationMs = 20_000L
    private val wakeWord = "net"

    private val wakeWordVariants = setOf(
        "net",
        "ne",
        "met",
        "nex",
        "nec",
        "nek"
    )

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
                val matches =
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                val text = matches
                    ?.firstOrNull()
                    ?.lowercase()
                    ?.trim()

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

    private fun containsWakeWord(text: String): Boolean {
        val words = text
            .lowercase()
            .split("\\s+".toRegex())

        return words.any { word ->
            word.length in 2..3 &&
                    word.contains("e") &&
                    word in wakeWordVariants
        }
    }

    // ─────────────── CORE LOGIC ───────────────
    private fun processText(text: String) {
        if (!wakeWordDetected) {
            if (containsWakeWord(text)) {
                wakeWordDetected = true
                onStateChanged(ListeningState.LISTENING_COMMAND)
            }
        } else {
            //onStateChanged(ListeningState.SPEAKING)
            onCommandDetected(text)
            wakeWordDetected = false
            startCooldown()
        }
    }

    // ─────────────── CYCLE CONTROL ───────────────
    fun start() {
        if (isListening) return
        onStateChanged(ListeningState.LISTENING_HOTWORD)
        scheduleNextCycle()
    }

    private fun scheduleNextCycle() {
        if (isCoolingDown || isSpeaking) return

        handler.postDelayed({
            startListeningInternal()
        }, 500)
    }

    private fun startListeningInternal() {
        if (isListening || isCoolingDown || isSpeaking) return

        isListening = true

        onStateChanged(
            if (wakeWordDetected)
                ListeningState.LISTENING_COMMAND
            else
                ListeningState.LISTENING_HOTWORD
        )

        speechRecognizer.startListening(recognizerIntent)
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

    // ─────────────── COOLDOWN ───────────────
    private fun startCooldown() {
        isCoolingDown = true

        handler.postDelayed({
            isCoolingDown = false
            scheduleNextCycle() // 👈 REANUDA ESCUCHA
        }, cooldownDurationMs)
    }


    // ─────────────── ERROR HANDLING ───────────────
    private fun handleSpeechError(error: Int) {
        onStateChanged(ListeningState.IDLE)
        onError("Error de reconocimiento ($error)")
    }

    // ─────────────── CLEANUP ───────────────
    fun destroy() {
        handler.removeCallbacksAndMessages(null)
        speechRecognizer.destroy()
    }

    fun onSpeakingStarted() {
        isSpeaking = true
    }

    fun onSpeakingFinished() {
        isSpeaking = false
        scheduleNextCycle()
    }

}
