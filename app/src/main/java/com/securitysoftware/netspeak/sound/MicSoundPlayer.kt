package com.securitysoftware.netspeak.sound

import android.content.Context
import android.media.SoundPool
import com.securitysoftware.netspeak.R

class MicSoundPlayer(context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .build()

    private val micOnSound = soundPool.load(context, R.raw.mic_on, 1)
    private val micOffSound = soundPool.load(context, R.raw.mic_off, 1)

    fun playMicOn() {
        soundPool.play(micOnSound, 1f, 1f, 1, 0, 1f)
    }

    fun playMicOff() {
        soundPool.play(micOffSound, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}