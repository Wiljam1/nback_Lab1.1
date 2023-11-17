package mobappdev.example.nback_cimpl.ui.screens

import android.content.res.Configuration
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
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import mobappdev.example.nback_cimpl.ui.viewmodels.GuessType
import kotlin.math.sqrt

@Composable
fun GameScreen(
    vm: GameViewModel,
    navigate: () -> Unit,
    textToSpeech: TextToSpeech
) {
    var orientation by remember { mutableStateOf(Configuration.ORIENTATION_PORTRAIT) }
    val configuration = LocalConfiguration.current

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }
            .collect { orientation = it }
    }

    val gameState by vm.gameState.collectAsState()
    val shouldSpeak: Boolean = gameState.shouldSpeak

    if (shouldSpeak) {
        val text = gameState.textToSpeak
        if (text != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
        vm.setShouldSpeak(false)
    }

    when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            LandscapeContent(vm, navigate)
        }

        else -> {
            PortraitContent(vm, navigate)
        }
    }

}

@Composable
private fun PortraitContent(
    vm: GameViewModel,
    navigate: () -> Unit
) {
    val gameState by vm.gameState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HomeButton(vm, navigate)
        StateInformationText(vm, 24)
        Divider(color = Color.Black, thickness = 2.dp)
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (gameState.gameType == GameType.Visual) {
                Grid(vm, 115.dp, true, 0.dp)

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
                MatchButton(vm, true, false)
            }
        }
    }
}

@Composable
private fun LandscapeContent(
    vm: GameViewModel,
    navigate: () -> Unit
) {
    val gameState by vm.gameState.collectAsState()

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (gameState.gameType == GameType.Visual) {
                    Grid(vm, 100.dp, false, 100.dp)

                } else {
                    Text(
                        modifier = Modifier.padding(start = 100.dp),
                        text = "Listen closely",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp)
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            HomeButton(vm, navigate)
            StateInformationText(vm, 24)
            MatchButton(vm, false, true)
        }
    }
}


@Composable
fun HomeButton(
    vm: GameViewModel,
    navigate: () -> Unit
) {
    Button(
        onClick = {
            navigate.invoke()
            vm.resetGame()
        },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        modifier = Modifier
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back arrow icon",
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Back to home",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}


@Composable
fun MatchButton(
    vm: GameViewModel,
    applyBottomPadding: Boolean,
    applyTopPadding: Boolean
) {
    val gameState by vm.gameState.collectAsState()
    val isRoundInProgress: Boolean = gameState.eventValue != -1
    val paddingBottom = if (applyBottomPadding && !isRoundInProgress) 20.dp else 0.dp
    val paddingTop = if (applyTopPadding && !isRoundInProgress) 25.dp else 20.dp
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
            .padding(bottom = paddingBottom, top = paddingTop)
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
fun StateInformationText(
    vm: GameViewModel,
    fontSize: Int,
) {
    val gameState by vm.gameState.collectAsState()
    val score by vm.score.collectAsState()

    Text(
        modifier = Modifier.padding(16.dp),
        text = "Score: $score".uppercase(),
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold
        )
    )
    Text(
        modifier = Modifier,
        text = "Current event number: ${gameState.previousValues.size}",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold
        )
    )
    Text(
        modifier = Modifier,
        text = "Correct responses: ${gameState.nrOfCorrect}",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold
        )
    )

}

@Composable
fun Grid(
    vm: GameViewModel,
    squareSize: Dp,
    applyBottomPadding: Boolean,
    startPadding: Dp
) {
    val gameState by vm.gameState.collectAsState()
    val TAG = "Grid"
    val gridSize = 3

    val paddingBottom: Dp = if (applyBottomPadding) 100.dp else 0.dp

    Column(
        modifier = Modifier
            .padding(bottom = paddingBottom, start = startPadding)
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