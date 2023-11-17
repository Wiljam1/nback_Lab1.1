package mobappdev.example.nback_cimpl.ui.screens

import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import mobappdev.example.nback_cimpl.ui.viewmodels.GuessType

@Composable
fun GameScreen(
    vm: GameViewModel,
    navigate: () -> Unit,
    textToSpeech: TextToSpeech
) {
    val gameState by vm.gameState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    // -----------
    val TAG = "GameScreen"
    val isRoundInProgress: Boolean = gameState.eventValue != -1
    val shouldSpeak: Boolean = gameState.shouldSpeak

    // textToSpeech when updated in view-model
    if (shouldSpeak) {
        val text = gameState.textToSpeak
        if (text != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
        vm.setShouldSpeak(false)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    navigate.invoke()
                    vm.resetGame()
                },
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(4.dp)
            ) {
                Text(
                    text = "Back to home",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            StateInformationText(
                vm = vm,

                )
            // Todo: You'll probably want to change this "BOX" part of the composable
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (gameState.gameType == GameType.Visual) {
                    Grid(vm)

                } else {
                    Text(
                        modifier = Modifier.padding(bottom = 100.dp),
                        text = "Listen closely",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp)
                    )
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    MatchButton(vm)
                }
            }
        }
    }
}

@Composable
fun MatchButton(
    vm: GameViewModel
) {
    val gameState by vm.gameState.collectAsState()
    val isRoundInProgress: Boolean = gameState.eventValue != -1
    val paddingValue = if (!isRoundInProgress) 20.dp else 0.dp
    Button(
        onClick = {
            when {
                isRoundInProgress -> vm.checkMatch()
                else -> vm.startGame()
            }
        },
        colors = if (isRoundInProgress) {
            when (gameState.guessType) {
                GuessType.NONE -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                GuessType.WRONG -> ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Red)
                GuessType.CORRECT -> ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Green)
            }
        } else {
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        },
        modifier = Modifier
            .padding(bottom = paddingValue)
    ) {
        if (!isRoundInProgress) {
            Text(
                text = "Start round".uppercase(),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        } else {
            Icon(
                painter = painterResource(
                    id = when (gameState.gameType) {
                        GameType.Audio -> R.drawable.sound_on
                        GameType.Visual -> R.drawable.visual
                        else -> {
                            R.drawable.visual
                        }
                    }
                ),
                contentDescription = "Button with icon",
                modifier = Modifier
                    .height(88.dp)
                    .aspectRatio(3f / 2f)
            )
        }
    }
}

@Composable
fun PlayArea(
    vm: GameViewModel
) {
    Text(
        modifier = Modifier.padding(8.dp),
        text = "Tap below when match".uppercase(),
        style = MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MatchButton(vm)
    }
}

@Composable
fun StartButton(
    vm: GameViewModel
) {
    val gameState by vm.gameState.collectAsState()
    val isRoundInProgress: Boolean = gameState.eventValue != -1
    Button(
        onClick = vm::startGame,
        enabled = !isRoundInProgress
    ) {
        Text(
            text = "Start round".uppercase(),
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun StateInformationText(
    vm: GameViewModel,
    ) {
    val gameState by vm.gameState.collectAsState()
    val score by vm.score.collectAsState()

    Text(
        modifier = Modifier.padding(16.dp),
        text = "Score: $score".uppercase(),
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    )
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = "Current event number: ${gameState.previousValues.size}",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    )
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = "Correct responses: ${gameState.nrOfCorrect}",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    )

}

@Composable
fun Grid(
    vm: GameViewModel
) {
    val gameState by vm.gameState.collectAsState()
    val TAG = "Grid"
    val gridSize = 3
    val squareSize = 115.dp

    Column(
        modifier = Modifier
            .padding(bottom = 100.dp)
    ) {
        repeat(gridSize) { rowIndex ->
            Row {
                repeat(gridSize) { columnIndex ->
                    val boxValue = rowIndex * gridSize + columnIndex + 1
                    Box(
                        modifier = Modifier
                            .size(squareSize)
                            .padding(5.dp)
                            .clickable {
                                Log.d(
                                    TAG,
                                    "Box ($rowIndex, $columnIndex) clicked, value: $boxValue"
                                )
                            }
                            .background(
                                if (gameState.eventValue != -1) {
                                    if (boxValue == gameState.eventValue) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.secondary
                                    }
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                }
                            )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun GameScreenPreview() {
    Surface() {
        //GameScreen(FakeVM())
    }
}