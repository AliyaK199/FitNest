package np.com.bimalkafle.firebaseauthdemoapp.pages

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VitalSignInputScreen(userId: Int, navController: NavController) {
    var pulse by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var glucose by remember { mutableStateOf("") }
    var spo2 by remember { mutableStateOf("") }
    var respiratoryRate by remember { mutableStateOf("") }
    var bloodPressure by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    // Get the keyboard controller
    val keyboardController = LocalSoftwareKeyboardController.current

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFEFDDE0), // soft blush
            Color(0xFFF6D3E1) // pink
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
            .padding(16.dp)
            .pointerInteropFilter {
                // Dismiss the keyboard when tapping outside of the text fields
                keyboardController?.hide()
                false // Pass touch event to other views
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color(0xFFE91E63),
                modifier = Modifier
                    .size(64.dp)
                    .padding(top = 28.dp, bottom = 8.dp)
            )

            Text(
                text = "Vital Sign Entry",
                fontSize = 36.sp,
                color = Color(0xFF5D3B66),
                style = TextStyle(
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp,
                    lineHeight = 40.sp,
                    shadow = Shadow(
                        color = Color(0xFF888888),
                        offset = Offset(2f, 2f),
                        blurRadius = 6f
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )

            Icon(
                imageVector = Icons.Filled.FavoriteBorder,
                contentDescription = null,
                tint = Color(0xFFD6C1CF),
                modifier = Modifier
                    .size(32.dp)
                    .padding(bottom = 12.dp)
            )

            InputField("Pulse (beats/min)", pulse, { pulse = it }, "Invalid pulse value")
            Spacer(modifier = Modifier.height(12.dp))
            InputField("Temperature (°C)", temperature, { temperature = it }, "Invalid temperature value")
            Spacer(modifier = Modifier.height(12.dp))
            InputField("Glucose (mg/dL)", glucose, { glucose = it }, "Invalid glucose value")
            Spacer(modifier = Modifier.height(12.dp))
            InputField("SPO2 (%)", spo2, { spo2 = it }, "Invalid SPO2 value")
            Spacer(modifier = Modifier.height(12.dp))
            InputField("Respiratory Rate (breaths/min)", respiratoryRate, { respiratoryRate = it }, "Invalid respiratory rate")
            Spacer(modifier = Modifier.height(12.dp))
            InputField(
                label = "Blood Pressure (mmHg)",
                value = bloodPressure,
                onValueChange = { bloodPressure = it },
                errorMessage = "Invalid blood pressure format (expected systolic/diastolic)",
                isBloodPressure = true,
                icon = Icons.Default.FavoriteBorder
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
            }

            if (successMessage.isNotEmpty()) {
                Text(successMessage, color = Color(0xFF4A148C), modifier = Modifier.padding(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val pulseValue = pulse.toDoubleOrNull()
                    val tempValue = temperature.toDoubleOrNull()
                    val glucoseValue = glucose.toDoubleOrNull()
                    val spo2Value = spo2.toDoubleOrNull()
                    val respiratoryRateValue = respiratoryRate.toDoubleOrNull()
                    val bloodPressureValid =
                        bloodPressure.matches(Regex("^\\d{2,3}/\\d{2,3}$")) || bloodPressure.toDoubleOrNull() != null

                    if (listOf(
                            pulseValue, tempValue, glucoseValue, spo2Value, respiratoryRateValue
                        ).any { it == null } || !bloodPressureValid
                    ) {
                        errorMessage = "Please enter valid values for all fields"
                        successMessage = ""
                    } else {
                        errorMessage = ""
                        successMessage = ""

                        submitVitalSignToFirestore(
                            userId = userId.toString(),
                            pulse = pulseValue!!,
                            temperature = tempValue!!,
                            glucose = glucoseValue!!,
                            spo2 = spo2Value!!,
                            respiratoryRate = respiratoryRateValue!!,
                            bloodPressure = bloodPressure,
                            onSuccess = {
                                successMessage = "Vital signs submitted successfully."
                                pulse = ""; temperature = ""; glucose = ""; spo2 = ""
                                respiratoryRate = ""; bloodPressure = ""
                                navController.navigate("home")
                            },
                            onFailure = { e ->
                                errorMessage = "Error submitting data: ${e.localizedMessage}"
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB497BD),
                    contentColor = Color.Black
                )
            ) {
                Text("Submit")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    pulse = ""; temperature = ""; glucose = ""; spo2 = ""
                    respiratoryRate = ""; bloodPressure = ""
                    errorMessage = ""; successMessage = ""
                    navController.navigate("home")
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB497BD),
                    contentColor = Color.Black
                )
            ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "✨ Powered by FitNest ✨",
                color = Color(0xFF9C7CA5),
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

private fun submitVitalSignToFirestore(
    userId: String,
    pulse: Double,
    temperature: Double,
    glucose: Double,
    spo2: Double,
    respiratoryRate: Double,
    bloodPressure: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val vitalData = hashMapOf(
        "userId" to userId,
        "pulse" to pulse,
        "temperature" to temperature,
        "glucose" to glucose,
        "spo2" to spo2,
        "respiratoryRate" to respiratoryRate,
        "bloodPressure" to bloodPressure,
        "timestamp" to System.currentTimeMillis()
    )

    db.collection("vital_signs")
        .add(vitalData)
        .addOnSuccessListener {
            Log.d("Firestore", "Success!")
            onSuccess()
        }
        .addOnFailureListener { e -> onFailure(e) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String,
    icon: ImageVector = Icons.Default.Star,
    isBloodPressure: Boolean = false
) {
    val isError = if (isBloodPressure) {
        value.isNotEmpty() && !value.matches(Regex("^\\d{2,3}/\\d{2,3}$")) && value.toDoubleOrNull() == null
    } else {
        value.isNotEmpty() && value.toDoubleOrNull() == null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(vertical = 6.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = {
                Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF8A5A95))
            },
            isError = isError,
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color(0xFFEADDEF).copy(alpha = 0.8f),
                unfocusedIndicatorColor = Color(0xFFB497BD),
                focusedIndicatorColor = Color(0xFFD1A7F7),
                focusedPlaceholderColor = Color(0xFF4A148C),
                cursorColor = Color(0xFF6D4D6D)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(
                    color = Color.Transparent,
                    shape = MaterialTheme.shapes.medium
                )
        )

        if (isError) {
            Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview
@Composable
fun PreviewVitalSignInputScreen() {
    val navController = rememberNavController()
    VitalSignInputScreen(userId = 1, navController = navController)
}
