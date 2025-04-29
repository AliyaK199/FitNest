package np.com.bimalkafle.firebaseauthdemoapp.pages

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.TimePicker
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import np.com.bimalkafle.firebaseauthdemoapp.R
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Medicine(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var medicineList by remember { mutableStateOf(listOf<Medicine>()) }
    var selectedMedicine by remember { mutableStateOf<Medicine?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(Calendar.getInstance()) }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val bannerHeight = screenHeight * 0.3f

    // Fetch data from Firestore
    LaunchedEffect(Unit) {
        db.collection("medicine")
            .get()
            .addOnSuccessListener { result ->
                medicineList = result.mapNotNull { it.toObject(Medicine::class.java) }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Banner with background image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bannerHeight)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFF2E9E4), Color(0xFFF2E9E4))
                        )
                    )
                    .paint(
                        painterResource(id = R.drawable.mg), // Your drawable resource
                        contentScale = ContentScale.Crop
                    )
            )
            {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),

                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(1.dp), // Padding to give some space from the edges
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = {
                            navController.navigate("home") // Navigate to the home screen
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }

                    Text(
                        text = "Your Medicines",
                        color = Color.Black,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold

                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Stay on track with your health",
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                }
            }

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF2E9E4))
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                val interactionSource = remember { MutableInteractionSource() }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFD8B65A), Color(0xFF8F6E2A))
                            )
                        )

                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            selectedMedicine = null
                            showDialog = true
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp), // padding comes after clickable
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Medicine",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))


                medicineList.forEach { medicine ->
                    MedicationCard(
                        name = medicine.name,
                        dosage = medicine.dosage,
                        time = medicine.time,
                        taken = medicine.taken,
                        onClick = {
                            selectedMedicine = medicine
                            showDialog = true
                        },
                        onToggleTaken = {
                            val updated = medicine.copy(taken = !medicine.taken)
                            db.collection("medicine").document(medicine.name)
                                .set(updated)
                                .addOnSuccessListener {
                                    medicineList = medicineList.map {
                                        if (it.name == medicine.name) updated else it
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Failed to update taken status", e)
                                }
                        }
                    )
                }
            }
        }

        if (showDialog) {
            MedicationDialog(
                medicine = selectedMedicine,
                selectedTime = selectedTime,
                onDismiss = { showDialog = false },
                onTimePick = { hour, minute ->
                    selectedTime.set(Calendar.HOUR_OF_DAY, hour)
                    selectedTime.set(Calendar.MINUTE, minute)
                },
                onSave = { med ->
                    val newMedicine = med.copy(time = String.format("%02d:%02d", selectedTime.get(Calendar.HOUR_OF_DAY), selectedTime.get(Calendar.MINUTE)))
                    val docRef = db.collection("medicine").document(newMedicine.name)

                    docRef.set(newMedicine)
                        .addOnSuccessListener {
                            medicineList = if (selectedMedicine == null) {
                                medicineList + newMedicine
                            } else {
                                medicineList.map { m -> if (m.name == selectedMedicine?.name) newMedicine else m }
                            }
                            scheduleNotification(context, selectedTime, newMedicine.name)
                            showDialog = false
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error writing document", e)
                        }
                },
                onDelete = { med ->
                    deleteMedicine(med)
                    medicineList = medicineList.filterNot { it.name == med.name }
                    showDialog = false
                }
            )
        }
    }
}


fun deleteMedicine(medicine: Medicine) {
    val db = FirebaseFirestore.getInstance()
    db.collection("medicine").document(medicine.name)
        .delete()
        .addOnSuccessListener {
            Log.d("Firestore", "Medicine deleted successfully")
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error deleting document", e)
        }
}

@Composable
fun MedicationDialog(
    medicine: Medicine?,
    selectedTime: Calendar,
    onDismiss: () -> Unit,
    onTimePick: (Int, Int) -> Unit,
    onSave: (Medicine) -> Unit,
    onDelete: (Medicine) -> Unit
) {
    val context = LocalContext.current
    var tempName by remember { mutableStateOf(medicine?.name ?: "") }
    var tempDosage by remember { mutableStateOf(medicine?.dosage ?: "") }
    var tempFrequency by remember { mutableStateOf(medicine?.dosage ?: "") }
    var tempInstructions by remember { mutableStateOf(medicine?.instructions ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (medicine == null) "Add Medication" else "Edit Medication",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Medicine Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tempDosage,
                    onValueChange = { tempDosage = it },
                    label = { Text("Dosage") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tempFrequency,
                    onValueChange = { tempFrequency = it },
                    label = { Text("Frequency") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tempInstructions,
                    onValueChange = { tempInstructions = it },
                    label = { Text("Instructions") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _: TimePicker, hour: Int, minute: Int ->
                                onTimePick(hour, minute)
                            },
                            selectedTime.get(Calendar.HOUR_OF_DAY),
                            selectedTime.get(Calendar.MINUTE),
                            false
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8F6E2A), // Background color
                        contentColor = Color.Black          // Text color
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Pick Reminder Time")
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp)
            ) {
                if (medicine != null) {
                    TextButton(onClick = { onDelete(medicine) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8F6E2A),  // Background color (change it to the color you want)
                            contentColor = Color.Black   // Text color
                        )
                    ) {
                        Text("Delete", color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8F6E2A), // Background color
                        contentColor = Color.Black          // Text color
                    )
                ){
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(4.dp))

                Button(onClick = {
                    val updatedMedicine = Medicine(
                        name = tempName,
                        dosage = tempDosage,
                        time = String.format("%02d:%02d", selectedTime.get(Calendar.HOUR_OF_DAY), selectedTime.get(Calendar.MINUTE)),
                        taken = false,
                        instructions = tempInstructions
                    )
                    onSave(updatedMedicine)
                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8F6E2A), // Background color
                        contentColor = Color.Black          // Text color
                    )) {
                    Text("Save")
                }
            }
        }
    )
}


fun scheduleNotification(context: Context, calendar: Calendar, title: String) {
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("title", title)
    }
    val requestCode = Random().nextInt() // Use a random request code for uniqueness
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val currentTime = System.currentTimeMillis()

    // Check if the app can schedule exact alarms
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
            // Show an alert to the user that permission is required
            Toast.makeText(context, "Please enable exact alarm permissions in settings.", Toast.LENGTH_LONG).show()
            return
        }
    }

    if (calendar.timeInMillis > currentTime) {
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        Log.d("Notification", "Scheduled at: ${calendar.time}")
    } else {
        Log.d("Notification", "Cannot schedule notification in the past.")
    }
}



class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"

        // Check for permission before posting notification (for Android 13+)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle the case where permission is not granted
            return
        }

        // Create a Notification Channel for Android 8.0+
        val channelId = "med_channel"
        val channelName = "Medication Reminder"
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Channel for medication reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        // Build the notification
        val builder = NotificationCompat.Builder(context, "med_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Medication Reminder")
            .setContentText("Time to take $title")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            notify(Random().nextInt(), builder.build())
        }
    }
}


data class Medicine(
    val name: String = "",
    val dosage: String = "",
    val time: String = "",
    val taken: Boolean = false,
    val instructions: String = ""
)

@Composable
fun MedicationCard(
    name: String,
    dosage: String,
    time: String,
    taken: Boolean,
    onClick: () -> Unit,
    onToggleTaken: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (taken) Color(0xFF9D9FBE) else Color (0xFFC2D0D1)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Dosage: $dosage", fontSize = 14.sp)
                Text(text = "Time: $time", fontSize = 14.sp)
            }

            IconToggleButton(checked = taken, onCheckedChange = { onToggleTaken() }) {
                Icon(
                    imageVector = if (taken) Icons.Default.Check else Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (taken) Color.White else Color(0xFF1A1A7C)
                )
            }
        }
    }
}
