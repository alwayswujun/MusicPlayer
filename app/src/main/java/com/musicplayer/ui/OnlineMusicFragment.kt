package com.musicplayer.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.musicplayer.adapter.MusicAdapter
import com.musicplayer.api.OnlineMusicRepository
import com.musicplayer.databinding.FragmentOnlineMusicBinding
import com.musicplayer.model.Music
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 在线音乐Fragment
 */
class OnlineMusicFragment : Fragment() {

    private var _binding: FragmentOnlineMusicBinding? = null
    private val binding get() = _binding!!

    private lateinit var musicAdapter: MusicAdapter
    private lateinit var onlineMusicRepository: OnlineMusicRepository
    private var searchJob: Job? = null
    private var searchResults: List<Music> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnlineMusicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        onlineMusicRepository = OnlineMusicRepository()
    }

    private fun setupRecyclerView() {
        musicAdapter = MusicAdapter { music, position ->
            playOnlineMusic(music, position)
        }

        binding.searchResultsRecyclerView.adapter = musicAdapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchMusic(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 防抖搜索
                searchJob?.cancel()
                if (!newText.isNullOrEmpty()) {
                    searchJob = viewLifecycleOwner.lifecycleScope.launch {
                        delay(500)
                        searchMusic(newText)
                    }
                }
                return true
            }
        })
    }

    private fun searchMusic(query: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyText.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            onlineMusicRepository.searchMusic(query)
                .onSuccess { musicList ->
                    searchResults = musicList
                    musicAdapter.submitList(musicList)

                    binding.progressBar.visibility = View.GONE
                    if (musicList.isEmpty()) {
                        binding.emptyText.text = "未找到相关音乐"
                        binding.emptyText.visibility = View.VISIBLE
                    } else {
                        binding.emptyText.visibility = View.GONE
                    }
                }
                .onFailure { e ->
                    binding.progressBar.visibility = View.GONE
                    binding.emptyText.text = "搜索失败，请稍后重试"
                    binding.emptyText.visibility = View.VISIBLE
                    Toast.makeText(
                        requireContext(),
                        "搜索失败: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private suspend fun playOnlineMusic(music: Music, position: Int) {
        binding.progressBar.visibility = View.VISIBLE

        // 获取播放URL
        onlineMusicRepository.getMusicUrl(music.id)
            .onSuccess { url ->
                binding.progressBar.visibility = View.GONE

                // 创建带有播放URL的音乐对象
                val musicWithUrl = music.copy(url = url)

                // 发送广播通知MainActivity播放音乐
                val intent = Intent("com.musicplayer.PLAY_MUSIC").apply {
                    putExtra("music", musicWithUrl)
                    putExtra("position", position)
                    putExtra("isLocal", false)
                }
                requireContext().sendBroadcast(intent)

                musicAdapter.setCurrentPlaying(music.id)
            }
            .onFailure { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "获取音乐失败: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun updateCurrentPlaying(musicId: String?) {
        musicAdapter.setCurrentPlaying(musicId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        _binding = null
    }
}
