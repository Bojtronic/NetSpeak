package com.securitysoftware.netspeak.speech

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

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

    fun speak(
        text: String,
        onDone: (() -> Unit)? = null
    ) {
        if (!ready || text.isBlank()) return

        val utteranceId = UUID.randomUUID().toString()

        tts?.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {

                override fun onStart(utteranceId: String) {
                    // opcional
                }

                override fun onDone(utteranceId: String) {
                    onDone?.invoke()
                }

                override fun onError(utteranceId: String) {
                    onDone?.invoke()
                }
            }
        )

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            params,
            utteranceId
        )
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
