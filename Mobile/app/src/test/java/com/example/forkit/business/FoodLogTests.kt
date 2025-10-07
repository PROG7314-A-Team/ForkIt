package com.example.forkit.business

import com.example.forkit.data.models.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Food Log data models
 * Tests food logging, daily summaries, and nutritional tracking
 */
class FoodLogsModelsTest {
    
    @Test
    fun `food log creates with all required fields`() {
        // Arrange & Act
        val foodLog = FoodLog(
            id = "foodlog123",
            userId = "user123",
            foodName = "Chicken Breast",
            servingSize = 250.0,
            measuringUnit = "grams",
            date = "2025-10-05",
            mealType = "Lunch",
            calories = 300.0,
            carbs = 0.0,
            fat = 6.0,
            protein = 55.0,
            foodId = "food123",
            createdAt = "2025-10-05T12:00:00Z",
            updatedAt = "2025-10-05T12:00:00Z"
        )
        
        // Assert
        assertEquals("foodlog123", foodLog.id)
        assertEquals("user123", foodLog.userId)
        assertEquals("Chicken Breast", foodLog.foodName)
        assertEquals(250.0, foodLog.servingSize, 0.01)
        assertEquals("grams", foodLog.measuringUnit)
        assertEquals("Lunch", foodLog.mealType)
        assertEquals(300.0, foodLog.calories, 0.01)
        assertEquals(55.0, foodLog.protein, 0.01)
    }
    
    @Test
    fun `daily calorie summary aggregates nutritional data`() {
        // Arrange
        val mealDistribution = listOf(
            MealDistributionEntry("Breakfast", 400.0, 25),
            MealDistributionEntry("Lunch", 600.0, 37),
            MealDistributionEntry("Dinner", 500.0, 31),
            MealDistributionEntry("Snacks", 100.0, 7)
        )
        val mealTotals = MealTotals(
            Breakfast = 400.0,
            Lunch = 600.0,
            Dinner = 500.0,
            Snacks = 100.0
        )
        
        // Act
        val dailySummary = DailyCalorieSummary(
            userId = "user123",
            date = "2025-10-05",
            totalCalories = 1600.0,
            totalCarbs = 200.0,
            totalFat = 60.0,
            totalProtein = 120.0,
            mealDistribution = mealDistribution,
            mealTotals = mealTotals,
            entryCount = 8
        )
        
        // Assert
        assertEquals("user123", dailySummary.userId)
        assertEquals("2025-10-05", dailySummary.date)
        assertEquals(1600.0, dailySummary.totalCalories, 0.01)
        assertEquals(200.0, dailySummary.totalCarbs, 0.01)
        assertEquals(60.0, dailySummary.totalFat, 0.01)
        assertEquals(120.0, dailySummary.totalProtein, 0.01)
        assertEquals(4, dailySummary.mealDistribution.size)
        assertEquals(8, dailySummary.entryCount)
    }
    
    @Test
    fun `meal distribution entry tracks meal percentages`() {
        // Act
        val breakfastEntry = MealDistributionEntry(
            mealType = "Breakfast",
            calories = 400.0,
            percentage = 25
        )
        
        val lunchEntry = MealDistributionEntry(
            mealType = "Lunch",
            calories = 600.0,
            percentage = 37
        )
        
        // Assert
        assertEquals("Breakfast", breakfastEntry.mealType)
        assertEquals(400.0, breakfastEntry.calories, 0.001)
        assertEquals(25, breakfastEntry.percentage)
        
        assertEquals("Lunch", lunchEntry.mealType)
        assertEquals(600.0, lunchEntry.calories, 0.001)
        assertEquals(37, lunchEntry.percentage)
    }
    
    @Test
    fun `meal totals sum all meal types`() {
        // Act
        val mealTotals = MealTotals(
            Breakfast = 400.0,
            Lunch = 600.0,
            Dinner = 500.0,
            Snacks = 100.0
        )
        
        // Assert
        assertEquals(400.0, mealTotals.Breakfast, 0.001)
        assertEquals(600.0, mealTotals.Lunch, 0.001)
        assertEquals(500.0, mealTotals.Dinner, 0.001)
        assertEquals(100.0, mealTotals.Snacks, 0.001)
        
        // Verify total
        val total = mealTotals.Breakfast + mealTotals.Lunch + mealTotals.Dinner + mealTotals.Snacks
        assertEquals(1600.0, total, 0.001)
    }
    
    @Test
    fun `monthly calorie summary tracks long-term nutrition`() {
        // Arrange
        val dailyTotals = mapOf(
            "2025-10-01" to DailyTotalEntry(1600, 200, 60, 120, 8),
            "2025-10-02" to DailyTotalEntry(1800, 220, 70, 140, 10),
            "2025-10-03" to DailyTotalEntry(1500, 180, 50, 110, 6)
        )
        
        // Act
        val monthlySummary = MonthlyCalorieSummary(
            userId = "user123",
            year = 2025,
            month = 10,
            totalCalories = 4900.0,
            totalCarbs = 600.0,
            totalFat = 180.0,
            totalProtein = 370.0,
            averageDailyCalories = 1633.33,
            daysWithData = 3,
            dailyTotals = dailyTotals,
            entryCount = 24
        )
        
        // Assert
        assertEquals(2025, monthlySummary.year)
        assertEquals(10, monthlySummary.month)
        assertEquals(4900.0, monthlySummary.totalCalories, 0.01)
        assertEquals(1633.33, monthlySummary.averageDailyCalories, 0.01)
        assertEquals(3, monthlySummary.daysWithData)
        assertEquals(3, monthlySummary.dailyTotals.size)
        assertEquals(24, monthlySummary.entryCount)
    }
    
    @Test
    fun `daily total entry tracks daily nutrition`() {
        // Act
        val dailyEntry = DailyTotalEntry(
            calories = 1600,
            carbs = 200,
            fat = 60,
            protein = 120,
            entryCount = 8
        )
        
        // Assert
        assertEquals(1600, dailyEntry.calories)
        assertEquals(200, dailyEntry.carbs)
        assertEquals(60, dailyEntry.fat)
        assertEquals(120, dailyEntry.protein)
        assertEquals(8, dailyEntry.entryCount)
    }
    
    @Test
    fun `recent food activity tracks latest entries`() {
        // Arrange
        val recentEntries = listOf(
            RecentActivityEntry("1", "Apple", 150.0, "grams", 80, "Snack", "2025-10-05", "2025-10-05T15:00:00Z", "15:00"),
            RecentActivityEntry("2", "Banana", 120.0, "grams", 105, "Snack", "2025-10-05", "2025-10-05T16:00:00Z", "16:00")
        )
        
        // Act
        val recentActivity = RecentFoodActivity(
            userId = "user123",
            recentActivity = recentEntries,
            count = 2
        )
        
        // Assert
        assertEquals("user123", recentActivity.userId)
        assertEquals(2, recentActivity.recentActivity.size)
        assertEquals(2, recentActivity.count)
        assertEquals("Apple", recentActivity.recentActivity[0].foodName)
        assertEquals("Banana", recentActivity.recentActivity[1].foodName)
    }
    
    @Test
    fun `recent activity entry includes timing information`() {
        // Act
        val activityEntry = RecentActivityEntry(
            id = "entry123",
            foodName = "Greek Yogurt",
            servingSize = 200.0,
            measuringUnit = "grams",
            calories = 120,
            mealType = "Breakfast",
            date = "2025-10-05",
            createdAt = "2025-10-05T08:30:00Z",
            time = "08:30"
        )
        
        // Assert
        assertEquals("entry123", activityEntry.id)
        assertEquals("Greek Yogurt", activityEntry.foodName)
        assertEquals(200.0, activityEntry.servingSize, 0.01)
        assertEquals("grams", activityEntry.measuringUnit)
        assertEquals(120, activityEntry.calories)
        assertEquals("Breakfast", activityEntry.mealType)
        assertEquals("2025-10-05", activityEntry.date)
        assertEquals("08:30", activityEntry.time)
    }
    
    @Test
    fun `food log with different meal types`() {
        // Test different meal types
        val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snacks")
        
        mealTypes.forEach { mealType ->
            val foodLog = FoodLog(
                id = "log_$mealType",
                userId = "user123",
                foodName = "Test Food",
                servingSize = 100.0,
                measuringUnit = "grams",
                date = "2025-10-05",
                mealType = mealType,
                calories = 100.0,
                carbs = 10.0,
                fat = 5.0,
                protein = 8.0,
                foodId = null,
                createdAt = "2025-10-05T12:00:00Z",
                updatedAt = "2025-10-05T12:00:00Z"
            )
            
            assertEquals(mealType, foodLog.mealType)
        }
    }
    
    @Test
    fun `food log with different measuring units`() {
        // Test different measuring units
        val units = listOf("grams", "cups", "pieces", "ml", "tbsp")
        
        units.forEach { unit ->
            val foodLog = FoodLog(
                id = "log_$unit",
                userId = "user123",
                foodName = "Test Food",
                servingSize = 1.0,
                measuringUnit = unit,
                date = "2025-10-05",
                mealType = "Breakfast",
                calories = 100.0,
                carbs = 10.0,
                fat = 5.0,
                protein = 8.0,
                foodId = null,
                createdAt = "2025-10-05T12:00:00Z",
                updatedAt = "2025-10-05T12:00:00Z"
            )
            
            assertEquals(unit, foodLog.measuringUnit)
        }
    }
}