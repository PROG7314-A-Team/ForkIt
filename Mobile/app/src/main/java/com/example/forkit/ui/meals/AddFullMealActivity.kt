package com.example.forkit.ui.meals

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.forkit.ui.theme.ForkItTheme
import android.util.Log

private const val DEBUG_TAG = "MealsDebug"

class AddFullMealActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(DEBUG_TAG, "AddFullMealActivity: ✅ onCreate called — AddFullMealActivity launched.")
        setContent {
            ForkItTheme {
                AddFullMealScreen(onBackPressed = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFullMealScreen(onBackPressed: () -> Unit) {
    val TAG = "AddFullMealScreen"
    val context = LocalContext.current

    // 🔹 Local states for editable sections
    var mealName by remember { mutableStateOf("") }
    var isEditingName by remember { mutableStateOf(false) }

    var mealDescription by remember { mutableStateOf("") }
    var isEditingDescription by remember { mutableStateOf(false) }

    // 🔹 Ingredient list (temporary mock list)
    var ingredients = remember { mutableStateListOf<String>() }

    // 🔹 Footer checkbox toggle
    var logToToday by remember { mutableStateOf(false) }

    Log.d(DEBUG_TAG, "$TAG: Initialized composable with empty meal data")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "New Meal",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF22B27D)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d(DEBUG_TAG, "$TAG: Back button clicked.")
                        onBackPressed()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            // 🔹 Main Content Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Helper text
                Text(
                    text = "Create a custom meal by giving it a name, short description, and adding ingredients below.",
                    fontSize = 15.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Meal name section
                Text(
                    text = "Meal Name",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF22B27D)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isEditingName) {
                    OutlinedTextField(
                        value = mealName,
                        onValueChange = { mealName = it },
                        placeholder = { Text("Enter meal name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                isEditingName = false
                                Log.d(DEBUG_TAG, "$TAG: Meal name confirmed = $mealName")
                                Toast.makeText(context, "Meal name set!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Confirm", tint = Color(0xFF22B27D))
                            }
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isEditingName = true
                                Log.d(DEBUG_TAG, "$TAG: Editing meal name triggered.")
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (mealName.isBlank()) "Add Meal Name" else mealName,
                            fontSize = 18.sp,
                            color = if (mealName.isBlank()) Color.Gray else Color.Black,
                            fontWeight = if (mealName.isBlank()) FontWeight.Normal else FontWeight.Medium
                        )
                        Icon(Icons.Default.Edit, contentDescription = "Edit name", tint = Color(0xFF22B27D))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description section
                Text(
                    text = "Description",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF22B27D)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isEditingDescription) {
                    OutlinedTextField(
                        value = mealDescription,
                        onValueChange = { mealDescription = it },
                        placeholder = { Text("Write a short description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        trailingIcon = {
                            IconButton(onClick = {
                                isEditingDescription = false
                                Log.d(DEBUG_TAG, "$TAG: Description confirmed = $mealDescription")
                                Toast.makeText(context, "Description set!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Confirm", tint = Color(0xFF22B27D))
                            }
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isEditingDescription = true
                                Log.d(DEBUG_TAG, "$TAG: Editing description triggered.")
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (mealDescription.isBlank()) "Add Description" else mealDescription,
                            fontSize = 16.sp,
                            color = if (mealDescription.isBlank()) Color.Gray else Color.Black,
                            fontWeight = if (mealDescription.isBlank()) FontWeight.Normal else FontWeight.Medium
                        )
                        Icon(Icons.Default.Edit, contentDescription = "Edit description", tint = Color(0xFF22B27D))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Ingredients section
                Text(
                    text = "Ingredients",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color(0xFF22B27D)
                )

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(visible = ingredients.isEmpty()) {
                    Text(
                        text = "No ingredients added yet.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ingredients) { ingredient ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ingredient,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                IconButton(onClick = {
                                    ingredients.remove(ingredient)
                                    Log.d(DEBUG_TAG, "$TAG: Ingredient removed -> $ingredient")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add Ingredient button
                // 🔹 Add Ingredient button (now with gradient background)
                Button(
                    onClick = {
                        ingredients.add("New Ingredient ${(1..99).random()}")
                        Log.d(DEBUG_TAG, "$TAG: Ingredient added — total count = ${ingredients.size}")
                        Toast.makeText(context, "Added mock ingredient", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF22B27D), Color(0xFF1E9E6D))
                            ),
                            shape = MaterialTheme.shapes.medium
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues()
                ) {
                    Text(
                        text = "Add Ingredient",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            // 🔹 Footer section (conditionally visible)
            AnimatedVisibility(
                visible = ingredients.isNotEmpty(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = logToToday,
                                onCheckedChange = {
                                    logToToday = it
                                    Log.d(DEBUG_TAG, "$TAG: Checkbox toggled = $logToToday")
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF22B27D))
                            )
                            Text(
                                text = "Log this meal to today’s calories?",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                Log.d(DEBUG_TAG, "$TAG: Add Meal button clicked | logToToday=$logToToday | totalIngredients=${ingredients.size}")
                                Toast.makeText(context, "Meal added successfully!", Toast.LENGTH_SHORT).show()
                                // Reset mock fields
                                mealName = ""
                                mealDescription = ""
                                ingredients.clear()
                                logToToday = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22B27D))
                        ) {
                            Text(
                                text = "Add Meal",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
