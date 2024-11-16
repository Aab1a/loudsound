package com.example.loudsound

import android.app.ActivityManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * 服务保活JobService
 * 定期检查AudioService是否在运行，如果不在运行则重新启动
 */
class AudioServiceKeepAlive : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        // 检查服务是否在运行，如果没有则启动
        if (!isServiceRunning(AudioService::class.java)) {
            val intent = Intent(applicationContext, AudioService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true // 返回true表示需要重新调度这个任务
    }

    /**
     * 检查指定服务是否正在运行
     * @param serviceClass 要检查的服务类
     * @return 如果服务正在运行返回true，否则返回false
     */
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}