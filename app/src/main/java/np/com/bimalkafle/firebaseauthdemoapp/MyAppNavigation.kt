package np.com.bimalkafle.firebaseauthdemoapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.diet.pages.DietPage
import np.com.bimalkafle.firebaseauthdemoapp.pages.ChatViewModel
import np.com.bimalkafle.firebaseauthdemoapp.pages.Chatbot
import np.com.bimalkafle.firebaseauthdemoapp.pages.HomePage
import np.com.bimalkafle.firebaseauthdemoapp.pages.LoginPage
import np.com.bimalkafle.firebaseauthdemoapp.pages.SignupPage
import np.com.bimalkafle.firebaseauthdemoapp.pages.CommunicationPage // 👈 make sure to import it
import np.com.bimalkafle.firebaseauthdemoapp.pages.EventCalendar
import np.com.bimalkafle.firebaseauthdemoapp.pages.Medicine
import np.com.bimalkafle.firebaseauthdemoapp.pages.VitalSignInputScreen
import np.com.bimalkafle.firebaseauthdemoapp.pages.VitalsPage
import np.com.bimalkafle.firebaseauthdemoapp.pages.NotesUpdate

import np.com.bimalkafle.firebaseauthdemoapp.pages.VitalSignInputScreen


@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup") {
            SignupPage(modifier, navController, authViewModel)
        }
        composable("home") {
            HomePage(modifier, navController, authViewModel)
        }
        composable("communication") {
            CommunicationPage(navController)
        }
        composable("vital_sign_input") {
            VitalSignInputScreen(userId = 1, navController = navController)
        }
        composable("vitals_page") {
            VitalsPage(navController = navController)
        }
        composable("chatbot") { backStackEntry ->
            val viewModel: ChatViewModel = viewModel() // Get the viewModel
            Chatbot(modifier, viewModel, navController)  // Pass navController directly to Chatbot
        }
        composable("calendar") {
            EventCalendar(navController)
        }

        composable("medicine") {
            Medicine(navController) // This is your existing NotesUpdate composable
        }
        composable("diet_page") {
            DietPage(navController) // or DietPage(navController) if needed
        }
        composable("notes_update") {
            NotesUpdate(navController, onBack = { navController.navigate("home") })
        }








    }

}