// app/src/main/java/com/example/diet/pages/DietPage.kt
package com.example.diet.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import np.com.bimalkafle.firebaseauthdemoapp.R
import np.com.bimalkafle.firebaseauthdemoapp.pages.AddMealCard
import np.com.bimalkafle.firebaseauthdemoapp.pages.DietViewModel
import np.com.bimalkafle.firebaseauthdemoapp.pages.LabeledText
import np.com.bimalkafle.firebaseauthdemoapp.pages.Recipe
import np.com.bimalkafle.firebaseauthdemoapp.pages.SummaryCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDialog(vm: DietViewModel, onDismiss: () -> Unit) {
    var mealExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Recipe") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Recipe Name
                OutlinedTextField(
                    value = vm.recipeName.value,
                    onValueChange = vm::onRecipeNameChange,
                    label = { Text("Recipe Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Meal Type dropdown
                ExposedDropdownMenuBox(
                    expanded = mealExpanded,
                    onExpandedChange = { mealExpanded = !mealExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = vm.mealType.value,
                        onValueChange = { /* no-op */ },
                        label = { Text("Meal Type") },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = mealExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = mealExpanded,
                        onDismissRequest = { mealExpanded = false }
                    ) {
                        listOf("Breakfast", "Lunch", "Dinner").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    vm.onMealTypeChange(option)
                                    mealExpanded = false
                                }
                            )
                        }
                    }
                }

                // Ingredients
                OutlinedTextField(
                    value = vm.ingredients.value,
                    onValueChange = vm::onIngredientsChange,
                    label = { Text("Ingredients") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Instructions
                OutlinedTextField(
                    value = vm.instructions.value,
                    onValueChange = vm::onInstructionsChange,
                    label = { Text("Instructions") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Serving Size
                OutlinedTextField(
                    value = vm.servingSize.value.toString(),
                    onValueChange = {
                        vm.onServingSizeChange(it.toIntOrNull() ?: 0)
                    },
                    label = { Text("Serving Size") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Weight
                OutlinedTextField(
                    value = vm.weight.value.toString(),
                    onValueChange = {
                        vm.onWeightChange(it.toIntOrNull() ?: 0)
                    },
                    label = { Text("Weight (g)") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Calories
                OutlinedTextField(
                    value = vm.calories.value.toString(),
                    onValueChange = {
                        vm.onCaloriesChange(it.toIntOrNull() ?: 0)
                    },
                    label = { Text("Calories") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                vm.saveRecipe()
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietPage(navController: NavController, vm: DietViewModel = viewModel()) {
    val recipes by vm.recipes.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var viewingRecipe by remember { mutableStateOf<Recipe?>(null) }
    var editingRecipe by remember { mutableStateOf<Recipe?>(null) }
    val totalCalories by vm.totalCalories.collectAsState()

    Scaffold(
        topBar = {
            Box(modifier = Modifier.height(200.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.diettwo), // Replace with your image resource
                    contentDescription = "Background Banner",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RectangleShape) // Optional: clips the image to a specific shape
                        .align(Alignment.TopCenter),
                    contentScale = ContentScale.Crop // Adjust this based on how you want the image to scale
                )

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center

                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically

                    ) {
                        IconButton(onClick = { navController.navigate("home") }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }

                        Text(
                            text = "            My Diet Plan            ",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 24.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .background(Color(0x88000000))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SummaryCard(totalCalories = totalCalories) { caloriesToAdd ->
                vm.addCalories(caloriesToAdd)
            }

            if (showDialog) {
                RecipeDialog(vm = vm, onDismiss = { showDialog = false })
            }

            if (viewingRecipe != null) {
                AlertDialog(
                    onDismissRequest = { viewingRecipe = null },
                    title = { Text(viewingRecipe!!.name) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LabeledText("Type", viewingRecipe!!.mealType)
                            LabeledText("Calories", "${viewingRecipe!!.calories} kcal")
                            LabeledText("Serving Size", viewingRecipe!!.servingSize.toString())
                            LabeledText("Weight", "${viewingRecipe!!.weight}g")
                            LabeledText("Ingredients", viewingRecipe!!.ingredients)
                            LabeledText("Instructions", viewingRecipe!!.instructions)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { viewingRecipe = null }) {
                            Text("Close")
                        }
                    }
                )
            }

            if (editingRecipe != null) {
                var name by remember { mutableStateOf(editingRecipe!!.name) }
                var type by remember { mutableStateOf(editingRecipe!!.mealType) }
                var ing by remember { mutableStateOf(editingRecipe!!.ingredients) }
                var instr by remember { mutableStateOf(editingRecipe!!.instructions) }
                var serve by remember { mutableStateOf(editingRecipe!!.servingSize) }
                var wt by remember { mutableStateOf(editingRecipe!!.weight) }
                var cal by remember { mutableStateOf(editingRecipe!!.calories) }

                AlertDialog(
                    onDismissRequest = { editingRecipe = null },
                    title = { Text("Edit ${editingRecipe!!.name}") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(name, { name = it }, label = { Text("Recipe Name") })

                            var editExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = editExpanded,
                                onExpandedChange = { editExpanded = !editExpanded },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentSize(Alignment.TopStart)
                            ) {
                                OutlinedTextField(
                                    value = type,
                                    onValueChange = { },
                                    label = { Text("Meal Type") },
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editExpanded) },
                                    modifier = Modifier
                                        .wrapContentSize(Alignment.TopStart)
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = editExpanded,
                                    onDismissRequest = { editExpanded = false }
                                ) {
                                    listOf("Breakfast", "Lunch", "Dinner").forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                type = option
                                                editExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(ing, { ing = it }, label = { Text("Ingredients") })
                            OutlinedTextField(instr, { instr = it }, label = { Text("Instructions") })
                            OutlinedTextField(
                                serve.toString(),
                                { serve = it.toIntOrNull() ?: serve },
                                label = { Text("Serving Size") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                wt.toString(),
                                { wt = it.toIntOrNull() ?: wt },
                                label = { Text("Weight (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                cal.toString(),
                                { cal = it.toIntOrNull() ?: cal },
                                label = { Text("Calories") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val updated = editingRecipe!!.copy(
                                name = name,
                                mealType = type,
                                ingredients = ing,
                                instructions = instr,
                                servingSize = serve,
                                weight = wt,
                                calories = cal
                            )
                            vm.updateRecipe(updated)
                            editingRecipe = null
                        }) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingRecipe = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Text("My Meals Today", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, start = 8.dp))

            if (recipes.isEmpty()) {
                Text(
                    "No meals yet — start by adding one!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(recipes) { recipe ->
                        MealCard(
                            recipe = recipe,
                            onView = { viewingRecipe = recipe },
                            onEdit = { editingRecipe = recipe },
                            onDelete = {
                                if (viewingRecipe == recipe) viewingRecipe = null
                                if (editingRecipe == recipe) editingRecipe = null
                                vm.deleteRecipe(recipe)
                            }
                        )
                    }

                    item {
                        AddMealCard { showDialog = true }
                    }
                }
            }

        }
    }
}


@Composable
fun MealCard(recipe: Recipe, onView: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7E3)),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .size(width = 280.dp, height = 430.dp)
            .padding(6.dp)
            .clickable { onView() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF6B705C), modifier = Modifier.size(40.dp).align(Alignment.End))
            Text(text = recipe.name, style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp), modifier = Modifier.padding(bottom = 4.dp))
            LabeledText("Type", recipe.mealType)
            LabeledText("Calories", "${recipe.calories} kcal")
            LabeledText("Serving Size", recipe.servingSize.toString())
            LabeledText("Weight", "${recipe.weight}g")
            LabeledText("Ingredients", recipe.ingredients)
            LabeledText("Instructions", recipe.instructions)
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = {
                    onDelete()
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
