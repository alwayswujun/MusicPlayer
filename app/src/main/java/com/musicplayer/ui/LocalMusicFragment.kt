package com.musicplayer.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.musicplayer.adapter.MusicAdapter
import com.musicplayer.databinding.FragmentLocalMusicBinding
import com.musicplayer.model.Music
import com.musicplayer.scanner.LocalMusicScanner
import kotlinx.coroutines.launch

/**
 * 本地音乐Fragment
 */
class LocalMusicFragment : Fragment() {

    private var _binding: FragmentLocalMusicBinding? = null
    private val binding get() = _binding!!

    private lateinit var musicAdapter: MusicAdapter
    private lateinit var localMusicScanner: LocalMusicScanner
    private var localMusicList: List<Music> = emptyList()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            scanLocalMusic()
        } else {
            Toast.makeText(
                requireContext(),
                "需要权限才能访问本地音乐",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocalMusicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        localMusicScanner = LocalMusicScanner(requireContext())

        checkPermissionsAndScan()
    }

    private fun setupRecyclerView() {
        musicAdapter = MusicAdapter { music, position ->
            playMusic(music, position)
        }

        binding.musicRecyclerView.adapter = musicAdapter
    }

    private fun checkPermissionsAndScan() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (neededPermissions.isNotEmpty()) {
            permissionLauncher.launch(neededPermissions.toTypedArray())
        } else {
            scanLocalMusic()
        }
    }

    private fun scanLocalMusic() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyText.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            localMusicScanner.scanLocalMusic()
                .onSuccess { musicList ->
                    localMusicList = musicList
                    musicAdapter.submitList(musicList)

                    binding.progressBar.visibility = View.GONE
                    if (musicList.isEmpty()) {
                        binding.emptyText.visibility = View.VISIBLE
                    } else {
                        binding.emptyText.visibility = View.GONE
                    }
                }
                .onFailure { e ->
                    binding.progressBar.visibility = View.GONE
                    binding.emptyText.visibility = View.VISIBLE
                    Toast.makeText(
                        requireContext(),
                        "扫描失败: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun playMusic(music: Music, position: Int) {
        // 发送广播通知MainActivity播放音乐
        val intent = Intent("com.musicplayer.PLAY_MUSIC").apply {
            putExtra("music", music)
            putExtra("position", position)
            putExtra("isLocal", true)
        }
        requireContext().sendBroadcast(intent)

        musicAdapter.setCurrentPlaying(music.id)
    }

    fun updateCurrentPlaying(musicId: String?) {
        musicAdapter.setCurrentPlaying(musicId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
