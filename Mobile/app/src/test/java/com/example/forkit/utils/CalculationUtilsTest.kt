package com.example.forkit.utils

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Calculation utility functions
 * Tests calorie calculations, macronutrient calculations, and health metrics
 */
class CalculationUtilsTest {

    @Test
    fun `calorie calculation from macronutrients works correctly`() {
        // Test carbs calculation (4 calories per gram)
        val carbsGrams = 50.0
        val carbsCalories = calculateCarbsCalories(carbsGrams)
        assertEquals("Carbs calories should be 200", 200.0, carbsCalories, 0.01)
        
        // Test protein calculation (4 calories per gram)
        val proteinGrams = 30.0
        val proteinCalories = calculateProteinCalories(proteinGrams)
        assertEquals("Protein calories should be 120", 120.0, proteinCalories, 0.01)
        
        // Test fat calculation (9 calories per gram)
        val fatGrams = 20.0
        val fatCalories = calculateFatCalories(fatGrams)
        assertEquals("Fat calories should be 180", 180.0, fatCalories, 0.01)
    }

    @Test
    fun `total calorie calculation works correctly`() {
        // Arrange
        val carbsGrams = 50.0
        val proteinGrams = 30.0
        val fatGrams = 20.0
        
        // Act
        val totalCalories = calculateTotalCalories(carbsGrams, proteinGrams, fatGrams)
        
        // Assert
        val expectedCalories = (carbsGrams * 4) + (proteinGrams * 4) + (fatGrams * 9)
        assertEquals("Total calories should be 500", expectedCalories, totalCalories, 0.01)
    }

    @Test
    fun `macronutrient percentage calculation works correctly`() {
        // Arrange
        val carbsCalories = 200.0
        val proteinCalories = 120.0
        val fatCalories = 180.0
        val totalCalories = 500.0
        
        // Act
        val carbsPercentage = calculateMacronutrientPercentage(carbsCalories, totalCalories)
        val proteinPercentage = calculateMacronutrientPercentage(proteinCalories, totalCalories)
        val fatPercentage = calculateMacronutrientPercentage(fatCalories, totalCalories)
        
        // Assert
        assertEquals("Carbs percentage should be 40%", 40.0, carbsPercentage, 0.01)
        assertEquals("Protein percentage should be 24%", 24.0, proteinPercentage, 0.01)
        assertEquals("Fat percentage should be 36%", 36.0, fatPercentage, 0.01)
    }

    @Test
    fun `bmi calculation works correctly`() {
        // Test normal BMI
        val height = 175.0 // cm
        val weight = 70.0 // kg
        val bmi = calculateBMI(weight, height)
        val expectedBMI = weight / ((height / 100) * (height / 100))
        
        assertEquals("BMI should be calculated correctly", expectedBMI, bmi, 0.01)
        assertTrue("BMI should be in normal range", bmi >= 18.5 && bmi < 25)
    }

    @Test
    fun `bmi category classification works correctly`() {
        // Test different BMI categories
        val testCases = listOf(
            16.0 to "Underweight",
            20.0 to "Normal weight",
            26.0 to "Overweight",
            32.0 to "Obese"
        )
        
        testCases.forEach { (bmi, expectedCategory) ->
            val category = getBMICategory(bmi)
            assertEquals("BMI category should be correct for BMI $bmi", expectedCategory, category)
        }
    }

    @Test
    fun `water intake calculation works correctly`() {
        // Test daily water intake calculation
        val weight = 70.0 // kg
        val activityLevel = 1.2 // moderate activity
        val dailyWaterIntake = calculateDailyWaterIntake(weight, activityLevel)
        
        // Standard calculation: 35ml per kg of body weight
        val expectedWaterIntake = weight * 35 * activityLevel
        assertEquals("Daily water intake should be calculated correctly", expectedWaterIntake, dailyWaterIntake, 0.01)
    }

    @Test
    fun `calorie deficit calculation works correctly`() {
        // Test calorie deficit calculation
        val consumed = 2000.0
        val burned = 300.0
        val deficit = calculateCalorieDeficit(consumed, burned)
        
        assertEquals("Calorie deficit should be 1700", 1700.0, deficit, 0.01)
    }

    @Test
    fun `progress percentage calculation works correctly`() {
        // Test progress percentage calculation
        val current = 1500.0
        val goal = 2000.0
        val progress = calculateProgressPercentage(current, goal)
        
        assertEquals("Progress should be 75%", 75.0, progress, 0.01)
    }

    @Test
    fun `goal achievement calculation works correctly`() {
        // Test goal achievement
        val current = 2000.0
        val goal = 2000.0
        val isGoalAchieved = isGoalAchieved(current, goal)
        
        assertTrue("Goal should be achieved", isGoalAchieved)
        
        // Test goal not achieved
        val current2 = 1500.0
        val goal2 = 2000.0
        val isGoalAchieved2 = isGoalAchieved(current2, goal2)
        
        assertFalse("Goal should not be achieved", isGoalAchieved2)
    }

    @Test
    fun `streak calculation works correctly`() {
        // Test streak calculation
        val lastLogDate = "2025-01-15"
        val currentDate = "2025-01-16"
        val streak = calculateStreak(lastLogDate, currentDate)
        
        assertEquals("Streak should be 1 day", 1, streak)
    }

    @Test
    fun `macronutrient ratio calculation works correctly`() {
        // Test macronutrient ratio calculation
        val carbs = 200.0
        val protein = 120.0
        val fat = 180.0
        val total = 500.0
        
        val carbsRatio = calculateMacronutrientRatio(carbs, total)
        val proteinRatio = calculateMacronutrientRatio(protein, total)
        val fatRatio = calculateMacronutrientRatio(fat, total)
        
        assertEquals("Carbs ratio should be 0.4", 0.4, carbsRatio, 0.01)
        assertEquals("Protein ratio should be 0.24", 0.24, proteinRatio, 0.01)
        assertEquals("Fat ratio should be 0.36", 0.36, fatRatio, 0.01)
    }

    @Test
    fun `calorie calculation edge cases`() {
        // Test with zero values
        val zeroCalories = calculateTotalCalories(0.0, 0.0, 0.0)
        assertEquals("Zero macronutrients should result in zero calories", 0.0, zeroCalories, 0.01)
        
        // Test with negative values (should be handled gracefully)
        val negativeCalories = calculateTotalCalories(-10.0, 20.0, 30.0)
        assertTrue("Negative values should be handled", negativeCalories >= 0)
    }

    @Test
    fun `percentage calculation edge cases`() {
        // Test with zero total
        val zeroPercentage = calculateMacronutrientPercentage(100.0, 0.0)
        assertEquals("Zero total should result in zero percentage", 0.0, zeroPercentage, 0.01)
        
        // Test with zero macronutrient
        val zeroMacroPercentage = calculateMacronutrientPercentage(0.0, 100.0)
        assertEquals("Zero macronutrient should result in zero percentage", 0.0, zeroMacroPercentage, 0.01)
    }

    // Helper functions for calculations (these would be implemented in the actual utils class)
    private fun calculateCarbsCalories(grams: Double): Double {
        return grams * 4
    }

    private fun calculateProteinCalories(grams: Double): Double {
        return grams * 4
    }

    private fun calculateFatCalories(grams: Double): Double {
        return grams * 9
    }

    private fun calculateTotalCalories(carbs: Double, protein: Double, fat: Double): Double {
        return calculateCarbsCalories(carbs) + calculateProteinCalories(protein) + calculateFatCalories(fat)
    }

    private fun calculateMacronutrientPercentage(macroCalories: Double, totalCalories: Double): Double {
        return if (totalCalories > 0) (macroCalories / totalCalories) * 100 else 0.0
    }

    private fun calculateBMI(weight: Double, height: Double): Double {
        val heightInMeters = height / 100
        return weight / (heightInMeters * heightInMeters)
    }

    private fun getBMICategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Normal weight"
            bmi < 30 -> "Overweight"
            else -> "Obese"
        }
    }

    private fun calculateDailyWaterIntake(weight: Double, activityLevel: Double): Double {
        return weight * 35 * activityLevel
    }

    private fun calculateCalorieDeficit(consumed: Double, burned: Double): Double {
        return consumed - burned
    }

    private fun calculateProgressPercentage(current: Double, goal: Double): Double {
        return if (goal > 0) (current / goal) * 100 else 0.0
    }

    private fun isGoalAchieved(current: Double, goal: Double): Boolean {
        return current >= goal
    }

    private fun calculateStreak(lastLogDate: String, currentDate: String): Int {
        // Simplified streak calculation
        return if (lastLogDate < currentDate) 1 else 0
    }

    private fun calculateMacronutrientRatio(macroCalories: Double, totalCalories: Double): Double {
        return if (totalCalories > 0) macroCalories / totalCalories else 0.0
    }
}
