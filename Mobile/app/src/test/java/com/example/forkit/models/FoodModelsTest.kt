package com.example.forkit.models

import com.example.forkit.data.models.Food
import com.example.forkit.data.models.Nutrients
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Food data models
 * Tests creation, validation, and properties of Food and Nutrients models
 */
class FoodModelsTest {
    
    @Test
    fun `nutrients model creates correctly with all values`() {
        // Arrange
        val carbs = 50.0
        val protein = 20.0
        val fat = 10.0
        val fiber = 5.0
        val sugar = 15.0
        
        // Act
        val nutrients = Nutrients(
            carbs = carbs,
            protein = protein,
            fat = fat,
            fiber = fiber,
            sugar = sugar
        )
        
        // Assert
        assertEquals(carbs, nutrients.carbs, 0.01)
        assertEquals(protein, nutrients.protein, 0.01)
        assertEquals(fat, nutrients.fat, 0.01)
        assertEquals(fiber, nutrients.fiber, 0.01)
        assertEquals(sugar, nutrients.sugar, 0.01)
    }
    
    @Test
    fun `food model creates correctly with all required fields`() {
        // Arrange
        val nutrients = Nutrients(
            carbs = 50.0,
            protein = 20.0,
            fat = 10.0,
            fiber = 5.0,
            sugar = 15.0
        )
        
        // Act
        val food = Food(
            id = "food123",
            name = "Chicken Breast",
            brand = "Generic",
            barcode = "1234567890123",
            calories = 165.0,
            nutrients = nutrients,
            image = "https://example.com/chicken.jpg",
            ingredients = "Chicken"
        )
        
        // Assert
        assertEquals("food123", food.id)
        assertEquals("Chicken Breast", food.name)
        assertEquals("Generic", food.brand)
        assertEquals("1234567890123", food.barcode)
        assertEquals(165.0, food.calories, 0.01)
        assertEquals(nutrients, food.nutrients)
        assertEquals("https://example.com/chicken.jpg", food.image)
        assertEquals("Chicken", food.ingredients)
    }
    
    @Test
    fun `food calories match expected macronutrient calculation`() {
        // Arrange: Carbs = 50g (200 cal), Protein = 20g (80 cal), Fat = 10g (90 cal) = 370 total
        val nutrients = Nutrients(
            carbs = 50.0,
            protein = 20.0,
            fat = 10.0,
            fiber = 5.0,
            sugar = 15.0
        )
        
        val expectedCalories = (50.0 * 4) + (20.0 * 4) + (10.0 * 9) // carbs*4 + protein*4 + fat*9
        
        // Act
        val food = Food(
            id = "test1",
            name = "Test Food",
            brand = "Test Brand",
            barcode = "123456",
            calories = expectedCalories,
            nutrients = nutrients,
            image = "",
            ingredients = "Test"
        )
        
        // Assert
        assertEquals(expectedCalories, food.calories, 0.01)
    }
    
    @Test
    fun `two food items with same properties are equal`() {
        // Arrange
        val nutrients1 = Nutrients(50.0, 20.0, 10.0, 5.0, 15.0)
        val nutrients2 = Nutrients(50.0, 20.0, 10.0, 5.0, 15.0)
        
        val food1 = Food("1", "Apple", "Fresh", "123", 95.0, nutrients1, "img", "apple")
        val food2 = Food("1", "Apple", "Fresh", "123", 95.0, nutrients2, "img", "apple")
        
        // Assert
        assertEquals(food1, food2)
    }
}
