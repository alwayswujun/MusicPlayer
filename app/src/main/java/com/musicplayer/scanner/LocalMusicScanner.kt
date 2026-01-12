package com.musicplayer.scanner

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.musicplayer.model.Music
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 本地音乐扫描器
 * 扫描设备上的所有音频文件
 */
class LocalMusicScanner(private val context: Context) {

    /**
     * 扫描所有本地音乐
     */
    suspend fun scanLocalMusic(): Result<List<Music>> = withContext(Dispatchers.IO) {
        queryMusic(selection = null, selectionArgs = null)
    }

    /**
     * 搜索本地音乐
     */
    suspend fun searchLocalMusic(query: String): Result<List<Music>> = withContext(Dispatchers.IO) {
        val selection = """
            ${MediaStore.Audio.Media.IS_MUSIC} = 1 AND (
                ${MediaStore.Audio.Media.TITLE} LIKE ? OR
                ${MediaStore.Audio.Media.ARTIST} LIKE ?
            )
        """.trimIndent()
        val selectionArgs = arrayOf("%$query%", "%$query%")
        queryMusic(selection, selectionArgs)
    }

    /**
     * 通用音乐查询方法
     */
    private fun queryMusic(selection: String?, selectionArgs: Array<String>?): Result<List<Music>> {
        return try {
            val musicList = mutableListOf<Music>()
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID
            )

            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                parseMusicCursor(it, musicList)
            }

            Result.success(musicList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 解析音乐数据游标
     */
    private fun parseMusicCursor(cursor: Cursor, musicList: MutableList<Music>) {
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val title = cursor.getString(titleColumn) ?: "未知歌曲"
            val artist = cursor.getString(artistColumn) ?: "未知歌手"
            val data = cursor.getString(dataColumn)
            val duration = cursor.getLong(durationColumn)
            val albumId = cursor.getLong(albumIdColumn)

            // 构建专辑封面URI
            val coverUri = Uri.parse("content://media/external/audio/albumart/$albumId")

            if (data != null) {
                musicList.add(
                    Music(
                        id = id.toString(),
                        title = title,
                        artist = artist,
                        url = data,
                        coverUrl = coverUri.toString(),
                        duration = duration,
                        isLocal = true
                    )
                )
            }
        }
    }
}
