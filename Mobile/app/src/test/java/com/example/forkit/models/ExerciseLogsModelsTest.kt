package com.example.forkit.models

import com.example.forkit.data.models.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Exercise Log data models
 * Tests exercise tracking, calorie burn, and workout analytics
 */
class ExerciseLogsModelsTest {
    
    @Test
    fun `exercise log creates with all required fields`() {
        // Arrange & Act
        val exerciseLog = ExerciseLog(
            id = "exercise123",
            userId = "user123",
            name = "Morning Run",
            date = "2025-10-05",
            caloriesBurnt = 300.0,
            type = "Cardio",
            duration = 30.0,
            notes = "Felt great today!",
            createdAt = "2025-10-05T08:00:00Z",
            updatedAt = "2025-10-05T08:00:00Z"
        )
        
        // Assert
        assertEquals("exercise123", exerciseLog.id)
        assertEquals("user123", exerciseLog.userId)
        assertEquals("Morning Run", exerciseLog.name)
        assertEquals(300.0, exerciseLog.caloriesBurnt, 0.01)
        assertEquals("Cardio", exerciseLog.type)
        assertEquals(30.0, exerciseLog.duration)
        assertEquals("Felt great today!", exerciseLog.notes)
    }
    
    @Test
    fun `create exercise log request validates required fields`() {
        // Act
        val request = CreateExerciseLogRequest(
            userId = "user123",
            name = "Weight Training",
            date = "2025-10-05",
            caloriesBurnt = 400.0,
            type = "Strength",
            duration = 45.0,
            notes = "Heavy lifting session"
        )
        
        // Assert
        assertEquals("user123", request.userId)
        assertEquals("Weight Training", request.name)
        assertEquals(400.0, request.caloriesBurnt, 0.01)
        assertEquals("Strength", request.type)
        assertEquals(45.0, request.duration)
        assertEquals("Heavy lifting session", request.notes)
    }
    
    @Test
    fun `update exercise log request allows partial updates`() {
        // Act
        val request = UpdateExerciseLogRequest(
            name = "Updated Exercise",
            caloriesBurnt = 350.0,
            duration = 40.0
        )
        
        // Assert
        assertEquals("Updated Exercise", request.name)
        assertEquals(350.0, request.caloriesBurnt)
        assertEquals(40.0, request.duration)
        assertNull(request.type)
        assertNull(request.notes)
    }
    
    @Test
    fun `daily exercise total aggregates workout data`() {
        // Arrange
        val exerciseLogs = listOf(
            ExerciseLog("1", "user123", "Run", "2025-10-05", 300.0, "Cardio", 30.0, "", "", ""),
            ExerciseLog("2", "user123", "Weights", "2025-10-05", 200.0, "Strength", 20.0, "", "", "")
        )
        
        // Act
        val dailyTotal = DailyExerciseTotal(
            userId = "user123",
            date = "2025-10-05",
            totalCaloriesBurnt = 500.0,
            totalDuration = 50.0,
            totalExercises = 2,
            cardioExercises = 1,
            strengthExercises = 1,
            entries = exerciseLogs
        )
        
        // Assert
        assertEquals(500.0, dailyTotal.totalCaloriesBurnt, 0.01)
        assertEquals(50.0, dailyTotal.totalDuration, 0.01)
        assertEquals(2, dailyTotal.totalExercises)
        assertEquals(1, dailyTotal.cardioExercises)
        assertEquals(1, dailyTotal.strengthExercises)
        assertEquals(2, dailyTotal.entries.size)
    }
    
    @Test
    fun `exercise type breakdown analyzes workout distribution`() {
        // Act
        val cardioBreakdown = ExerciseTypeBreakdown(
            type = "Cardio",
            calories = 300,
            duration = 30,
            count = 1,
            percentage = 60
        )
        
        val strengthBreakdown = ExerciseTypeBreakdown(
            type = "Strength",
            calories = 200,
            duration = 20,
            count = 1,
            percentage = 40
        )
        
        // Assert
        assertEquals("Cardio", cardioBreakdown.type)
        assertEquals(300, cardioBreakdown.calories)
        assertEquals(60, cardioBreakdown.percentage)
        
        assertEquals("Strength", strengthBreakdown.type)
        assertEquals(200, strengthBreakdown.calories)
        assertEquals(40, strengthBreakdown.percentage)
    }
    
    @Test
    fun `hourly exercise distribution tracks workout timing`() {
        // Act
        val distribution = HourlyExerciseDistribution(
            hour = 18,
            calories = 400,
            duration = 40,
            count = 2
        )
        
        // Assert
        assertEquals(18, distribution.hour)
        assertEquals(400, distribution.calories)
        assertEquals(40, distribution.duration)
        assertEquals(2, distribution.count)
    }
    
    @Test
    fun `daily exercise summary provides detailed analytics`() {
        // Arrange
        val typeBreakdown = listOf(
            ExerciseTypeBreakdown("Cardio", 300, 30, 1, 60),
            ExerciseTypeBreakdown("Strength", 200, 20, 1, 40)
        )
        val hourlyDistribution = listOf(
            HourlyExerciseDistribution(8, 300, 30, 1),
            HourlyExerciseDistribution(18, 200, 20, 1)
        )
        
        // Act
        val summary = DailyExerciseSummary(
            userId = "user123",
            date = "2025-10-05",
            totalCaloriesBurnt = 500.0,
            totalDuration = 50.0,
            totalExercises = 2,
            typeBreakdown = typeBreakdown,
            hourlyDistribution = hourlyDistribution,
            averageCaloriesPerExercise = 250.0,
            averageDurationPerExercise = 25.0
        )
        
        // Assert
        assertEquals(500.0, summary.totalCaloriesBurnt, 0.01)
        assertEquals(50.0, summary.totalDuration, 0.01)
        assertEquals(2, summary.totalExercises)
        assertEquals(2, summary.typeBreakdown.size)
        assertEquals(2, summary.hourlyDistribution.size)
        assertEquals(250.0, summary.averageCaloriesPerExercise, 0.01)
        assertEquals(25.0, summary.averageDurationPerExercise, 0.01)
    }
    
    @Test
    fun `monthly exercise summary tracks long-term progress`() {
        // Arrange
        val monthlyTypeBreakdown = listOf(
            ExerciseTypeBreakdown("Cardio", 9000, 900, 30, 70),
            ExerciseTypeBreakdown("Strength", 4000, 400, 20, 30)
        )
        val dailyTotals = listOf(
            DailyExerciseTotalEntry("2025-10-01", 500, 50, 2, 1, 1),
            DailyExerciseTotalEntry("2025-10-02", 400, 40, 1, 1, 0)
        )
        
        // Act
        val monthlySummary = MonthlyExerciseSummary(
            userId = "user123",
            year = 2025,
            month = 10,
            totalCalories = 13000.0,
            totalDuration = 1300.0,
            totalExercises = 50,
            averageDailyCalories = 433.33,
            averageDailyDuration = 43.33,
            daysWithData = 30,
            monthlyTypeBreakdown = monthlyTypeBreakdown,
            dailyTotals = dailyTotals
        )
        
        // Assert
        assertEquals(2025, monthlySummary.year)
        assertEquals(10, monthlySummary.month)
        assertEquals(13000.0, monthlySummary.totalCalories, 0.01)
        assertEquals(433.33, monthlySummary.averageDailyCalories, 0.01)
        assertEquals(30, monthlySummary.daysWithData)
        assertEquals(2, monthlySummary.dailyTotals.size)
    }
    
    @Test
    fun `recent exercise activity tracks latest workouts`() {
        // Arrange
        val recentEntries = listOf(
            RecentExerciseActivityEntry("1", "Morning Run", "Cardio", 300, 30, "2025-10-05", "2025-10-05T08:00:00Z", "08:00"),
            RecentExerciseActivityEntry("2", "Evening Weights", "Strength", 200, 20, "2025-10-05", "2025-10-05T18:00:00Z", "18:00")
        )
        
        // Act
        val recentActivity = RecentExerciseActivity(
            userId = "user123",
            recentActivity = recentEntries,
            count = 2
        )
        
        // Assert
        assertEquals("user123", recentActivity.userId)
        assertEquals(2, recentActivity.recentActivity.size)
        assertEquals(2, recentActivity.count)
        assertEquals("Morning Run", recentActivity.recentActivity[0].name)
        assertEquals("Evening Weights", recentActivity.recentActivity[1].name)
    }
    
    @Test
    fun `exercise trends analyze workout patterns`() {
        // Arrange
        val trendEntries = listOf(
            ExerciseTrendEntry("2025-10-01", 500, 50, 2, 1, 1),
            ExerciseTrendEntry("2025-10-02", 400, 40, 1, 1, 0),
            ExerciseTrendEntry("2025-10-03", 600, 60, 3, 2, 1)
        )
        
        // Act
        val trends = ExerciseTrends(
            userId = "user123",
            startDate = "2025-10-01",
            endDate = "2025-10-03",
            groupBy = "daily",
            trends = trendEntries,
            totalDays = 3
        )
        
        // Assert
        assertEquals("user123", trends.userId)
        assertEquals("2025-10-01", trends.startDate)
        assertEquals("2025-10-03", trends.endDate)
        assertEquals("daily", trends.groupBy)
        assertEquals(3, trends.trends.size)
        assertEquals(3, trends.totalDays)
    }
    
    @Test
    fun `exercise dashboard provides comprehensive overview`() {
        // Arrange
        val dailyData = DailyExerciseDashboardData(500, 50, 2, 1, 1, 250)
        val monthlyData = MonthlyExerciseDashboardData(13000, 1300, 433, 30, 50)
        val recentActivityData = RecentExerciseActivityData(emptyList(), 0)
        val summaryData = ExerciseSummaryDashboardData(500, 13000, 50, CardioVsStrength(1, 1))
        
        // Act
        val dashboard = ExerciseDashboard(
            userId = "user123",
            date = "2025-10-05",
            year = 2025,
            month = 10,
            daily = dailyData,
            monthly = monthlyData,
            recentActivity = recentActivityData,
            summary = summaryData
        )
        
        // Assert
        assertEquals("user123", dashboard.userId)
        assertEquals("2025-10-05", dashboard.date)
        assertEquals(2025, dashboard.year)
        assertEquals(10, dashboard.month)
        assertEquals(500, dashboard.daily.totalCalories)
        assertEquals(13000, dashboard.monthly.totalCalories)
        assertEquals(500, dashboard.summary.totalCaloriesBurnt)
    }
    
    @Test
    fun `cardio vs strength comparison`() {
        // Act
        val comparison = CardioVsStrength(
            cardio = 15,
            strength = 10
        )
        
        // Assert
        assertEquals(15, comparison.cardio)
        assertEquals(10, comparison.strength)
    }
    
    @Test
    fun `exercise log response with success status`() {
        // Arrange
        val exerciseLog = ExerciseLog("1", "user123", "Test Exercise", "2025-10-05", 300.0, "Cardio", 30.0, "", "", "")
        
        // Act
        val response = ExerciseLogResponse(
            success = true,
            message = "Exercise log created successfully",
            data = exerciseLog
        )
        
        // Assert
        assertTrue(response.success)
        assertEquals("Exercise log created successfully", response.message)
        assertEquals(exerciseLog, response.data)
    }
}