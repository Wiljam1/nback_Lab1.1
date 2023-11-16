package mobappdev.example.nback_cimpl.ui.screens

import android.content.res.Configuration
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

/**
 * This is the Home screen composable
 *
 * Currently this screen shows the saved highscore
 * It also contains a button which can be used to show that the C-integration works
 * Furthermore it contains two buttons that you can use to start a game
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */

@Composable
fun HomeScreen(
    vm: GameViewModel,
    navigate: () -> Unit
) {
    var orientation by remember { mutableStateOf(Configuration.ORIENTATION_PORTRAIT) }
    val configuration = LocalConfiguration.current

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }
            .collect { orientation = it }
    }

    when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            LandscapeContent(vm = vm, navigate = navigate)
        }

        else -> {
            PortraitContent(vm = vm, navigate = navigate)
        }
    }

}

@Composable
fun PortraitContent(
    vm: GameViewModel,
    navigate: () -> Unit,
) {
    val highScore by vm.highscore.collectAsState()  // Highscore is its own StateFlow
    val gameState by vm.gameState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconAndTitle(88, 36, 32)
        Divider(color = Color.Black, thickness = 2.dp)
        HighScoreText(highScore, 36, 32)
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {

            // -- Can't remove this if-statement --
            if (gameState.eventValue != -1) {

            }
            // ----------------------
            SettingInformationText(
                mode = vm.getGameType().toString(),
                n = vm.getNValue(),
                eventDelay = vm.getEventInterval(),
                nrOfEvents = vm.getNrOfEvents()
            )
        }
        GameTypeToggles(vm = vm)
        StartButton(navigate, 88, 16, 1.0f)
    }

}

@Composable
fun LandscapeContent(
    vm: GameViewModel,
    navigate: () -> Unit,
) {
    val highScore by vm.highscore.collectAsState()  // Highscore is its own StateFlow
    val gameState by vm.gameState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconAndTitle(44, 16, 8)
        Divider(color = Color.Black, thickness = 2.dp)
        HighScoreText(highScore, 24, 8)
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {

                    // -- Can't remove this if-statement --
                    if (gameState.eventValue != -1) {
                    }
                    // ----------------------
                    SettingInformationText(
                        mode = vm.getGameType().toString(),
                        n = vm.getNValue(),
                        eventDelay = vm.getEventInterval(),
                        nrOfEvents = vm.getNrOfEvents()
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                GameTypeToggles(vm = vm)
                StartButton(navigate, 88, 16, 1.0f)
            }
        }
    }
}

@Composable
fun HighScoreText(
    highScore: Int,
    fontSize: Int,
    padding: Int
) {
    Text(
        modifier = Modifier.padding(padding.dp),
        text = "High-Score = $highScore",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
fun IconAndTitle(
    iconHeight: Int,
    titleFontSize: Int,
    padding: Int,
) {
    Icon(
        painter = painterResource(id = R.drawable.baseline_lens_blur_24),
        contentDescription = "grid",
        modifier = Modifier
            .height(iconHeight.dp)
            .aspectRatio(3f / 2f)
    )
    Text(
        modifier = Modifier.padding(padding.dp),
        text = "N-Back",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = titleFontSize.sp,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
fun GameTypeToggles(
    vm: GameViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { vm.selectAudio() },
            colors = if (vm.getGameType() == GameType.Audio) {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            } else {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.sound_on),
                contentDescription = "Sound",
                modifier = Modifier
                    .height(48.dp)
                    .aspectRatio(3f / 2f)
            )
        }
        Button(
            onClick = { vm.selectVisual() },
            colors = if (vm.getGameType() == GameType.Visual) {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            } else {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.visual),
                contentDescription = "Visual",
                modifier = Modifier
                    .height(48.dp)
                    .aspectRatio(3f / 2f)
            )
        }
    }
}

@Composable
fun StartButton(
    navigate: () -> Unit,
    height: Int,
    padding: Int,
    fillFraction: Float,
) {
    Box(
        modifier = Modifier
            .padding(padding.dp)
            .fillMaxWidth(fillFraction)
            .height(height.dp)
            .background(
                MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { navigate.invoke() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Start Game".uppercase(),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun SettingInformationText(
    mode: String,
    n: Int,
    eventDelay: Long,
    nrOfEvents: Int
) {
    Column {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "-- Settings --",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Mode: $mode",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Value of N: $n",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Event delay: $eventDelay ms",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Number of events: $nrOfEvents",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}


@Preview
@Composable
fun HomeScreenPreview() {
    // Since I am injecting a VM into my homescreen that depends on Application context, the preview doesn't work.
    Surface() {
        //HomeScreen(FakeVM())
    }
}