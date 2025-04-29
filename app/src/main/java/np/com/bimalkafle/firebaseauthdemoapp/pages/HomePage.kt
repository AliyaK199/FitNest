package np.com.bimalkafle.firebaseauthdemoapp.pages

import android.app.AlertDialog
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import np.com.bimalkafle.firebaseauthdemoapp.AuthState
import np.com.bimalkafle.firebaseauthdemoapp.AuthViewModel
import np.com.bimalkafle.firebaseauthdemoapp.R
import kotlin.math.min

@Composable
fun WidgetButton(label: String, color: Color, imageId: Int, navController: NavController) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .clickable {
                when (label) {
                    "Communication" -> navController.navigate("communication")
                    "Vital Signs" -> navController.navigate("vital_sign_input")
                    "Vitals Page" -> navController.navigate("vitals_page")
                    "Notes" -> navController.navigate("notes_update")
                    "Medications" -> navController.navigate("medicine")
                    "Diet" -> navController.navigate("diet_page")

                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageId),
                contentDescription = label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.6f))
                    .padding(horizontal = 4.dp)
            )
        }
    }
}
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    var searchText by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val allWidgets = listOf(
        Triple("Vital Signs", Color(0xFFE0F7FA), R.drawable.vitalsign),
        Triple("Vitals Page", Color(0xFFE0F7FA), R.drawable.vitalone),
        Triple("Medications", Color(0xFFFFF1E6), R.drawable.med),
        Triple("Diet", Color(0xFFE3FCEC), R.drawable.diet),
        Triple("Notes", Color(0xFFFFFDE7), R.drawable.notes),
        Triple("Communication", Color(0xFFEDE7F6), R.drawable.communication)
    )

    val filteredWidgets = allWidgets.filter {
        it.first.contains(searchText, ignoreCase = true)
    }

    val animatedWidth by animateDpAsState(targetValue = if (isFocused) 360.dp else 200.dp)
    val animatedHeight by animateDpAsState(targetValue = if (isFocused) 60.dp else 40.dp)

    val listState = rememberLazyListState()
    val scrollOffset = min(1f, listState.firstVisibleItemScrollOffset / 200f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .offset(y = (-100 * scrollOffset).dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_12),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88DCCFF6))
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 42.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.homenew),
                        contentDescription = "home",
                        modifier = Modifier
                            .height(80.dp)
                            .width(70.dp)
                            .background(Color.Transparent),
                        contentScale = ContentScale.FillWidth
                    )

                    Text(
                        text = "Home",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Left,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { navController.navigate("chatbot") },
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ai),
                            contentDescription = "Logo",
                            modifier = Modifier.size(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Welcome Back!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5E548E),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Explore your health journey",
                            fontSize = 14.sp,
                            color = Color(0xFF7E6B8F),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = {
                            Text(
                                text = "Search...",
                                fontSize = if (isFocused) 14.sp else 12.sp
                            )
                        },
                        modifier = Modifier
                            .width(animatedWidth)
                            .height(animatedHeight)
                            .onFocusChanged { focusState -> isFocused = focusState.isFocused },
                        singleLine = true,
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        colors = TextFieldDefaults.colors(
                            cursorColor = Color(0xFFFF69B4),
                            unfocusedContainerColor = Color(0xFFC2D0D1),
                            focusedLabelColor = Color(0xFFFF69B4),
                            focusedContainerColor = Color(0xFFC2D0D1)
                        )
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            navController.navigate("calendar")
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.calendertwo),
                        contentDescription = "Open Calendar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 1.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    filteredWidgets.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowItems.forEach { (label, color, imageId) ->
                                Box(modifier = Modifier.weight(1f)) {
                                    WidgetButton(label, color, imageId, navController)
                                }
                                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = { authViewModel.signout() },
                    modifier = Modifier
                        .width(200.dp)
                        .height(80.dp)
                        .padding(bottom = 32.dp)
                        .background(
                            color = Color(0xFF9ABAAB),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Text(
                        text = "Sign Out",
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
