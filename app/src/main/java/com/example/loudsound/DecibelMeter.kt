package com.example.loudsound

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt

/**
 * 分贝计量器类，负责音频采集和分贝值计算
 */
class DecibelMeter(private val context: Context) {
    // 音频录制器实例
    private var audioRecord: AudioRecord? = null
    // 录制状态标志
    private var isRecording = false

    // 音频采样参数
    private val sampleRate = 44100 // 采样率
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO // 单声道
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT // 16位PCM编码
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    /**
     * 启动音频录制
     * 需要RECORD_AUDIO权限
     */
    fun start() {
        // 检查录音权限
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // 初始化AudioRecord实例
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        // 开始录音
        audioRecord?.startRecording()
        isRecording = true
    }

    /**
     * 获取当前环境的分贝值
     * @return 计算得到的分贝值
     */
    fun getDecibel(): Double {
        if (!isRecording || audioRecord == null) return -1.0

        // 读取音频数据
        val buffer = ShortArray(bufferSize)
        val read = audioRecord?.read(buffer, 0, bufferSize) ?: 0
        if (read <= 0) return 0.0

        // 计算均方根值
        var sum = 0.0
        for (sample in buffer) {
            sum += sample * sample
        }

        // 计算有效值（RMS）
        val rms = sqrt(sum / buffer.size)

        // 计算分贝值
        // 使用16位音频的最大值(32767)作为参考值
        val db = 20 * log10(rms / 32767)

        // 将分贝值调整到更易读的范围（通常在0-120dB之间）
        return max(0.0, db + 90)
    }

    /**
     * 停止录音并释放资源
     */
    fun stop() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}