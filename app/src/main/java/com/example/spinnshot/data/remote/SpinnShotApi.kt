package com.example.spinnshot.data.remote

import com.example.spinnshot.BuildConfig
import com.example.spinnshot.data.Player
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@Serializable
data class PlayerScoreDto(
    val name: String,
    val points: Int,
    val shots: Int
) {
    companion object {
        fun fromPlayer(p: Player) = PlayerScoreDto(p.name, p.points, p.shots)
    }
}

@Serializable
data class GameRecordDto(
    val mode: String,
    val rounds: Int,
    val categories: List<String>,
    val players: List<PlayerScoreDto>,
    val winner: String,
    @SerialName("played_at") val playedAt: String? = null
)

@Serializable
data class LeaderboardEntryDto(
    val name: String,
    val games: Int = 0,
    val points: Int = 0,
    val shots: Int = 0,
    val wins: Int = 0
)

interface SpinnShotApi {
    @POST("api/games")
    suspend fun saveGame(@Body record: GameRecordDto): Map<String, String>

    @GET("api/leaderboard")
    suspend fun leaderboard(@Query("limit") limit: Int = 10): List<LeaderboardEntryDto>
}

object ApiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    }

    val service: SpinnShotApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SpinnShotApi::class.java)
    }
}
