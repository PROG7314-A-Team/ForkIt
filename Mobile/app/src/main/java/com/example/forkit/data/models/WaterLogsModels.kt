package com.example.forkit.data.models

// Water Logs Models for Water Logging Controller

// Request Models
data class CreateWaterLogRequest(
    val userId: String,
    val amount: Double, // Amount in milliliters
    val date: String
)

data class UpdateWaterLogRequest(
    val amount: Double? = null, // Amount in milliliters
    val date: String? = null
)

// Response Models
data class WaterLogResponse(
    val success: Boolean,
    val message: String,
    val data: WaterLog
)

data class WaterLogsResponse(
    val success: Boolean,
    val message: String,
    val data: List<WaterLog>
)

data class DailyWaterTotalResponse(
    val success: Boolean,
    val message: String,
    val data: DailyWaterTotal
)

data class DailyWaterSummaryResponse(
    val success: Boolean,
    val message: String,
    val data: DailyWaterSummary
)

data class MonthlyWaterSummaryResponse(
    val success: Boolean,
    val message: String,
    val data: MonthlyWaterSummary
)

data class RecentWaterActivityResponse(
    val success: Boolean,
    val message: String,
    val data: RecentWaterActivity
)

data class WaterTrendsResponse(
    val success: Boolean,
    val message: String,
    val data: WaterTrends
)

data class WaterDashboardResponse(
    val success: Boolean,
    val message: String,
    val data: WaterDashboard
)


// Data Classes
data class WaterLog(
    val id: String,
    val userId: String,
    val amount: Double,
    val date: String,
    val createdAt: String,
    val updatedAt: String
)

data class DailyWaterTotal(
    val userId: String,
    val date: String,
    val totalAmount: Double,
    val entries: Int
)

data class DailyWaterSummary(
    val userId: String,
    val date: String,
    val totalAmount: Double,
    val totalEntries: Int,
    val averagePerEntry: Double,
    val hourlyDistribution: List<HourlyWaterDistribution>,
    val goal: Int,
    val remaining: Double,
    val goalPercentage: Int
)

data class HourlyWaterDistribution(
    val hour: Int,
    val amount: Int,
    val count: Int,
    val average: Int
)

data class MonthlyWaterSummary(
    val userId: String,
    val year: Int,
    val month: Int,
    val totalAmount: Double,
    val totalEntries: Int,
    val averageDailyAmount: Double,
    val daysWithData: Int,
    val dailyGoal: Int,
    val goalAchievement: Int,
    val dailyTotals: List<DailyWaterTotalEntry>
)

data class DailyWaterTotalEntry(
    val date: String,
    val amount: Int,
    val entries: Int,
    val goalMet: Boolean
)

data class RecentWaterActivity(
    val userId: String,
    val recentActivity: List<RecentWaterActivityEntry>,
    val count: Int
)

data class RecentWaterActivityEntry(
    val id: String,
    val amount: Int,
    val date: String,
    val createdAt: String,
    val time: String
)

data class WaterTrends(
    val userId: String,
    val startDate: String,
    val endDate: String,
    val groupBy: String,
    val trends: List<WaterTrendEntry>,
    val totalDays: Int
)

data class WaterTrendEntry(
    val date: String,
    val amount: Int,
    val entries: Int
)

data class WaterDashboard(
    val userId: String,
    val date: String,
    val year: Int,
    val month: Int,
    val daily: DailyWaterDashboardData,
    val monthly: MonthlyWaterDashboardData,
    val recentActivity: RecentWaterActivityData,
    val summary: WaterSummaryDashboardData
)

data class DailyWaterDashboardData(
    val totalAmount: Int,
    val totalEntries: Int,
    val dailyGoal: Int,
    val remaining: Int,
    val goalPercentage: Int,
    val averagePerEntry: Int
)

data class MonthlyWaterDashboardData(
    val totalAmount: Int,
    val averageDaily: Int,
    val daysWithData: Int,
    val totalEntries: Int,
    val goalAchievement: Int
)

data class RecentWaterActivityData(
    val entries: List<RecentWaterActivityEntry>,
    val count: Int
)

data class WaterSummaryDashboardData(
    val totalWaterIntake: Int,
    val monthlyTotal: Int,
    val remaining: Int,
    val goalMet: Boolean
)
