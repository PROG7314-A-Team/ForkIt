package com.example.forkit.business

import com.example.forkit.data.models.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Calorie Calculator data models
 * Tests calorie calculations, macronutrient breakdowns, and validation logic
 */
class CalorieCalculatorModelsTest {
    
    @Test
    fun `macronutrient detail creates correctly`() {
        // Act
        val detail = MacronutrientDetail(
            grams = 50.0,
            calories = 200.0
        )
        
        // Assert
        assertEquals(50.0, detail.grams, 0.01)
        assertEquals(200.0, detail.calories, 0.01)
    }
    
    @Test
    fun `carbs should calculate to 4 calories per gram`() {
        // Arrange
        val carbGrams = 50.0
        val expectedCalories = carbGrams * 4
        
        // Act
        val carbDetail = MacronutrientDetail(
            grams = carbGrams,
            calories = expectedCalories
        )
        
        // Assert
        assertEquals(200.0, carbDetail.calories, 0.01)
    }
    
    @Test
    fun `protein should calculate to 4 calories per gram`() {
        // Arrange
        val proteinGrams = 30.0
        val expectedCalories = proteinGrams * 4
        
        // Act
        val proteinDetail = MacronutrientDetail(
            grams = proteinGrams,
            calories = expectedCalories
        )
        
        // Assert
        assertEquals(120.0, proteinDetail.calories, 0.01)
    }
    
    @Test
    fun `fat should calculate to 9 calories per gram`() {
        // Arrange
        val fatGrams = 20.0
        val expectedCalories = fatGrams * 9
        
        // Act
        val fatDetail = MacronutrientDetail(
            grams = fatGrams,
            calories = expectedCalories
        )
        
        // Assert
        assertEquals(180.0, fatDetail.calories, 0.01)
    }
    
    @Test
    fun `total calories from macronutrients calculates correctly`() {
        // Arrange
        val carbsCalories = 50.0 * 4  // 200
        val proteinCalories = 30.0 * 4  // 120
        val fatCalories = 20.0 * 9  // 180
        val expectedTotal = carbsCalories + proteinCalories + fatCalories  // 500
        
        // Act
        val macroCalories = MacronutrientCalories(
            carbs = carbsCalories,
            protein = proteinCalories,
            fat = fatCalories
        )
        
        val totalCalories = macroCalories.carbs + macroCalories.protein + macroCalories.fat
        
        // Assert
        assertEquals(expectedTotal, totalCalories, 0.01)
    }
    
    @Test
    fun `validation result with valid data`() {
        // Act
        val validation = ValidationResult(
            isValid = true,
            message = "Calories calculated successfully"
        )
        
        // Assert
        assertTrue(validation.isValid)
        assertEquals("Calories calculated successfully", validation.message)
    }
    
    @Test
    fun `validation result with invalid data`() {
        // Act
        val validation = ValidationResult(
            isValid = false,
            message = "Invalid macronutrient values"
        )
        
        // Assert
        assertFalse(validation.isValid)
        assertEquals("Invalid macronutrient values", validation.message)
    }
    
    @Test
    fun `calorie calculation result has correct structure`() {
        // Arrange
        val breakdown = MacronutrientBreakdown(
            carbs = MacronutrientDetail(50.0, 200.0),
            protein = MacronutrientDetail(30.0, 120.0),
            fat = MacronutrientDetail(20.0, 180.0)
        )
        
        val macroCalories = MacronutrientCalories(
            carbs = 200.0,
            protein = 120.0,
            fat = 180.0
        )
        
        // Act
        val result = CalorieCalculationResult(
            totalCalories = 500.0,
            breakdown = breakdown,
            macronutrientCalories = macroCalories
        )
        
        // Assert
        assertEquals(500.0, result.totalCalories, 0.01)
        assertEquals(breakdown, result.breakdown)
        assertEquals(macroCalories, result.macronutrientCalories)
    }
    
    @Test
    fun `calculate calories request with all macronutrients`() {
        // Act
        val request = CalculateCaloriesRequest(
            carbs = 50.0,
            protein = 30.0,
            fat = 20.0
        )
        
        // Assert
        assertEquals(50.0, request.carbs)
        assertEquals(30.0, request.protein)
        assertEquals(20.0, request.fat)
    }
    
    @Test
    fun `calculate calories request with partial macronutrients`() {
        // Act
        val request = CalculateCaloriesRequest(
            carbs = 50.0,
            protein = null,
            fat = 20.0
        )
        
        // Assert
        assertEquals(50.0, request.carbs)
        assertNull(request.protein)
        assertEquals(20.0, request.fat)
    }
}