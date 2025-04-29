package np.com.bimalkafle.firebaseauthdemoapp.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun Chatbot(modifier: Modifier = Modifier, viewModel: ChatViewModel, navController: NavController) {
    val messages by viewModel.messageList.collectAsState()

    // LocalFocusManager to clear focus
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5E5DB))
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()  // Dismiss the keyboard when tapping outside
                })
            },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppHeader(navController = navController)  // Pass navController here
        MessageList(modifier = Modifier.weight(1f), messageList = messages)
        MessageInput(onMessageSend = { viewModel.sendMessage(it) })
    }
}

@Composable
fun MessageList(modifier: Modifier = Modifier, messageList: List<MessageModel>) {
    LazyColumn(
        modifier = modifier,
        reverseLayout = true
    ) {
        items(messageList.reversed()) { message ->
            MessageRow(messageModel = message)
        }
    }
}

@Composable
fun MessageRow(messageModel: MessageModel) {
    val isModel = messageModel.role == "model"
    val paddingStart = if (isModel) 8.dp else 70.dp
    val paddingEnd = if (isModel) 70.dp else 8.dp
    val backgroundColor = if (isModel) Color(0xFFE0F7FA) else Color(0xFFBBDEFB)  // Blue for model, light cyan for user
    val icon = Icons.Filled.Person  // Use default icons for user and bot

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = paddingStart, end = paddingEnd, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isModel) Arrangement.Start else Arrangement.End
    ) {
        // Avatar + Message
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = "Avatar", modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = backgroundColor,  // Use background color, no borders
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp)  // Padding inside the box
            ) {
                Text(
                    text = messageModel.message,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
                )
            }
        }
    }
}

@Composable
fun AppHeader(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Back Button
        IconButton(
            modifier = Modifier.align(Alignment.TopStart),
            onClick = {
                // Navigate to the "home" screen and clear the back stack if needed
                navController.navigate("home")
            }
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        // Title
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "Dr.Assist",
            color = Color.Black,
            fontSize = 32.sp,
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInput(onMessageSend: (String) -> Unit) {
    var message by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(50)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 12.dp),
            value = message,
            onValueChange = { message = it },
            label = { Text("Type your message...") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFFB3E5FC),
                unfocusedBorderColor = Color(0xFFE0F7FA)
            )
        )
        IconButton(
            onClick = {
                if (message.isNotBlank()) {
                    onMessageSend(message)
                    message = ""
                }
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "Send",
                modifier = Modifier.size(32.dp),
                tint = Color(0xFFBBDEFB)
            )
        }
    }
}
