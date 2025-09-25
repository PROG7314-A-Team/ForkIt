package com.example.forkit.data.models

// Food Logs Models for Food Logging Controller

// Request Models
data class CreateFoodLogRequest(
    val userId: String,
    val foodName: String,
    val servingSize: Double,
    val measuringUnit: String,
    val date: String,
    val mealType: String,
    val calories: Double,
    val carbs: Double,
    val fat: Double,
    val protein: Double,
    val foodId: String? = null
)

data class UpdateFoodLogRequest(
    val foodName: String? = null,
    val servingSize: Double? = null,
    val measuringUnit: String? = null,
    val date: String? = null,
    val mealType: String? = null,
    val calories: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val protein: Double? = null,
    val foodId: String? = null
)

// Response Models
data class FoodLogResponse(
    val success: Boolean,
    val message: String,
    val data: FoodLog
)

data class FoodLogsResponse(
    val success: Boolean,
    val message: String,
    val data: List<FoodLog>
)

data class DailyCalorieSummaryResponse(
    val success: Boolean,
    val message: String,
    val data: DailyCalorieSummary
)

data class MonthlyCalorieSummaryResponse(
    val success: Boolean,
    val message: String,
    val data: MonthlyCalorieSummary
)

data class RecentFoodActivityResponse(
    val success: Boolean,
    val message: String,
    val data: RecentFoodActivity
)

data class CalorieTrendsResponse(
    val success: Boolean,
    val message: String,
    val data: CalorieTrends
)

data class FoodLogDashboardResponse(
    val success: Boolean,
    val message: String,
    val data: FoodLogDashboard
)

// Data Classes
data class FoodLog(
    val id: String,
    val userId: String,
    val foodName: String,
    val servingSize: Double,
    val measuringUnit: String,
    val date: String,
    val mealType: String,
    val calories: Double,
    val carbs: Double,
    val fat: Double,
    val protein: Double,
    val foodId: String?,
    val createdAt: String,
    val updatedAt: String
)

data class DailyCalorieSummary(
    val userId: String,
    val date: String,
    val totalCalories: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val totalProtein: Double,
    val mealDistribution: List<MealDistributionEntry>,
    val mealTotals: MealTotals,
    val entryCount: Int
)

data class MealDistributionEntry(
    val mealType: String,
    val calories: Int,
    val percentage: Int
)

data class MealTotals(
    val Breakfast: Int,
    val Lunch: Int,
    val Dinner: Int,
    val Snacks: Int
)

data class MonthlyCalorieSummary(
    val userId: String,
    val year: Int,
    val month: Int,
    val totalCalories: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val totalProtein: Double,
    val averageDailyCalories: Double,
    val daysWithData: Int,
    val dailyTotals: Map<String, DailyTotalEntry>,
    val entryCount: Int
)

data class DailyTotalEntry(
    val calories: Int,
    val carbs: Int,
    val fat: Int,
    val protein: Int,
    val entryCount: Int
)

data class RecentFoodActivity(
    val userId: String,
    val recentActivity: List<RecentActivityEntry>,
    val count: Int
)

data class RecentActivityEntry(
    val id: String,
    val foodName: String,
    val servingSize: Double,
    val measuringUnit: String,
    val calories: Int,
    val mealType: String,
    val date: String,
    val createdAt: String,
    val time: String
)

data class CalorieTrends(
    val userId: String,
    val startDate: String,
    val endDate: String,
    val groupBy: String,
    val trends: List<TrendEntry>,
    val totalDays: Int
)

data class TrendEntry(
    val date: String,
    val calories: Int,
    val carbs: Int,
    val fat: Int,
    val protein: Int,
    val entryCount: Int
)

data class FoodLogDashboard(
    val userId: String,
    val date: String,
    val year: Int,
    val month: Int,
    val daily: DailyDashboardData,
    val monthly: MonthlyDashboardData,
    val recentActivity: RecentFoodActivityData,
    val summary: SummaryDashboardData
)

data class DailyDashboardData(
    val totalCalories: Int,
    val totalCarbs: Int,
    val totalFat: Int,
    val totalProtein: Int,
    val mealDistribution: List<MealDistributionEntry>,
    val remainingCalories: Int,
    val dailyGoal: Int,
    val entryCount: Int
)

data class MonthlyDashboardData(
    val consumed: Int,
    val averageDaily: Int,
    val daysWithData: Int,
    val dailyBreakdown: List<DailyBreakdownEntry>
)

data class DailyBreakdownEntry(
    val date: String,
    val calories: Int
)

data class RecentFoodActivityData(
    val entries: List<RecentActivityEntry>,
    val count: Int
)

data class SummaryDashboardData(
    val totalCaloricIntake: Int,
    val consumed: Int,
    val remaining: Int,
    val mealBreakdown: List<MealDistributionEntry>
)