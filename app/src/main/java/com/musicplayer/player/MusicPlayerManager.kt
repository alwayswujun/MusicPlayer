package com.musicplayer.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.musicplayer.model.Music
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 音乐播放管理器
 * 使用ExoPlayer进行音乐播放
 */
class MusicPlayerManager(context: Context) : Player.Listener {

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        setListener(this@MusicPlayerManager)
    }

    private var service: MusicPlaybackService? = null

    private val _playlist = MutableStateFlow<List<Music>>(emptyList())
    val playlist: StateFlow<List<Music>> = _playlist.asStateFlow()

    private val _currentMusic = MutableStateFlow<Music?>(null)
    val currentMusic: StateFlow<Music?> = _currentMusic.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private var currentIndex = 0

    init {
        // 启用播放器位置更新
        exoPlayer.playWhenReady = false
    }

    /**
     * 设置Service引用
     */
    fun setService(service: MusicPlaybackService) {
        this.service = service
    }

    /**
     * 获取Service引用
     */
    fun getService(): MusicPlaybackService? = service

    /**
     * 获取当前播放位置
     */
    fun getCurrentPosition(): Long {
        return exoPlayer.currentPosition
    }

    /**
     * 设置播放列表
     */
    fun setPlaylist(musicList: List<Music>, startIndex: Int = 0) {
        _playlist.value = musicList
        if (musicList.isNotEmpty()) {
            currentIndex = startIndex.coerceIn(0, musicList.size - 1)
            loadMusic(currentIndex)
        }
    }

    /**
     * 加载音乐
     */
    private fun loadMusic(index: Int) {
        val music = _playlist.value.getOrNull(index) ?: return

        _currentMusic.value = music

        val mediaItem = MediaItem.fromUri(music.url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        _duration.value = exoPlayer.duration
    }

    /**
     * 播放
     */
    fun play() {
        exoPlayer.playWhenReady = true
        exoPlayer.play()
    }

    /**
     * 暂停
     */
    fun pause() {
        exoPlayer.pause()
    }

    /**
     * 播放指定索引的音乐
     */
    fun playMusicAt(index: Int) {
        val music = _playlist.value.getOrNull(index) ?: return
        currentIndex = index
        loadMusic(index)
        play()
    }

    /**
     * 播放下一首
     */
    fun playNext() {
        if (_playlist.value.isEmpty()) return

        currentIndex = (currentIndex + 1) % _playlist.value.size
        loadMusic(currentIndex)
        play()
    }

    /**
     * 播放上一首
     */
    fun playPrevious() {
        if (_playlist.value.isEmpty()) return

        currentIndex = if (currentIndex > 0) currentIndex - 1 else _playlist.value.size - 1
        loadMusic(currentIndex)
        play()
    }

    /**
     * 跳转到指定位置
     */
    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    /**
     * 释放播放器
     */
    fun release() {
        exoPlayer.release()
    }

    // Player.Listener 实现
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                _duration.value = exoPlayer.duration
            }
            Player.STATE_ENDED -> {
                // 自动播放下一首
                if (_playlist.value.size > 1) {
                    playNext()
                }
            }
            else -> {}
        }
    }
}
