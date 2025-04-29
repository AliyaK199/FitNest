package np.com.bimalkafle.firebaseauthdemoapp.pages

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DietViewModel : ViewModel() {

    companion object {
        private const val TAG        = "DietViewModel"
        private const val COLLECTION = "diet_plans"
    }

    private val db = FirebaseFirestore.getInstance().apply {
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
    }

    // —— StateFlows for UI observation
    private val _totalCalories = MutableStateFlow(0)
    val totalCalories: StateFlow<Int> = _totalCalories

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes

    // —— Form fields
    var recipeName   = mutableStateOf("")
    var mealType     = mutableStateOf("")
    var ingredients  = mutableStateOf("")
    var instructions = mutableStateOf("")
    var servingSize  = mutableStateOf(0)
    var weight       = mutableStateOf(0)
    var calories     = mutableStateOf(0)

    // —— Field‑updater methods, re‑added so DietPage.kt compiles
    fun onRecipeNameChange(new: String)     { recipeName.value   = new }
    fun onMealTypeChange(new: String)       { mealType.value     = new }
    fun onIngredientsChange(new: String)    { ingredients.value  = new }
    fun onInstructionsChange(new: String)   { instructions.value = new }
    fun onServingSizeChange(new: Int)       { servingSize.value  = new }
    fun onWeightChange(new: Int)            { weight.value       = new }
    fun onCaloriesChange(new: Int)          { calories.value     = new }

    init {
        fetchRecipes()
        fetchTotalCalories()
    }

    /** Create or overwrite a recipe document in “Diet plan” */
    fun saveRecipe() {
        val newRecipe = Recipe(
            id           = generateId(),
            name         = recipeName.value,
            mealType     = mealType.value,
            ingredients  = ingredients.value,
            instructions = instructions.value,
            servingSize  = servingSize.value,
            weight       = weight.value,
            calories     = calories.value
        )

        // Update local list
        _recipes.value = _recipes.value + newRecipe

        // Write to Firestore
        db.collection(COLLECTION)
            .document(newRecipe.id)
            .set(newRecipe)
            .addOnSuccessListener { fetchRecipes() }
            .addOnFailureListener  { Log.e(TAG, "Error saving recipe", it) }

        clearRecipeFields()
    }

    /** Increment totalCalories and persist under “Diet plan/totalCalories” */
    fun addCalories(amount: Int) {
        val newTotal = _totalCalories.value + amount
        _totalCalories.value = newTotal

        db.collection(COLLECTION)
            .document("totalCalories")
            .set(mapOf("value" to newTotal))
            .addOnFailureListener { Log.e(TAG, "Error updating totalCalories", it) }
    }

    /** Update an existing recipe in the same collection */
    fun updateRecipe(updated: Recipe) {
        _recipes.value = _recipes.value.map {
            if (it.id == updated.id) updated else it
        }
        db.collection(COLLECTION)
            .document(updated.id)
            .set(updated)
            .addOnFailureListener { Log.e(TAG, "Error updating recipe", it) }
    }

    /** Delete from Firestore and local list */
    fun deleteRecipe(recipe: Recipe) {
        _recipes.value = _recipes.value.filter { it.id != recipe.id }
        db.collection(COLLECTION)
            .document(recipe.id)
            .delete()
            .addOnFailureListener { Log.e(TAG, "Error deleting recipe", it) }
    }

    /** Listen for real‑time recipe updates */
    private fun fetchRecipes() {
        db.collection(COLLECTION)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed", e)
                    return@addSnapshotListener
                }
                _recipes.value = snap
                    ?.documents
                    ?.mapNotNull { it.toObject(Recipe::class.java) }
                    ?: emptyList()
            }
    }

    /** Load the last‑saved totalCalories on startup */
    private fun fetchTotalCalories() {
        db.collection(COLLECTION)
            .document("totalCalories")
            .get()
            .addOnSuccessListener { doc ->
                _totalCalories.value = (doc.getLong("value") ?: 0L).toInt()
            }
            .addOnFailureListener { Log.e(TAG, "Error fetching totalCalories", it) }
    }

    private fun clearRecipeFields() {
        recipeName.value   = ""
        mealType.value     = ""
        ingredients.value  = ""
        instructions.value = ""
        servingSize.value  = 0
        weight.value       = 0
        calories.value     = 0
    }

    private fun generateId(): String = System.currentTimeMillis().toString()
}

data class Recipe(
    val id: String = "",
    val name: String = "",
    val mealType: String = "",
    val ingredients: String = "",
    val instructions: String = "",
    val servingSize: Int = 0,
    val weight: Int = 0,
    val calories: Int = 0
)
