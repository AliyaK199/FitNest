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

val LightjColors = listOf(
    Color(0xFFFFCDD2), Color(0xFFBBDEFB), Color(0xFFC8E6C9), Color(0xFFFFF9C4),
    Color(0xFFFFE0B2), Color(0xFFE1BEE7), Color(0xFFB2DFDB), Color(0xFFD7CCC8),
    Color(0xFFEEEEEE), Color(0xFFB2EBF2)
)

@Composable
fun EmptyMedicinePlaceholder(onAddMedicine: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Add your Medicine", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddMedicine) {
            Text("Add Medicine")
        }
    }
}