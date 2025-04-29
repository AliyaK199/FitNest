package np.com.bimalkafle.firebaseauthdemoapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun signup(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        dob: String,
        gender: String,
        bloodType: String,
        id: String,
        password: String,
        securityQuestion: String,
        securityAnswer: String
    ) {
        if (id.length < 8 || !id.matches(Regex("^[a-zA-Z0-9]+$"))) {
            _authState.value = AuthState.Error("ID must be at least 8 characters long and alphanumeric.")
            return
        }

        if (password.length < 6 || !password.matches(Regex(".*[A-Z].*")) || !password.matches(Regex(".*[0-9].*"))) {
            _authState.value = AuthState.Error("Password must be at least 6 characters long, contain at least one uppercase letter and one number.")
            return
        }

        if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            _authState.value = AuthState.Error("All fields are required.")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userData = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email,
                        "phone" to phone,
                        "dob" to dob,
                        "gender" to gender,
                        "bloodType" to bloodType,
                        "id" to id,
                        "securityQuestion" to securityQuestion,
                        "securityAnswer" to securityAnswer
                    )

                    user?.uid?.let {
                        db.collection("users").document(it)
                            .set(userData, SetOptions.merge())
                            .addOnSuccessListener {
                                _authState.value = AuthState.Authenticated
                            }
                            .addOnFailureListener { exception ->
                                _authState.value = AuthState.Error(exception.message ?: "Failed to save user data")
                            }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }
    fun resetPassword(email: String, onResult: (Boolean) -> Unit) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true)
                } else {
                    onResult(false)
                }
            }
    }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
