//package np.com.bimalkafle.firebaseauthdemoapp.pages
//
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.Surface
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.example.notes.ui.theme.HomeScreen
//import com.example.notes.ui.theme.NotesUpdate
//
//@Composable
//fun NotesApp() {
//    // Create a NavController
//    val navController = rememberNavController()
//
//    // A Surface to apply our Material theme background
//    Surface(modifier = Modifier.fillMaxSize()) {
//        NavHost(
//            navController = navController,
//            startDestination = "home"        // <-- MUST match one of the composable() routes below
//        ) {
//            composable("home") {
//                HomeScreen { navController.navigate("notes") }
//            }
//            composable("notes") {
//                NotesUpdate { navController.popBackStack() }
//            }
//        }
//    }
//}
//






