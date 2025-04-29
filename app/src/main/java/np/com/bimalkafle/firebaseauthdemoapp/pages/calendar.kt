package np.com.bimalkafle.firebaseauthdemoapp.pages

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EventCalendar(navController: NavController) {
    val db = Firebase.firestore
    val today = LocalDate.now()
    val oneWeekFromToday = today.plusDays(6)
    var editingEvent by remember { mutableStateOf<Pair<String, String>?>(null) }
    var selectedDate by remember { mutableStateOf(today) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var eventDescription by remember { mutableStateOf(TextFieldValue()) }
    var events by remember { mutableStateOf<Map<LocalDate, MutableList<Pair<String, String>>>>(emptyMap()) }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun loadEvents() {
        db.collection("events").get()
            .addOnSuccessListener { result ->
                val fetchedEvents = mutableMapOf<LocalDate, MutableList<Pair<String, String>>>()
                for (document in result) {
                    val id = document.id
                    val dateStr = document.getString("date")
                    val desc = document.getString("description")
                    if (dateStr != null && desc != null) {
                        val parsedDate = LocalDate.parse(dateStr)
                        if (parsedDate.isBefore(today)) {
                            db.collection("events").document(id).delete()
                        } else if (!parsedDate.isAfter(oneWeekFromToday)) {
                            fetchedEvents.getOrPut(parsedDate) { mutableListOf() }.add(id to desc)
                        }
                    }
                }
                events = fetchedEvents
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching documents", e)
            }
    }

    LaunchedEffect(Unit) {
        loadEvents()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("home")
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text("              Event Calendar")
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFFEFF1FC))
            )
        },
        content = { innerPadding ->
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            keyboardController?.hide()
                        })
                    }
                    .padding(16.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Previous Month")
                    }
                    Text(
                        text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Next Month")
                    }
                }

                MonthViewCalendar(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    today = today,
                    events = events,
                    onDateSelected = { selectedDate = it }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Selected Date: ${selectedDate.format(DateTimeFormatter.ISO_DATE)}")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add Event", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        BasicTextField(
                            value = eventDescription,
                            onValueChange = { eventDescription = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFEFF1FC), shape = MaterialTheme.shapes.small)
                                .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.small)
                                .padding(12.dp),
                            decorationBox = { innerTextField ->
                                if (eventDescription.text.isEmpty()) {
                                    Text("Enter event description...", color = Color.Gray)
                                }
                                innerTextField()
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val desc = eventDescription.text.trim()
                                if (desc.isNotEmpty()) {
                                    val dateStr = selectedDate.toString()
                                    val newEvent = hashMapOf("date" to dateStr, "description" to desc)

                                    editingEvent?.let { (id, _) ->
                                        db.collection("events").document(id).delete()
                                        events = events.mapValues { (date, list) ->
                                            if (date == selectedDate) list.filterNot { it.first == id }.toMutableList() else list
                                        }
                                        editingEvent = null
                                    }

                                    db.collection("events")
                                        .add(newEvent)
                                        .addOnSuccessListener { docRef ->
                                            val updated = events.toMutableMap()
                                            updated.getOrPut(selectedDate) { mutableListOf() }.add(docRef.id to desc)
                                            events = updated
                                            loadEvents() // Ensure refresh
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Firestore", "Failed to add event", e)
                                        }

                                    eventDescription = TextFieldValue()
                                    keyboardController?.hide()
                                }
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D9FBE))
                        ) {
                            Text(if (editingEvent != null) "Save" else "Add Event")
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF1FC)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Events", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (events.isEmpty()) {
                            Text("No events added.")
                        } else {
                            val sortedEvents = events.toSortedMap()
                            sortedEvents.forEach { (date, descList) ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = date.format(DateTimeFormatter.ISO_DATE),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    descList.forEach { (id, desc) ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "• $desc",
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(onClick = {
                                                eventDescription = TextFieldValue(desc)
                                                selectedDate = date
                                                editingEvent = id to desc
                                            }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                                            }
                                            IconButton(onClick = {
                                                db.collection("events").document(id).delete()
                                                    .addOnSuccessListener { loadEvents() }
                                                val updated = events.toMutableMap()
                                                updated[date]?.removeIf { it.first == id }
                                                if (updated[date]?.isEmpty() == true) updated.remove(date)
                                                events = updated
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthViewCalendar(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    events: Map<LocalDate, MutableList<Pair<String, String>>>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value % 7)
    val totalCells = 42

    val dayCells = buildList<LocalDate?> {
        repeat(firstDayOfWeek) { add(null) }
        for (day in 1..daysInMonth) add(currentMonth.atDay(day))
        while (size < totalCells) add(null)
    }

    Column {
        Row(Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        for (week in 0 until 6) {
            Row(Modifier.fillMaxWidth()) {
                for (day in 0 until 7) {
                    val date = dayCells[week * 7 + day]
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(1.dp)
                            .clickable(enabled = date != null) {
                                date?.let { onDateSelected(it) }
                            }
                            .background(
                                when (date) {
                                    selectedDate -> Color(0xFF90CAF9)
                                    today -> Color(0xFFE3F2FD)
                                    else -> Color.Transparent
                                }
                            )
                            .border(0.5.dp, Color.LightGray)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = date?.dayOfMonth?.toString() ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (date != null) Color.Black else Color.Gray
                            )
                            if (date != null && events.containsKey(date)) {
                                Box(
                                    Modifier
                                        .size(5.dp)
                                        .background(Color.Red, shape = MaterialTheme.shapes.small)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
