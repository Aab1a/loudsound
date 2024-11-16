package com.example.loudsound

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.File

/**
 * 音频监测服务类，负责在后台持续监测环境音量
 */
class AudioService : Service() {
    // 服务运行状态标志
    private var isRunning = false
    // 通知相关常量
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "AudioServiceChannel"
    // 用于定时任务的Handler
    private lateinit var handler: Handler
    // 分贝计量器实例
    private lateinit var decibelMeter: DecibelMeter
    // 媒体播放器
    private var mediaPlayer: MediaPlayer? = null
    // 是否正在播放音乐
    private var isPlaying = false
    // 分贝阈值
    private val DECIBEL_THRESHOLD = 50.0

    override fun onCreate() {
        super.onCreate()
        // 创建通知渠道（Android 8.0及以上需要）
        createNotificationChannel()
        // 初始化Handler，使用主线程的Looper
        handler = Handler(Looper.getMainLooper())
        // 初始化分贝计量器
        decibelMeter = DecibelMeter(this)
        // 初始化MediaPlayer
        initMediaPlayer()
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 启动前台服务并开始录音
        startForeground(NOTIFICATION_ID, createNotification())
        startAudioRecording()
        // 服务被系统杀死后自动重启
        return START_STICKY
    }

    /**
     * 创建通知渠道
     * 仅在Android 8.0及以上版本需要
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "音频监测服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于监测环境音量"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 创建前台服务通知
     */
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("音频监测服务运行中")
            .setContentText("正在监测环境音量")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * 初始化媒体播放器
     */
    private fun initMediaPlayer() {
        try {
            val musicFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "early_riser.mp3"
            )
            if (musicFile.exists()) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(musicFile.absolutePath)
                    prepare()
                    isLooping = true // 循环播放
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            appendLog("音乐文件加载失败: ${e.message}")
        }
    }

    /**
     * 开始音频录制和分贝值监测
     */
    private fun startAudioRecording() {
        if (isRunning) return
        isRunning = true

        // 启动分贝计量器
        decibelMeter.start()

        // 每15分钟获取一次分贝值
        val INTERVAL = 15 * 60 * 1000L // 15分钟转换为毫秒
        
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isRunning) {
                    val decibel = decibelMeter.getDecibel()
                    broadcastDecibel(decibel)
                    // 检查分贝值并控制音乐播放
                    checkDecibelAndPlayMusic(decibel)
                    // 延迟15分钟后再次执行
                    handler.postDelayed(this, INTERVAL)
                }
            }
        }, 0)
    }

    /**
     * 广播分贝值更新
     * @param decibel 当前测得的分贝值
     */
    private fun broadcastDecibel(decibel: Double) {
        val intent = Intent("DECIBEL_UPDATE")
        intent.putExtra("decibel", decibel)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /**
     * 检查分贝值并控制音乐播放
     * @param decibel 当前分贝值
     */
    private fun checkDecibelAndPlayMusic(decibel: Double) {
        if (decibel > DECIBEL_THRESHOLD) {
            if (!isPlaying) {
                startMusic()
            }
        } else {
            if (isPlaying) {
                stopMusic()
            }
        }
    }

    /**
     * 开始播放音乐
     */
    private fun startMusic() {
        mediaPlayer?.let {
            try {
                if (!it.isPlaying) {
                    it.start()
                    isPlaying = true
                    appendLog("开始播放音乐")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                appendLog("音乐播放失败: ${e.message}")
            }
        }
    }

    /**
     * 停止播放音乐
     */
    private fun stopMusic() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.pause()
                    isPlaying = false
                    appendLog("停止播放音乐")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                appendLog("音乐停止失败: ${e.message}")
            }
        }
    }

    /**
     * 添加日志
     */
    private fun appendLog(message: String) {
        val intent = Intent("DECIBEL_UPDATE")
        intent.putExtra("message", message)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止服务时清理资源
        isRunning = false
        decibelMeter.stop()
        handler.removeCallbacksAndMessages(null)
        // 释放MediaPlayer资源
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}