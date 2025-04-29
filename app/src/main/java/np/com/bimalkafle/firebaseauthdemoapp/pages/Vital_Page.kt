package np.com.bimalkafle.firebaseauthdemoapp.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun VitalsPage(userId: Int = 1, viewModel: VitalViewModel = viewModel(), navController: NavController) {
    val latest = viewModel.latestVitals

    var pulse by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var glucose by remember { mutableStateOf("") }
    var spo2 by remember { mutableStateOf("") }
    var respiratoryRate by remember { mutableStateOf("") }
    var bloodPressure by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    val colorTheme = ColorTheme()
    var currentColor by remember { mutableStateOf(colorTheme.color1.copy(alpha = 0.9f)) }
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(currentColor, colorTheme.color1.copy(alpha = 0.6f))
    )

    LaunchedEffect(Unit) {
        viewModel.fetchLatestVitals(userId)
    }

    val healthScore = calculateHealthScore(
        pulse = latest?.pulse ?: 0,
        temperature = latest?.temperature ?: 0.0,
        glucose = latest?.glucose ?: 0,
        spo2 = latest?.spo2 ?: 0,
        respiratoryRate = latest?.respiratoryRate ?: 0,
        bloodPressure = latest?.bloodPressure ?: 0
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
            .padding(24.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 0) {
                        currentColor = colorTheme.color3.copy(alpha = 0.8f)
                    } else {
                        currentColor = colorTheme.color1.copy(alpha = 0.9f)
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Health Score Circle
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Gray.copy(alpha = 0.5f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = healthScore.toFloat(),
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 8.dp,
                    color = Color(0xFF000080),
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
                Text(
                    text = "${(healthScore * 100).toInt()}%",
                    color = Color.Black,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )
            }

            GradientTitle(text = "Your Vital Signs", colorTheme)

            Spacer(modifier = Modifier.height(20.dp))

            if (latest != null) {
                val vitalList = listOf(
                    VitalDisplay("Pulse", "❤️", "${latest.pulse} bpm", (latest.pulse / 120f) * 0.8f, colorTheme.color5.copy(alpha = 0.7f)),
                    VitalDisplay("Temperature", "🌡", "${latest.temperature}°C", (latest.temperature - 35) / 5f, colorTheme.color6.copy(alpha = 0.7f)),
                    VitalDisplay("Glucose", "🩸", "${latest.glucose} mg/dL", latest.glucose / 200f, colorTheme.color7.copy(alpha = 0.7f)),
                    VitalDisplay("SpO₂", "🫁", "${latest.spo2}%", latest.spo2 / 100f, colorTheme.color8.copy(alpha = 0.7f)),
                    VitalDisplay("Respiratory Rate", "🌬", "${latest.respiratoryRate} bpm", (latest.respiratoryRate / 30f) * 0.8f, colorTheme.color9.copy(alpha = 0.7f)),
                    VitalDisplay("Blood Pressure", "🩺", "${latest.bloodPressure} mmHg", 0.85f, colorTheme.color10.copy(alpha = 0.7f))
                )

                vitalList.forEach {
                    VitalRow("${it.icon} ${it.label}", it.value, it.progress.coerceIn(0f, 1f), it.color)
                }
            } else {
                CircularProgressIndicator(color = Color(0xFF6A11CB))
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    pulse = ""; temperature = ""; glucose = ""; spo2 = ""
                    respiratoryRate = ""; bloodPressure = ""
                    errorMessage = ""; successMessage = ""
                    navController.navigate("home")
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorTheme.color5.copy(alpha = 0.8f),
                    contentColor = Color.Black
                )
            ) {
                Text("Back to Home")
            }
        }
    }
}

@Composable
fun GradientTitle(text: String, colorTheme: ColorTheme) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            color = Color.DarkGray, // 🎯 Use a dark solid color here
            style = TextStyle(
                fontFamily = FontFamily.Cursive
            )
        )
    }
}


@Composable
fun VitalRow(label: String, value: String, progress: Float, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(vertical = 16.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF222222) // darkened label
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111111) // darkened value
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = Color.White.copy(alpha = 0.2f)
        )
    }
}


fun calculateHealthScore(
    pulse: Int,
    temperature: Number,
    glucose: Int,
    spo2: Int,
    respiratoryRate: Int,
    bloodPressure: Comparable<*>
): Float {
    val temp = temperature.toDouble()
    val bp = bloodPressure.toString().toIntOrNull() ?: 0

    fun scoreInRange(value: Float, min: Float, max: Float): Float {
        return when {
            value in min..max -> 1f
            value in (min - 5)..(max + 5) -> 0.8f
            else -> 0.6f
        }
    }

    val scorePulse = scoreInRange(pulse.toFloat(), 60f, 100f)
    val scoreTemp = scoreInRange(temp.toFloat(), 36.1f, 37.2f)
    val scoreGlucose = scoreInRange(glucose.toFloat(), 70f, 140f)
    val scoreSpo2 = when {
        spo2 >= 95 -> 1f
        spo2 in 90..94 -> 0.8f
        else -> 0.6f
    }
    val scoreResp = scoreInRange(respiratoryRate.toFloat(), 12f, 20f)
    val scoreBP = scoreInRange(bp.toFloat(), 90f, 120f)

    val averageScore = (scorePulse + scoreTemp + scoreGlucose + scoreSpo2 + scoreResp + scoreBP) / 6f

    // Scale so all normals result in 0.8 and up
    return (averageScore * 100).coerceIn(0f, 100f) / 100f
}
