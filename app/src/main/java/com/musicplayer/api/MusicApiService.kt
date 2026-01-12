package com.musicplayer.api

import com.musicplayer.model.Music
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 在线音乐API服务
 * 可以集成免费音乐API，如网易云音乐API等
 */
interface MusicApiService {

    @GET("search")
    suspend fun searchMusic(
        @Query("keywords") keywords: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): MusicSearchResponse

    @GET("song/url")
    suspend fun getMusicUrl(
        @Query("id") id: String
    ): MusicUrlResponse

    @GET("lyric")
    suspend fun getLyrics(
        @Query("id") id: String
    ): LyricsResponse
}

/**
 * 音乐搜索响应
 */
data class MusicSearchResponse(
    val result: SearchResult? = null
)

data class SearchResult(
    val songs: List<SongInfo>? = null
)

data class SongInfo(
    val id: Long,
    val name: String,
    val artists: List<ArtistInfo>? = null,
    val album: AlbumInfo? = null,
    val duration: Long = 0
)

data class ArtistInfo(
    val name: String
)

data class AlbumInfo(
    val name: String,
    val picUrl: String? = null
)

/**
 * 音乐URL响应
 */
data class MusicUrlResponse(
    val data: List<MusicUrlData>? = null
)

data class MusicUrlData(
    val url: String? = null
)

/**
 * 歌词响应
 */
data class LyricsResponse(
    val lrc: LrcData? = null
)

data class LrcData(
    val lyric: String? = null
)

/**
 * 音乐API客户端
 */
class MusicApiClient {

    companion object {
        private const val BASE_URL = "https://api.injahown.cn/" // 示例API地址
        // 注意：实际使用时需要替换为可用的音乐API
        // 可以使用：网易云API、QQ音乐API等免费API

        private var apiService: MusicApiService? = null

        fun getApiService(): MusicApiService {
            if (apiService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                apiService = retrofit.create(MusicApiService::class.java)
            }
            return apiService!!
        }
    }
}

/**
 * 在线音乐仓库
 * 负责处理在线音乐相关的业务逻辑
 */
class OnlineMusicRepository {

    private val apiService = MusicApiClient.getApiService()

    /**
     * 搜索音乐
     */
    suspend fun searchMusic(query: String): Result<List<Music>> {
        return try {
            val response = apiService.searchMusic(query)
            val songs = response.result?.songs ?: emptyList()

            val musicList = songs.map { song ->
                Music(
                    id = song.id.toString(),
                    title = song.name,
                    artist = song.artists?.firstOrNull()?.name ?: "未知歌手",
                    url = "", // 需要通过getMusicUrl获取
                    coverUrl = song.album?.picUrl,
                    duration = song.duration,
                    isLocal = false
                )
            }

            Result.success(musicList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取音乐播放URL
     */
    suspend fun getMusicUrl(musicId: String): Result<String> {
        return try {
            val response = apiService.getMusicUrl(musicId)
            val url = response.data?.firstOrNull()?.url

            if (url != null) {
                Result.success(url)
            } else {
                Result.failure(Exception("无法获取音乐URL"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取歌词
     */
    suspend fun getLyrics(musicId: String): Result<String> {
        return try {
            val response = apiService.getLyrics(musicId)
            val lyrics = response.lrc?.lyric

            if (lyrics != null) {
                Result.success(lyrics)
            } else {
                Result.failure(Exception("无法获取歌词"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
