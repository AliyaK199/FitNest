package np.com.bimalkafle.firebaseauthdemoapp.pages

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunicationPage(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var emergencyNumber by remember { mutableStateOf("") }
    var emergencyName by remember { mutableStateOf("") }

    val emergencyContacts = listOf(
        "911" to "Police",
        "112" to "Ambulance",
        "100" to "Fire Brigade"
    )

    var successMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val savedData = remember { mutableStateListOf<Pair<String, Map<String, Any>>>() }

    fun fetchSavedData() {
        FirebaseFirestore.getInstance()
            .collection("communication_data")
            .get()
            .addOnSuccessListener { result ->
                savedData.clear()
                for (document in result) {
                    savedData.add(document.id to document.data)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }

    LaunchedEffect(Unit) {
        fetchSavedData()
    }

    val backgroundColor = Color(0xFFF2E9E4)
    val primaryTextColor = Color(0xFF3A3C42)
    val cardColor = Color.White
    val accentColor = Color(0xFFB7977F)
    val accentColor2 = Color(0xFFFEBB6B)
    val successColor = Color(0xFF2E7D32)

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 1.dp, vertical = 35.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }

                Text(
                    text = "           \n Communication Options",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        letterSpacing = 1.5.sp,
                        fontSize = 24.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 30.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                CommOptionCard("Save Emergency Email", "Enter your email for secure communication.", cardColor, primaryTextColor) {
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.White,
                            focusedIndicatorColor = accentColor,
                            cursorColor = accentColor
                        )
                    )
                    Button(
                        onClick = {
                            val isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches()
                            if (isValidEmail) {
                                saveToFirebase("email", email, {
                                    successMessage = "Email saved successfully."
                                    errorMessage = ""
                                    email = ""
                                    fetchSavedData()
                                }, {
                                    errorMessage = "Failed to save email."
                                    successMessage = ""
                                })
                            } else {
                                errorMessage = "Please enter a valid email address."
                                successMessage = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor2)
                    ) {
                        Text("Save Email", color = Color.Black)
                    }
                }

                CommOptionCard("Emergency Hotlines", "Tap to select an emergency service.", cardColor, primaryTextColor) {
                    Column {
                        emergencyContacts.forEach { (number, label) ->
                            val isSelected = emergencyNumber == "$number - $label"
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        emergencyNumber = "$number - $label"
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:$number")
                                        }
                                        context.startActivity(intent)
                                    },
                                colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFB3E5FC) else Color.White)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("$number - $label")
                                }
                            }
                        }
                    }
                }

                CommOptionCard("Save Your Emergency Contact", "Type a name and number to use in emergencies.", cardColor, primaryTextColor) {
                    TextField(
                        value = emergencyName,
                        onValueChange = { emergencyName = it },
                        label = { Text("Contact Name") },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.White,
                            focusedIndicatorColor = accentColor,
                            cursorColor = accentColor
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = emergencyNumber,
                        onValueChange = { emergencyNumber = it },
                        label = { Text("Contact Number") },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.White,
                            focusedIndicatorColor = accentColor,
                            cursorColor = accentColor
                        )
                    )
                    Button(
                        onClick = {
                            val isValidPhone = emergencyNumber.matches(Regex("^\\+?[0-9]{7,15}\$"))
                            if (isValidPhone && emergencyName.isNotBlank()) {
                                val data = hashMapOf(
                                    "emergencyName" to emergencyName,
                                    "emergencyNumber" to emergencyNumber,
                                    "timestamp" to System.currentTimeMillis()
                                )
                                FirebaseFirestore.getInstance().collection("communication_data")
                                    .add(data)
                                    .addOnSuccessListener {
                                        successMessage = "Emergency contact saved successfully."
                                        errorMessage = ""
                                        emergencyName = ""
                                        emergencyNumber = ""
                                        fetchSavedData()
                                    }
                                    .addOnFailureListener {
                                        errorMessage = "Failed to save emergency contact."
                                        successMessage = ""
                                    }
                            } else {
                                errorMessage = "Enter a valid name and phone number (7–15 digits)."
                                successMessage = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor2)
                    ) {
                        Text("Save Contact", color = Color.Black)
                    }
                }

                if (successMessage.isNotEmpty()) Text(successMessage, color = successColor)
                if (errorMessage.isNotEmpty()) Text(errorMessage, color = MaterialTheme.colorScheme.error)

                val emailList = savedData.filter { it.second.containsKey("email") }
                val contactList = savedData.filter { it.second.containsKey("emergencyNumber") && it.second.containsKey("emergencyName") }

                if (emailList.isNotEmpty() || contactList.isNotEmpty()) {
                    Text("--------Saved Data--------", style = MaterialTheme.typography.titleLarge)

                    if (emailList.isNotEmpty()) {
                        Text("Email Info", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                        emailList.forEach { (docId, item) ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${item["email"]}", modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:${item["email"]}")
                                        }
                                        context.startActivity(intent)
                                    }) {
                                        Icon(Icons.Default.Email, contentDescription = "Email")
                                    }
                                    IconButton(onClick = {
                                        FirebaseFirestore.getInstance().collection("communication_data")
                                            .document(docId)
                                            .delete()
                                            .addOnSuccessListener {
                                                successMessage = "Email deleted successfully."
                                                errorMessage = ""
                                                fetchSavedData()
                                            }
                                            .addOnFailureListener {
                                                errorMessage = "Failed to delete email."
                                                successMessage = ""
                                            }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (contactList.isNotEmpty()) {
                        Text("Contact Info", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                        contactList.forEach { (docId, item) ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${item["emergencyName"]}", fontWeight = FontWeight.Bold)
                                            Text("${item["emergencyNumber"]}")
                                        }
                                        IconButton(onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:${item["emergencyNumber"]}")
                                            }
                                            context.startActivity(intent)
                                        }) {
                                            Icon(Icons.Default.Call, contentDescription = "Call")
                                        }
                                        IconButton(onClick = {
                                            FirebaseFirestore.getInstance().collection("communication_data")
                                                .document(docId)
                                                .delete()
                                                .addOnSuccessListener {
                                                    successMessage = "Contact deleted successfully."
                                                    errorMessage = ""
                                                    fetchSavedData()
                                                }
                                                .addOnFailureListener {
                                                    errorMessage = "Failed to delete contact."
                                                    successMessage = ""
                                                }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun CommOptionCard(
    title: String,
    description: String,
    cardColor: Color,
    titleColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(color = titleColor, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

private fun saveToFirebase(
    fieldName: String,
    fieldValue: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val data = hashMapOf(fieldName to fieldValue, "timestamp" to System.currentTimeMillis())

    db.collection("communication_data")
        .add(data)
        .addOnSuccessListener {
            Log.d("Firestore", "$fieldName saved")
            onSuccess()
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error saving $fieldName", e)
            onFailure(e)
        }
}
