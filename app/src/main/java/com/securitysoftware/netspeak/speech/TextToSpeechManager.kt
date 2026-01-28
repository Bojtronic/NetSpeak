package com.securitysoftware.netspeak.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechManager(
    context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("es", "ES")
            ready = true
        }
    }

    fun speak(text: String) {
        if (!ready || text.isBlank()) return

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "NetSpeakTTS"
        )
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
