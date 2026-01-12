package com.musicplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.musicplayer.MainActivity
import com.musicplayer.R
import com.musicplayer.model.Music
import com.musicplayer.player.MusicPlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 音乐播放服务
 * 在后台持续播放音乐
 */
class MusicPlaybackService : Service() {

    private lateinit var playerManager: MusicPlayerManager
    private val binder = MusicBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var positionUpdateJob: Job? = null

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "music_playback_channel"
        const val ACTION_PLAY_PAUSE = "com.musicplayer.PLAY_PAUSE"
        const val ACTION_NEXT = "com.musicplayer.NEXT"
        const val ACTION_PREVIOUS = "com.musicplayer.PREVIOUS"
    }

    inner class MusicBinder : Binder() {
        fun getPlayerManager(): MusicPlayerManager = this@MusicPlaybackService.playerManager
    }

    override fun onCreate() {
        super.onCreate()
        playerManager = MusicPlayerManager(this)
        playerManager.setService(this)
        createNotificationChannel()
        startPositionUpdates()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                if (playerManager.isPlaying.value) {
                    playerManager.pause()
                } else {
                    playerManager.play()
                }
            }
            ACTION_NEXT -> playerManager.playNext()
            ACTION_PREVIOUS -> playerManager.playPrevious()
        }

        // 更新通知
        playerManager.currentMusic.value?.let { music ->
            updateNotification(music, playerManager.isPlaying.value)
        }

        return START_STICKY
    }

    /**
     * 启动前台服务
     */
    fun startForegroundService(music: Music) {
        val notification = createNotification(music, false)
        startForeground(NOTIFICATION_ID, notification)
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "音乐播放",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示当前播放的音乐"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 创建通知
     */
    private fun createNotification(music: Music, isPlaying: Boolean): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPauseAction = NotificationCompat.Action(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
            if (isPlaying) "暂停" else "播放",
            getServiceIntent(ACTION_PLAY_PAUSE)
        )

        val nextAction = NotificationCompat.Action(
            R.drawable.ic_next,
            "下一首",
            getServiceIntent(ACTION_NEXT)
        )

        val previousAction = NotificationCompat.Action(
            R.drawable.ic_previous,
            "上一首",
            getServiceIntent(ACTION_PREVIOUS)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(music.title)
            .setContentText(music.artist)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(pendingIntent)
            .addAction(previousAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    /**
     * 更新通知
     */
    private fun updateNotification(music: Music, isPlaying: Boolean) {
        val notification = createNotification(music, isPlaying)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 创建服务Intent
     */
    private fun getServiceIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * 启动位置更新
     */
    private fun startPositionUpdates() {
        positionUpdateJob = serviceScope.launch {
            while (true) {
                playerManager.currentPosition.value = playerManager.getCurrentPosition()
                delay(100)
            }
        }
    }

    override fun onDestroy() {
        positionUpdateJob?.cancel()
        playerManager.release()
        super.onDestroy()
    }
}
