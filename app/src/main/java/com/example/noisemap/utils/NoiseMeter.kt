package com.example.noisemap.utils

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlin.math.log10
import kotlin.math.sqrt

object NoiseMeter {

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun getDecibel(): Int {
        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        val buffer = ShortArray(bufferSize)
        audioRecord.startRecording()
        audioRecord.read(buffer, 0, bufferSize)
        audioRecord.stop()
        audioRecord.release()

        // Calculate RMS and convert to dB
        var sum = 0.0
        for (sample in buffer) {
            sum += sample * sample
        }

        val mean = sum / buffer.size
        val rms = sqrt(mean)
        val db = 20 * log10(rms)

        return db.toInt().coerceIn(0, 120) // Clamp to human hearing range
    }
}