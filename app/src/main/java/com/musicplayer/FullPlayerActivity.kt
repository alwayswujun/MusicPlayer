package com.musicplayer

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.musicplayer.databinding.ActivityFullPlayerBinding
import com.musicplayer.lyrics.LyricsParser
import com.musicplayer.model.LyricLine
import com.musicplayer.model.Music
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * 全屏播放器Activity
 */
class FullPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullPlayerBinding
    private var currentMusic: Music? = null
    private var lyrics: List<LyricLine> = emptyList()
    private lateinit var lyricsParser: LyricsParser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lyricsParser = LyricsParser(this)

        // 获取音乐数据
        currentMusic = intent.getParcelableExtra<Music>("music")
        currentMusic?.let { music ->
            setupUI(music)
            loadLyrics(music)
        }

        setupControls()
    }

    private fun setupUI(music: Music) {
        binding.titleText.text = music.title
        binding.artistText.text = music.artist

        Glide.with(this)
            .load(music.coverUrl)
            .placeholder(R.drawable.ic_music_note)
            .error(R.drawable.ic_music_note)
            .into(binding.coverImage)
    }

    private fun loadLyrics(music: Music) {
        if (music.lyricsUrl != null) {
            lifecycleScope.launch {
                lyricsParser.parseFromUrl(music.lyricsUrl)
                    .onSuccess { lyricLines ->
                        lyrics = lyricLines
                        binding.lyricsView.setLyrics(lyrics)
                    }
                    .onFailure {
                        // 如果没有歌词，显示提示
                        binding.lyricsView.setLyrics(emptyList())
                    }
            }
        }
    }

    private fun setupControls() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.playPauseButton.setOnClickListener {
            // 通过服务控制播放
            val intent = android.content.Intent("com.musicplayer.TOGGLE_PLAY")
            sendBroadcast(intent)
        }

        binding.previousButton.setOnClickListener {
            val intent = android.content.Intent("com.musicplayer.PLAY_PREVIOUS")
            sendBroadcast(intent)
        }

        binding.nextButton.setOnClickListener {
            val intent = android.content.Intent("com.musicplayer.PLAY_NEXT")
            sendBroadcast(intent)
        }

        binding.progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val intent = android.content.Intent("com.musicplayer.SEEK_TO").apply {
                        putExtra("position", progress.toLong())
                    }
                    sendBroadcast(intent)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 监听播放状态更新
        lifecycleScope.launch {
            // 这里应该从服务获取播放状态，简化示例
            // 实际应用中应该通过Service绑定或EventBus等方式获取
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.lyricsView.reset()
    }
}
