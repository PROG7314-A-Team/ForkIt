package com.example.forkit.business

import com.example.forkit.data.models.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Meal Log data models
 * Tests meal creation, ingredients, instructions, and nutritional data
 */
class MealLogsModelsTest {
    
    @Test
    fun `ingredient creates with all required fields`() {
        // Arrange & Act
        val ingredient = Ingredient(
            name = "Chicken Breast",
            amount = 250.0,
            unit = "grams"
        )
        
        // Assert
        assertEquals("Chicken Breast", ingredient.name)
        assertEquals(250.0, ingredient.amount, 0.01)
        assertEquals("grams", ingredient.unit)
    }
    
    @Test
    fun `meal log creates with all required fields`() {
        // Arrange
        val ingredients = listOf(
            Ingredient("Chicken", 250.0, "grams"),
            Ingredient("Rice", 100.0, "grams")
        )
        val instructions = listOf(
            "Cook chicken for 20 minutes",
            "Boil rice for 15 minutes"
        )
        
        // Act
        val mealLog = MealLog(
            id = "meal123",
            userId = "user123",
            name = "Chicken and Rice",
            description = "Healthy protein and carb meal",
            ingredients = ingredients,
            instructions = instructions,
            totalCalories = 500.0,
            totalCarbs = 50.0,
            totalFat = 10.0,
            totalProtein = 40.0,
            servings = 2.0,
            date = "2025-10-05",
            mealType = "Lunch",
            createdAt = "2025-10-05T12:00:00Z",
            updatedAt = "2025-10-05T12:00:00Z"
        )
        
        // Assert
        assertEquals("meal123", mealLog.id)
        assertEquals("user123", mealLog.userId)
        assertEquals("Chicken and Rice", mealLog.name)
        assertEquals(2, mealLog.ingredients.size)
        assertEquals(2, mealLog.instructions.size)
        assertEquals(500.0, mealLog.totalCalories, 0.01)
        assertEquals(2.0, mealLog.servings, 0.01)
        assertEquals("Lunch", mealLog.mealType)
    }
    
    @Test
    fun `create meal log request validates required fields`() {
        // Arrange
        val ingredients = listOf(Ingredient("Test", 100.0, "grams"))
        val instructions = listOf("Test instruction")
        
        // Act
        val request = CreateMealLogRequest(
            userId = "user123",
            name = "Test Meal",
            description = "Test description",
            ingredients = ingredients,
            instructions = instructions,
            totalCalories = 300.0,
            totalCarbs = 30.0,
            totalFat = 15.0,
            totalProtein = 25.0,
            servings = 1.0,
            date = "2025-10-05",
            mealType = "Dinner"
        )
        
        // Assert
        assertEquals("user123", request.userId)
        assertEquals("Test Meal", request.name)
        assertEquals(1, request.ingredients.size)
        assertEquals(1, request.instructions.size)
        assertEquals(300.0, request.totalCalories)
        assertEquals("Dinner", request.mealType)
    }
    
    @Test
    fun `update meal log request allows partial updates`() {
        // Act
        val request = UpdateMealLogRequest(
            name = "Updated Meal Name",
            totalCalories = 400.0,
            servings = 2.0
        )
        
        // Assert
        assertEquals("Updated Meal Name", request.name)
        assertEquals(400.0, request.totalCalories)
        assertEquals(2.0, request.servings)
        assertNull(request.description)
        assertNull(request.ingredients)
    }
    
    @Test
    fun `meal log response creates with success status`() {
        // Arrange
        val mealLog = MealLog(
            id = "meal123",
            userId = "user123",
            name = "Test Meal",
            description = "",
            ingredients = emptyList(),
            instructions = emptyList(),
            totalCalories = 0.0,
            totalCarbs = 0.0,
            totalFat = 0.0,
            totalProtein = 0.0,
            servings = 1.0,
            date = "2025-10-05",
            mealType = null,
            createdAt = "",
            updatedAt = ""
        )
        
        // Act
        val response = MealLogResponse(
            success = true,
            message = "Meal log created successfully",
            data = mealLog
        )
        
        // Assert
        assertTrue(response.success)
        assertEquals("Meal log created successfully", response.message)
        assertEquals(mealLog, response.data)
    }
    
    @Test
    fun `meal logs response creates with list of meals`() {
        // Arrange
        val mealLogs = listOf(
            MealLog("1", "user123", "Meal 1", "", emptyList(), emptyList(), 0.0, 0.0, 0.0, 0.0, 1.0, "2025-10-05", null, "", ""),
            MealLog("2", "user123", "Meal 2", "", emptyList(), emptyList(), 0.0, 0.0, 0.0, 0.0, 1.0, "2025-10-05", null, "", "")
        )
        
        // Act
        val response = MealLogsResponse(
            success = true,
            message = "Meal logs retrieved successfully",
            data = mealLogs
        )
        
        // Assert
        assertTrue(response.success)
        assertEquals(2, response.data.size)
        assertEquals("Meal 1", response.data[0].name)
        assertEquals("Meal 2", response.data[1].name)
    }
    
    @Test
    fun `ingredient with different units`() {
        // Act
        val liquidIngredient = Ingredient("Water", 500.0, "ml")
        val solidIngredient = Ingredient("Flour", 200.0, "grams")
        val pieceIngredient = Ingredient("Eggs", 2.0, "pieces")
        
        // Assert
        assertEquals("ml", liquidIngredient.unit)
        assertEquals("grams", solidIngredient.unit)
        assertEquals("pieces", pieceIngredient.unit)
    }
    
    @Test
    fun `meal log with multiple ingredients and instructions`() {
        // Arrange
        val ingredients = listOf(
            Ingredient("Chicken", 300.0, "grams"),
            Ingredient("Onion", 50.0, "grams"),
            Ingredient("Garlic", 10.0, "grams"),
            Ingredient("Olive Oil", 15.0, "ml")
        )
        val instructions = listOf(
            "Heat oil in pan",
            "Add chopped onion and garlic",
            "Add chicken and cook for 15 minutes",
            "Season with salt and pepper"
        )
        
        // Act
        val mealLog = MealLog(
            id = "complex123",
            userId = "user123",
            name = "Chicken Stir Fry",
            description = "Quick and healthy stir fry",
            ingredients = ingredients,
            instructions = instructions,
            totalCalories = 450.0,
            totalCarbs = 20.0,
            totalFat = 25.0,
            totalProtein = 35.0,
            servings = 1.0,
            date = "2025-10-05",
            mealType = "Dinner",
            createdAt = "2025-10-05T18:00:00Z",
            updatedAt = "2025-10-05T18:00:00Z"
        )
        
        // Assert
        assertEquals(4, mealLog.ingredients.size)
        assertEquals(4, mealLog.instructions.size)
        assertEquals("Chicken Stir Fry", mealLog.name)
        assertEquals(450.0, mealLog.totalCalories, 0.01)
    }
}