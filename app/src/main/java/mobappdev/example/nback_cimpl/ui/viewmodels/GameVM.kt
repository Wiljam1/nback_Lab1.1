package mobappdev.example.nback_cimpl.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository

/**
 * This is the GameViewModel.
 *
 * It is good practice to first make an interface, which acts as the blueprint
 * for your implementation. With this interface we can create fake versions
 * of the viewmodel, which we can use to test other parts of our app that depend on the VM.
 *
 * Our viewmodel itself has functions to start a game, to specify a gametype,
 * and to check if we are having a match
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>

    fun setShouldSpeak(bool: Boolean)
    fun setGameType(gameType: GameType)
    fun getGameType(): GameType
    fun getNValue(): Int
    fun getEventInterval(): Long
    fun getNrOfEvents(): Int
    fun startGame()
    fun resetGame()
    fun checkMatch(): Boolean
    fun selectAudio()
    fun selectVisual()

}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository
) : GameViewModel, ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    override fun setShouldSpeak(shouldSpeak: Boolean) {
        _gameState.value = _gameState.value.copy(
            shouldSpeak = shouldSpeak
        )
    }

    private var job: Job? = null  // coroutine job for the game event

    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var events = emptyArray<Int>()  // Array with all events

    //-----------------------------------

    override fun setGameType(gameType: GameType) {
        // update the gametype in the gamestate
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun getGameType(): GameType {
        return _gameState.value.gameType
    }

    override fun getNValue(): Int {
        return _gameState.value.nBack
    }

    override fun getEventInterval(): Long {
        return _gameState.value.eventDelay
    }

    override fun getNrOfEvents(): Int {
        return _gameState.value.nrOfEvents
    }

    override fun selectAudio() {
        setGameType(GameType.Audio)
    }

    override fun selectVisual() {
        setGameType(GameType.Visual)
    }

    override fun startGame() {
        job?.cancel()  // Cancel any existing game loop

        val nrOfEvents = _gameState.value.nrOfEvents
        val n = _gameState.value.nBack

        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        events = nBackHelper.generateNBackString(nrOfEvents, nrOfEvents - 1, 30, n).toList()
            .toTypedArray()  // TODO Higher Grade: currently the size etc. are hardcoded, make these based on user input
        Log.d("GameVM", "The following sequence was generated: ${events.contentToString()}")

        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame(events)
                GameType.AudioVisual -> runAudioVisualGame()
                GameType.Visual -> runVisualGame(events)
            }
        }
    }

    override fun checkMatch(): Boolean {
        val n = _gameState.value.nBack
        val currentValue = gameState.value.eventValue
        val previousValues = gameState.value.previousValues

        return if (previousValues.size < n + 1 || gameState.value.eventScored) {
            // Not allowed to check
            _gameState.value = _gameState.value.copy(guessType = GuessType.NONE)
            false
        } else {
            val correctValue = previousValues[previousValues.size - n - 1]
            val isMatch = currentValue == correctValue

            handleResult(isMatch)
            isMatch
        }
    }

    private fun handleResult(isMatch: Boolean) {
        if (isMatch) {
            _score.value += 10
            _gameState.value = _gameState.value.copy(
                guessType = GuessType.CORRECT,
                nrOfCorrect = gameState.value.nrOfCorrect + 1
            )
        } else {
            _score.value -= 5
            _gameState.value = _gameState.value.copy(guessType = GuessType.WRONG)
        }

        _gameState.value = _gameState.value.copy(eventScored = true)
    }


    private suspend fun runAudioGame(events: Array<Int>) {
        resetGame()

        for (value in events) {
            _gameState.value = _gameState.value.copy(
                eventValue = value,
                eventScored = false,
                guessType = GuessType.NONE,
                textToSpeak = numberToAlphabet(value),
                shouldSpeak = true,
                previousValues = _gameState.value.previousValues.toMutableList().apply { add(value) }
            )
            delay(_gameState.value.eventDelay)
        }

        delay(_gameState.value.eventDelay)
        endGame()
    }

    private fun numberToAlphabet(number: Int): String {
        return when (number) {
            1 -> "A"
            2 -> "B"
            3 -> "C"
            4 -> "D"
            5 -> "E"
            6 -> "F"
            7 -> "G"
            8 -> "H"
            9 -> "I"
            else -> ""
        }
    }

    private suspend fun runVisualGame(events: Array<Int>) {
        resetGame()

        for (value in events) {
            _gameState.value = _gameState.value.copy(
                eventValue = value,
                eventScored = false,
                previousValues = _gameState.value.previousValues.toMutableList().apply { add(value) },
                guessType = GuessType.NONE
            )
            delay(_gameState.value.eventDelay)
        }

        delay(_gameState.value.eventDelay)
        endGame()
    }

    private fun endGame() {
        _gameState.value = _gameState.value.copy(
            eventValue = -1,
            eventScored = false,
            previousValues = mutableListOf(),
        )
        updateHighScore()
    }

    private fun updateHighScore() {
        if (_score.value > _highscore.value) {
            _highscore.value = _score.value
        }
    }

    override fun resetGame() {
        job?.cancel()

        _gameState.value = _gameState.value.copy(
            eventValue = -1,
            previousValues = mutableListOf(),
            nrOfCorrect = 0,
            textToSpeak = null,
        )

        _score.value = 0
    }

    private fun runAudioVisualGame() {
        // Todo: Make work for Higher grade
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository)
            }
        }
    }

    init {
        // Code that runs during creation of the vm
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
    }
}

// Class with the different game types
enum class GameType {
    Audio,
    Visual,
    AudioVisual
}

enum class GuessType {
    NONE, CORRECT, WRONG
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType = GameType.Visual,  // Type of the game
    val nBack: Int = 2,
    val eventDelay: Long = 2000L,
    val nrOfEvents: Int = 10,
    val eventValue: Int = -1,  // The value of the array string
    val previousValues: MutableList<Int> = mutableListOf(),
    val eventScored: Boolean = false,
    val guessType: GuessType = GuessType.NONE,
    val nrOfCorrect: Int = 0,
    val textToSpeak: String? = null,
    val shouldSpeak: Boolean = true,
)

//class FakeVM : GameViewModel {
//    override val gameState: StateFlow<GameState>
//        get() = MutableStateFlow(GameState()).asStateFlow()
//    override val score: StateFlow<Int>
//        get() = MutableStateFlow(2).asStateFlow()
//    override val highscore: StateFlow<Int>
//        get() = MutableStateFlow(42).asStateFlow()
//    override val nBack: Int
//        get() = 2
//
//    override fun setGameType(gameType: GameType) {
//    }
//
//    override fun startGame() {
//    }
//
//    override fun checkMatch() {
//    }
//
//    override fun selectAudio() {
//    }
//
//    override fun selectVisual() {
//    }
//}