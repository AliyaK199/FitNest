package np.com.bimalkafle.firebaseauthdemoapp.pages



data class VitalDisplay(
    val label: String,    // Name of the vital sign
    val icon: String,     // Icon for the vital sign (like ❤️, 🌡, etc.)
    val value: String,    // Displayed value (e.g., 72 bpm, 98°F)
    val progress: Float,  // Progress bar value, calculated as a float (0-1)
    val color: androidx.compose.ui.graphics.Color
)
