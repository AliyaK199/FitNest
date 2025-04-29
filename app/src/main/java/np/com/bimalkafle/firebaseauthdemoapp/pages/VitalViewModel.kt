package np.com.bimalkafle.firebaseauthdemoapp.pages


import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class VitalViewModel : ViewModel() {
    var latestVitals by mutableStateOf<VitalSignEntry?>(null)
        private set

    fun fetchLatestVitals(userId: Int) {

        FirebaseFirestore.getInstance()
            .collection("vital_signs")
            .whereEqualTo("userId", userId.toString())
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d("VitalPage", "Documents found: ${snapshot.size()}")
                val document = snapshot.documents.firstOrNull()
                if (document != null) {
                    Log.d("VitalPage", "Document Data: ${document.data}")
                    val vitals = document.toObject(VitalSignEntry::class.java)
                    Log.d("VitalPage", "Parsed Vitals: $vitals")
                    latestVitals = vitals
                } else {
                    Log.w("VitalPage", "No vitals found for user.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("VitalPage", "Error fetching vitals", e)
            }
    }
}