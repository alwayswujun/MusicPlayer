package com.musicplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.musicplayer.databinding.ItemMusicBinding
import com.musicplayer.model.Music

/**
 * 音乐列表适配器
 */
class MusicAdapter(
    private val onMusicClick: (Music, Int) -> Unit
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    private var musicList: List<Music> = emptyList()
    private var currentPlayingId: String? = null

    fun submitList(list: List<Music>) {
        val diffCallback = MusicDiffCallback(musicList, list)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        musicList = list
        diffResult.dispatchUpdatesTo(this)
    }

    fun setCurrentPlaying(musicId: String?) {
        val previousId = currentPlayingId
        currentPlayingId = musicId

        musicList.indexOfFirst { it.id == previousId }.let {
            if (it >= 0) notifyItemChanged(it)
        }
        musicList.indexOfFirst { it.id == currentPlayingId }.let {
            if (it >= 0) notifyItemChanged(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding = ItemMusicBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MusicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.bind(musicList[position], position)
    }

    override fun getItemCount(): Int = musicList.size

    inner class MusicViewHolder(
        private val binding: ItemMusicBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(music: Music, position: Int) {
            binding.apply {
                titleText.text = music.title
                artistText.text = music.artist

                // 加载封面
                Glide.with(root)
                    .load(music.coverUrl)
                    .placeholder(com.musicplayer.R.drawable.ic_music_note)
                    .error(com.musicplayer.R.drawable.ic_music_note)
                    .into(coverImage)

                // 当前播放的高亮效果
                if (music.id == currentPlayingId) {
                    root.alpha = 1f
                } else {
                    root.alpha = 0.8f
                }

                root.setOnClickListener {
                    onMusicClick(music, position)
                }
            }
        }
    }

    private class MusicDiffCallback(
        private val oldList: List<Music>,
        private val newList: List<Music>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
