package np.com.bimalkafle.firebaseauthdemoapp.pages


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.runtime.SideEffect

// A very simple Material3 color scheme
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    background = Color(0xFFF2F2F2),
    onBackground = Color.Black
)

@Composable
fun NotesTheme(content: @Composable () -> Unit) {
//    val systemUiController = rememberSystemUiController()

    // Apply white system bar with dark icons for contrast
//    SideEffect {
//        systemUiController.setSystemBarsColor(
//            color = LightColorScheme.background,
//            darkIcons = true
//        )
//    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}

@Composable
fun HomeScreen(onGoToNotes: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = onGoToNotes) {
            Text("Go to Notes")
        }
    }
}

@Composable
fun EmptyNotesPlaceholder(onAddNote: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No notes yet!", fontSize = 50.sp)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onAddNote,
            modifier = Modifier
                .fillMaxWidth(0.8f) // wider button, adjust as needed
                .height(60.dp),     // taller button
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFBEBACE),
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "Add New Note",
                fontSize = 22.sp // 👈 bigger text
            )
        }

    }
}

