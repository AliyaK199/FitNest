package np.com.bimalkafle.firebaseauthdemoapp.pages



import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class VitalSignEntry(
    val pulse: Int = 0,
    val temperature: Int = 0,
    val glucose: Int = 0,
    val spo2: Int = 0,
    val respiratoryRate: Int = 0,
    val bloodPressure: String = "",
    val timestamp: Long = 0,
    val userId: String = ""
)
