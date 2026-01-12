package com.musicplayer

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.musicplayer.databinding.ActivityMainBinding
import com.musicplayer.model.Music
import com.musicplayer.service.MusicPlaybackService
import com.musicplayer.ui.LocalMusicFragment
import com.musicplayer.ui.OnlineMusicFragment
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * 主Activity
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var musicService: MusicPlaybackService? = null
    private var isBound = false
    private var currentMusic: Music? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlaybackService.MusicBinder
            musicService = binder.getPlayerManager().getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            musicService = null
        }
    }

    private val musicReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getParcelableExtra<Music>("music")?.let { music ->
                playMusic(music)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupBottomPlayer()
        registerReceiver()
        bindToService()
    }

    private fun bindToService() {
        Intent(this, MusicPlaybackService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int) = when (position) {
                0 -> LocalMusicFragment()
                1 -> OnlineMusicFragment()
                else -> throw IllegalStateException("Invalid position: $position")
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.local_music)
                1 -> getString(R.string.online_music)
                else -> ""
            }
        }.attach()
    }

    private fun setupBottomPlayer() {
        binding.playPauseButton.setOnClickListener {
            if (isBound) {
                musicService?.getPlayerManager()?.let { playerManager ->
                    if (playerManager.isPlaying.value) {
                        playerManager.pause()
                    } else {
                        playerManager.play()
                    }
                }
            }
        }

        binding.previousButton.setOnClickListener {
            if (isBound) {
                musicService?.getPlayerManager()?.playPrevious()
            }
        }

        binding.nextButton.setOnClickListener {
            if (isBound) {
                musicService?.getPlayerManager()?.playNext()
            }
        }

        binding.progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && isBound) {
                    musicService?.getPlayerManager()?.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 点击底部播放器打开全屏播放器
        binding.bottomPlayer.setOnClickListener {
            currentMusic?.let { music ->
                val intent = Intent(this, FullPlayerActivity::class.java).apply {
                    putExtra("music", music)
                }
                startActivity(intent)
            }
        }
    }

    private fun registerReceiver() {
        val filter = IntentFilter("com.musicplayer.PLAY_MUSIC")
        registerReceiver(musicReceiver, filter)
    }

    private fun playMusic(music: Music) {
        currentMusic = music
        binding.bottomPlayer.visibility = android.view.View.VISIBLE

        // 更新UI
        binding.titleText.text = music.title
        binding.artistText.text = music.artist

        Glide.with(this)
            .load(music.coverUrl)
            .placeholder(R.drawable.ic_music_note)
            .error(R.drawable.ic_music_note)
            .into(binding.coverImage)

        if (isBound) {
            musicService?.getPlayerManager()?.let { playerManager ->
                lifecycleScope.launch {
                    playerManager.currentMusic.collect { current ->
                        binding.playPauseButton.setImageResource(
                            if (playerManager.isPlaying.value) R.drawable.ic_pause else R.drawable.ic_play
                        )
                    }
                }

                lifecycleScope.launch {
                    playerManager.currentPosition.collect { position ->
                        binding.progressBar.progress = position.toInt()
                        binding.currentTimeText.text = formatTime(position)
                    }
                }

                lifecycleScope.launch {
                    playerManager.duration.collect { duration ->
                        binding.progressBar.max = duration.toInt()
                        binding.totalTimeText.text = formatTime(duration)
                    }
                }
            }
        }
    }

    private fun formatTime(milliseconds: Long): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(musicReceiver)
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}
