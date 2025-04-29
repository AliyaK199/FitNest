package np.com.bimalkafle.firebaseauthdemoapp.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

val CustomTypography = Typography(
    headlineSmall = TextStyle(fontSize = 50.sp),
    titleLarge     = TextStyle(fontSize = 40.sp),
    titleMedium    = TextStyle(fontSize = 35.sp),  // cards’ title
    bodyLarge      = TextStyle(fontSize = 25.sp),  // cards’ body
    bodySmall      = TextStyle(fontSize = 14.sp)   // dialog text
)

/**
 * A centered, semi‑transparent short divider.
 * Use inside a Column: SectionDivider()
 */
@Composable
fun ColumnScope.SectionDivider() {
    Divider(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .align(Alignment.CenterHorizontally)
            .padding(vertical = 4.dp),
        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
        thickness = 1.dp
    )
}
@Composable
fun SummaryCard(
    totalCalories: Int,
    goal: Int = 2100,
    burned: Int = 267,
    onAddCaloriesClick: (Int) -> Unit
) {
    var showAddCaloriesDialog by remember { mutableStateOf(false) }
    var calorieInput by remember { mutableStateOf("") }


    val remaining = goal - totalCalories
    val progress = totalCalories.toFloat() / goal

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC2D0D1)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { showAddCaloriesDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Calories")
                }
            }


            CircularProgressIndicator(
                progress = progress.coerceAtMost(1f),
                color = Color(0xFFAC8260),
                strokeWidth = 6.dp,
                modifier = Modifier
                    .size(64.dp)
                    .padding(vertical = 1.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("$totalCalories of $goal kcal", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(4.dp))
        }
    }


    if (showAddCaloriesDialog) {
        AlertDialog(
            onDismissRequest = { showAddCaloriesDialog = false },
            title = { Text("Add Calories") },
            text = {
                OutlinedTextField(
                    value = calorieInput,
                    onValueChange = { calorieInput = it },
                    label = { Text("Calories") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val calories = calorieInput.toIntOrNull()
                    if (calories != null && calories > 0) {
                        onAddCaloriesClick(calories)  // Add calories using the function
                        calorieInput = ""
                        showAddCaloriesDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCaloriesDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
@Composable
fun MacroStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
@Composable
fun LabeledText(label: String, value: String) {
    Column {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
        )
    }
}

@Composable
fun AddMealCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(width = 240.dp, height = 320.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD6C2B5)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(48.dp))
        }
    }
}