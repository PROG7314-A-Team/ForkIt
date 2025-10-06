package com.example.forkit.business

import com.example.forkit.data.models.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Water Log data models
 * Tests water intake tracking, daily summaries, and dashboard data
 */
class WaterLogsModelsTest {
    
    @Test
    fun `water log creates with all required fields`() {
        // Arrange & Act
        val waterLog = WaterLog(
            id = "water123",
            userId = "user123",
            amount = 250.0,
            date = "2025-10-05",
            createdAt = "2025-10-05T10:00:00Z",
            updatedAt = "2025-10-05T10:00:00Z"
        )
        
        // Assert
        assertEquals("water123", waterLog.id)
        assertEquals("user123", waterLog.userId)
        assertEquals(250.0, waterLog.amount, 0.01)
        assertEquals("2025-10-05", waterLog.date)
    }
    
    @Test
    fun `create water log request validates required fields`() {
        // Act
        val request = CreateWaterLogRequest(
            userId = "user123",
            amount = 500.0,
            date = "2025-10-05"
        )
        
        // Assert
        assertEquals("user123", request.userId)
        assertEquals(500.0, request.amount, 0.01)
        assertEquals("2025-10-05", request.date)
    }
    
    @Test
    fun `update water log request allows partial updates`() {
        // Act
        val request = UpdateWaterLogRequest(
            amount = 300.0,
            date = "2025-10-06"
        )
        
        // Assert
        assertEquals(300.0, request.amount)
        assertEquals("2025-10-06", request.date)
    }
    
    @Test
    fun `daily water total calculates correctly`() {
        // Act
        val dailyTotal = DailyWaterTotal(
            userId = "user123",
            date = "2025-10-05",
            totalAmount = 2000.0,
            entries = 8
        )
        
        // Assert
        assertEquals("user123", dailyTotal.userId)
        assertEquals("2025-10-05", dailyTotal.date)
        assertEquals(2000.0, dailyTotal.totalAmount, 0.01)
        assertEquals(8, dailyTotal.entries)
    }
    
    @Test
    fun `daily water summary includes goal tracking`() {
        // Arrange
        val hourlyDistribution = listOf(
            HourlyWaterDistribution(8, 250, 1, 250),
            HourlyWaterDistribution(12, 500, 2, 250),
            HourlyWaterDistribution(18, 300, 1, 300)
        )
        
        // Act
        val summary = DailyWaterSummary(
            userId = "user123",
            date = "2025-10-05",
            totalAmount = 2000.0,
            totalEntries = 4,
            averagePerEntry = 500.0,
            hourlyDistribution = hourlyDistribution,
            goal = 2500,
            remaining = 500.0,
            goalPercentage = 80
        )
        
        // Assert
        assertEquals(2000.0, summary.totalAmount, 0.01)
        assertEquals(4, summary.totalEntries)
        assertEquals(500.0, summary.averagePerEntry, 0.01)
        assertEquals(3, summary.hourlyDistribution.size)
        assertEquals(2500, summary.goal)
        assertEquals(500.0, summary.remaining, 0.01)
        assertEquals(80, summary.goalPercentage)
    }
    
    @Test
    fun `hourly water distribution tracks intake patterns`() {
        // Act
        val distribution = HourlyWaterDistribution(
            hour = 14,
            amount = 300,
            count = 2,
            average = 150
        )
        
        // Assert
        assertEquals(14, distribution.hour)
        assertEquals(300, distribution.amount)
        assertEquals(2, distribution.count)
        assertEquals(150, distribution.average)
    }
    
    @Test
    fun `monthly water summary provides comprehensive overview`() {
        // Arrange
        val dailyTotals = listOf(
            DailyWaterTotalEntry("2025-10-01", 2000, 8, true),
            DailyWaterTotalEntry("2025-10-02", 1800, 6, false),
            DailyWaterTotalEntry("2025-10-03", 2500, 10, true)
        )
        
        // Act
        val monthlySummary = MonthlyWaterSummary(
            userId = "user123",
            year = 2025,
            month = 10,
            totalAmount = 6300.0,
            totalEntries = 24,
            averageDailyAmount = 2100.0,
            daysWithData = 3,
            dailyGoal = 2000,
            goalAchievement = 67,
            dailyTotals = dailyTotals
        )
        
        // Assert
        assertEquals(2025, monthlySummary.year)
        assertEquals(10, monthlySummary.month)
        assertEquals(6300.0, monthlySummary.totalAmount, 0.01)
        assertEquals(2100.0, monthlySummary.averageDailyAmount, 0.01)
        assertEquals(3, monthlySummary.daysWithData)
        assertEquals(67, monthlySummary.goalAchievement)
        assertEquals(3, monthlySummary.dailyTotals.size)
    }
    
    @Test
    fun `recent water activity tracks latest entries`() {
        // Arrange
        val recentEntries = listOf(
            RecentWaterActivityEntry("1", 250, "2025-10-05", "2025-10-05T10:00:00Z", "10:00"),
            RecentWaterActivityEntry("2", 300, "2025-10-05", "2025-10-05T14:00:00Z", "14:00")
        )
        
        // Act
        val recentActivity = RecentWaterActivity(
            userId = "user123",
            recentActivity = recentEntries,
            count = 2
        )
        
        // Assert
        assertEquals("user123", recentActivity.userId)
        assertEquals(2, recentActivity.recentActivity.size)
        assertEquals(2, recentActivity.count)
        assertEquals(250, recentActivity.recentActivity[0].amount)
        assertEquals(300, recentActivity.recentActivity[1].amount)
    }
    
    @Test
    fun `water trends analyze intake patterns`() {
        // Arrange
        val trendEntries = listOf(
            WaterTrendEntry("2025-10-01", 2000, 8),
            WaterTrendEntry("2025-10-02", 1800, 6),
            WaterTrendEntry("2025-10-03", 2200, 9)
        )
        
        // Act
        val trends = WaterTrends(
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
    fun `water dashboard provides comprehensive overview`() {
        // Arrange
        val dailyData = DailyWaterDashboardData(2000, 8, 2500, 500, 80, 250)
        val monthlyData = MonthlyWaterDashboardData(60000, 2000, 30, 240, 80)
        val recentActivityData = RecentWaterActivityData(emptyList(), 0)
        val summaryData = WaterSummaryDashboardData(2000, 60000, 500, false)
        
        // Act
        val dashboard = WaterDashboard(
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
        assertEquals(2000, dashboard.daily.totalAmount)
        assertEquals(60000, dashboard.monthly.totalAmount)
        assertEquals(2000, dashboard.summary.totalWaterIntake)
    }
    
    @Test
    fun `water log response with success status`() {
        // Arrange
        val waterLog = WaterLog("1", "user123", 250.0, "2025-10-05", "", "")
        
        // Act
        val response = WaterLogResponse(
            success = true,
            message = "Water log created successfully",
            data = waterLog
        )
        
        // Assert
        assertTrue(response.success)
        assertEquals("Water log created successfully", response.message)
        assertEquals(waterLog, response.data)
    }
}