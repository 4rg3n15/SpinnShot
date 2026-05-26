package com.example.spinnshot.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spinnshot.data.GameMode
import com.example.spinnshot.data.GameState
import com.example.spinnshot.data.Player
import com.example.spinnshot.data.Question
import com.example.spinnshot.data.QuestionRepository
import com.example.spinnshot.data.remote.ApiClient
import com.example.spinnshot.data.remote.GameRecordDto
import com.example.spinnshot.data.remote.PlayerScoreDto
import com.example.spinnshot.logic.GameEngine
import com.example.spinnshot.logic.GameResolution
import com.example.spinnshot.logic.ScoreCalculator
import com.example.spinnshot.logic.TurnOutcome
import com.example.spinnshot.logic.WinnerResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val PARTY_PHRASES = listOf(
    "Que la suerte esté de tu lado…",
    "El destino te eligió 👀",
    "Demuestra qué tan culto eres",
    "Ojo, todos están mirando.",
    "Hora de brillar (o pagar).",
    "Sin presión… solo gloria o trago.",
    "El silencio cuesta puntos."
)

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = QuestionRepository(app.applicationContext)

    // ---- Setup-time state ----
    private val _isAdult = MutableStateFlow(false)
    val isAdult: StateFlow<Boolean> = _isAdult.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories.asStateFlow()

    private val _selectedMode = MutableStateFlow<GameMode?>(null)
    val selectedMode: StateFlow<GameMode?> = _selectedMode.asStateFlow()

    private val _draftPlayers = MutableStateFlow<List<Player>>(emptyList())
    val draftPlayers: StateFlow<List<Player>> = _draftPlayers.asStateFlow()

    private val _rounds = MutableStateFlow(5)
    val rounds: StateFlow<Int> = _rounds.asStateFlow()

    // ---- Runtime state ----
    private val _game = MutableStateFlow<GameState?>(null)
    val game: StateFlow<GameState?> = _game.asStateFlow()

    private val _phrase = MutableStateFlow(PARTY_PHRASES.first())
    val phrase: StateFlow<String> = _phrase.asStateFlow()

    private val _resolution = MutableStateFlow<GameResolution?>(null)
    val resolution: StateFlow<GameResolution?> = _resolution.asStateFlow()

    // ---- Setters ----
    fun setAdult(adult: Boolean) {
        _isAdult.value = adult
        // If currently not adult, ensure any alcohol-based mode picked previously is cleared.
        if (!adult && _selectedMode.value?.usesAlcohol == true) {
            _selectedMode.value = null
        }
    }

    fun toggleCategory(name: String) {
        _selectedCategories.update { current ->
            when {
                name == com.example.spinnshot.data.Categories.ALL -> {
                    if (current.contains(com.example.spinnshot.data.Categories.ALL)) emptySet()
                    else (com.example.spinnshot.data.Categories.available + com.example.spinnshot.data.Categories.ALL).toSet()
                }
                current.contains(name) -> current - name - com.example.spinnshot.data.Categories.ALL
                else -> {
                    val updated = current + name
                    if (updated.containsAll(com.example.spinnshot.data.Categories.available))
                        updated + com.example.spinnshot.data.Categories.ALL
                    else updated
                }
            }
        }
    }

    fun setMode(mode: GameMode) {
        _selectedMode.value = mode
    }

    fun addDraftPlayer(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        if (_draftPlayers.value.any { it.name.equals(trimmed, ignoreCase = true) }) return
        _draftPlayers.update { it + Player(name = trimmed) }
    }

    fun removeDraftPlayer(id: String) {
        _draftPlayers.update { it.filterNot { p -> p.id == id } }
    }

    fun setRounds(value: Int) {
        _rounds.value = value.coerceAtLeast(1)
    }

    // ---- Validation ----
    fun canStartGame(): Boolean {
        val mode = _selectedMode.value ?: return false
        if (mode.usesAlcohol && !_isAdult.value) return false
        return _draftPlayers.value.size >= 2 &&
                _selectedCategories.value.isNotEmpty() &&
                _rounds.value >= 1
    }

    // ---- Game lifecycle ----
    fun startGame() {
        val mode = _selectedMode.value ?: return
        val state = GameEngine.startNew(
            players = _draftPlayers.value,
            categories = _selectedCategories.value,
            mode = mode,
            rounds = _rounds.value
        )
        _game.value = state
        _resolution.value = null
    }

    fun spin() {
        val current = _game.value ?: return
        val pool = repository.byCategories(current.categories)
        val next = GameEngine.spin(current, pool)
        _phrase.value = PARTY_PHRASES.random()
        _game.value = next
    }

    fun applyOutcome(outcome: TurnOutcome) {
        val current = _game.value ?: return
        val updated = GameEngine.applyOutcomeAndAdvance(current, outcome)
        _game.value = updated
        if (updated.finished) finalize()
    }

    fun describeOutcome(outcome: TurnOutcome): String {
        val mode = _game.value?.mode ?: return ""
        return ScoreCalculator.describe(mode, outcome)
    }

    // ---- In-game editing ----
    fun editCategories(categories: Set<String>) {
        _selectedCategories.value = categories
        _game.update { it?.copy(categories = categories) }
    }

    fun editAddPlayer(name: String) {
        _game.update { current ->
            current?.let { GameEngine.addPlayer(it, name) }
        }
    }

    fun editRemovePlayer(id: String) {
        _game.update { current ->
            current?.let { GameEngine.removePlayer(it, id) }
        }
    }

    fun editExtendRounds(extra: Int) {
        _game.update { current ->
            current?.let { GameEngine.extendRounds(it, extra) }
        }
    }

    fun editChangeMode(mode: GameMode) {
        if (mode.usesAlcohol && !_isAdult.value) return
        _selectedMode.value = mode
        _game.update { it?.copy(mode = mode) }
    }

    fun terminate() {
        _game.value = null
        _resolution.value = null
    }

    fun resetToNewGame() {
        _game.value = null
        _resolution.value = null
        _draftPlayers.value = emptyList()
        _selectedCategories.value = emptySet()
        _selectedMode.value = null
        _rounds.value = 5
    }

    // ---- Finalization ----
    private fun finalize() {
        val state = _game.value ?: return
        val resolution = WinnerResolver.resolve(state.players, state.mode)
        _resolution.value = resolution
        _game.update { it?.copy(tieBreak = resolution.needsTieBreak) }
        resolution.winner?.let { winner -> persistRemote(state, winner) }
    }

    fun resolveTieBreak(winner: Player) {
        val state = _game.value ?: return
        val resolved = GameResolution(
            ranking = (listOf(winner) + state.ranking().filterNot { it.id == winner.id }),
            winner = winner,
            needsTieBreak = false
        )
        _resolution.value = resolved
        _game.update { it?.copy(tieBreak = false) }
        persistRemote(state, winner)
    }

    private fun persistRemote(state: GameState, winner: Player) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                ApiClient.service.saveGame(
                    GameRecordDto(
                        mode = state.mode.displayName,
                        rounds = state.totalRounds,
                        categories = state.categories.toList(),
                        players = state.players.map { PlayerScoreDto.fromPlayer(it) },
                        winner = winner.name
                    )
                )
            }
        }
    }

    /** Loaded questions count used by the categories screen to warn the user. */
    fun availableQuestionCount(): Int = repository.loadAll().size

    fun questionsForCurrentSelection(): List<Question> =
        repository.byCategories(_selectedCategories.value)
}
