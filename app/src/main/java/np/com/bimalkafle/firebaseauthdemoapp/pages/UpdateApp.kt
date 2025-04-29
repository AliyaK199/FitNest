package np.com.bimalkafle.firebaseauthdemoapp.pages


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
//import com.example.notes.ui.theme.MasonryGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.navigation.NavController

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import np.com.bimalkafle.firebaseauthdemoapp.R

// Simple data model
data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val colorHex: ULong = Color.White.value.toULong()
)

// A small palette for new notes
private val LightColors = listOf(
    Color(0xFF84675B),
    Color(0xFFA59A8D),
    Color(0xFFC2D0D1),
    Color(0xFFA1B6BE),
    Color(0xFF9D9FBE),
    Color(0xFFD9C1B0),
    Color(0xFFBEBACE),
    Color(0xFFAC8260)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesUpdate(navController: NavController, onBack: () -> Unit) {
    val notesCollection = FirebaseFirestore.getInstance().collection("notes")
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }

    // Listen for Firestore updates
    DisposableEffect(Unit) {
        val reg: ListenerRegistration = notesCollection
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    notes = it.documents.mapNotNull { doc ->
                        val t = doc.getString("title") ?: ""
                        val c = doc.getString("content") ?: ""
                        val col = doc.getLong("color") ?: Color.White.value.toLong()
                        Note(doc.id, t, c, col.toULong())
                    }
                }
            }
        onDispose { reg.remove() }
    }

    Box(Modifier.fillMaxSize()) {
        // 1) Full‐screen background image
        Image(
            painter           = painterResource(id = R.drawable.screen),
            contentDescription= null,
            contentScale      = ContentScale.Crop,
            modifier          = Modifier.matchParentSize()
        )

        // 2) Overlay a transparent Scaffold on top
        Scaffold(
            // make the body & slots transparent so bg shines through
            containerColor     = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    // override the default AppBar colors
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,    // no fill
                        scrolledContainerColor = Color.Transparent
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    title = {
                        Text(
                            text = "NOTES",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Text("+")
                }
            },
            content = { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (notes.isEmpty()) {
                        EmptyNotesPlaceholder { showDialog = true }
                    } else {
                        MasonryGrid(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp), // optional inner padding
                            columns = 2,
                            spacing = 8.dp
                        ) {
                            notes.forEach { note ->
                                NoteItem(note = note, onClick = { selectedNote = note })
                            }
                        }
                    }
                }
            }

        )
    }

    // Dialog for new note
    if (showDialog) {
        NoteDialog(
            title = "",
            content = "",
            color = LightColors.first(),
            onDismiss = { showDialog = false },
            onSave    = { title, content, color ->
                val noteMap = mapOf(
                    "title"     to title,
                    "content"   to content,
                    "color"     to color.value.toLong(),
                    "timestamp" to System.currentTimeMillis()
                )
                notesCollection.add(noteMap)
                showDialog = false
            }
        )
    }

    // Dialog for edit/delete
    selectedNote?.let { note ->
        NoteDialog(
            title = note.title,
            content = note.content,
            color = Color(note.colorHex),
            onDismiss = { selectedNote = null },
            onSave = { t, c, col ->
                notesCollection.document(note.id).update(
                    mapOf(
                        "title"   to t,
                        "content" to c,
                        "color"   to col.value.toLong()
                    )
                )
                selectedNote = null
            },
            onDelete = {
                notesCollection.document(note.id).delete()
                selectedNote = null
            }
        )
    }
}

@Composable
fun NoteItem(note: Note, onClick: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(note.colorHex))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(note.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(note.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDialog(
    title: String,
    content: String,
    color: Color,
    onDismiss: () -> Unit,
    onSave:    (String, String, Color) -> Unit,
    onDelete:  (() -> Unit)? = null
) {
    var titleState by remember { mutableStateOf(title) }
    var contentState by remember { mutableStateOf(content) }
    var selectedColor by remember { mutableStateOf(color) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Notes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text    = {
            Column {
                OutlinedTextField(
                    value       = titleState,
                    onValueChange = { titleState = it },
                    label       = { Text("Title") },
                    modifier    = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value       = contentState,
                    onValueChange = { contentState = it },
                    label       = { Text("Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 5
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Color:")
                    Spacer(Modifier.width(8.dp))
                    ColorDropdown(
                        selectedColor  = selectedColor,
                        onColorSelected = { selectedColor = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(titleState, contentState, selectedColor) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                onDelete?.let {
                    TextButton(onClick = it) { Text("Delete") }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
fun ColorDropdown(selectedColor: Color, onColorSelected: (Color) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        Modifier
            .size(24.dp)
            .background(selectedColor)
            .clickable { expanded = true }
    ) {
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            LightColors.forEach { color ->
                DropdownMenuItem(
                    text = { Box(Modifier.size(20.dp).background(color)) },
                    onClick = {
                        onColorSelected(color)
                        expanded = false
                    }
                )
            }
        }
    }
}