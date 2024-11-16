package com.example.loudsound

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.net.Uri
import android.widget.Button
import android.widget.TextView
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * 主活动类，负责用户界面交互和服务控制
 */
class MainActivity : AppCompatActivity() {
    // 用于显示日志信息的文本视图
    private lateinit var tvLog: TextView
    // 用于调度定期任务的JobScheduler
    //private lateinit var jobScheduler: JobScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化文本视图
        tvLog = findViewById(R.id.tvLog)

        // 设置启动服务按钮的点击监听器
        findViewById<Button>(R.id.btnStartService).setOnClickListener {
            startAudioService()
        }

        // 设置停止服务按钮的点击监听器
        findViewById<Button>(R.id.btnStopService).setOnClickListener {
            stopAudioService()
        }

        // 注册广播接收器来接收服务发送的分贝值更新
        LocalBroadcastManager.getInstance(this).registerReceiver(
            decibelReceiver,
            IntentFilter("DECIBEL_UPDATE")
        )

        // 设置定时任务
        //setupJobScheduler()
    }

//    private fun setupJobScheduler() {
//        jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
//        val componentName = ComponentName(this, AudioServiceKeepAlive::class.java)
//
//        val jobInfo = JobInfo.Builder(1, componentName)
//            .setPersisted(true) // 重启后继续运行
//            .setPeriodic(15 * 60 * 1000L) // 每15分钟检查一次
//            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
//            .build()
//
//        jobScheduler.schedule(jobInfo)
//    }

    /**
     * 启动音频监测服务
     * 根据Android版本选择适当的启动方式
     */
    private fun startAudioService() {
        val serviceIntent = Intent(this, AudioService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0及以上版本需要使用startForegroundService
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        appendLog("服务已启动")
    }

    /**
     * 停止音频监测服务
     */
    private fun stopAudioService() {
        val serviceIntent = Intent(this, AudioService::class.java)
        stopService(serviceIntent)
        appendLog("服务已停止")
    }

    /**
     * 向日志文本框添加带时间戳的消息
     * @param message 要添加的消息内容
     */
    private fun appendLog(message: String) {
        // 生成当前时间戳
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        // 添加带时间戳的消息到日志
        tvLog.append("$timestamp: $message\n")
        // 滚动到日志底部
        (tvLog.parent as ScrollView).fullScroll(ScrollView.FOCUS_DOWN)
    }

    /**
     * 广播接收器，用于接收服务发送的分贝值更新
     */
private val decibelReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // 处理分贝值更新
        intent?.getDoubleExtra("decibel", 0.0)?.let { decibel ->
            appendLog("当前分贝值: %.1f dB".format(decibel))
        }
        
        // 处理音乐播放状态消息
        intent?.getStringExtra("message")?.let { message ->
            appendLog(message)
        }
    }
}

    override fun onDestroy() {
        super.onDestroy()
        // 注销广播接收器，防止内存泄漏
        LocalBroadcastManager.getInstance(this).unregisterReceiver(decibelReceiver)
    }
}