package com.example.forkit.data.models

// Exercise Logs Models for Exercise Logging Controller

// Request Models
data class CreateExerciseLogRequest(
    val userId: String,
    val name: String,
    val date: String,
    val caloriesBurnt: Double,
    val type: String, // Cardio or Strength
    val duration: Double? = null, // Duration in minutes
    val notes: String? = null
)

data class UpdateExerciseLogRequest(
    val name: String? = null,
    val date: String? = null,
    val caloriesBurnt: Double? = null,
    val type: String? = null,
    val duration: Double? = null,
    val notes: String? = null
)

// Response Models
data class ExerciseLogResponse(
    val success: Boolean,
    val message: String,
    val data: ExerciseLog
)

data class ExerciseLogsResponse(
    val success: Boolean,
    val message: String,
    val data: List<ExerciseLog>
)

data class DailyExerciseTotalResponse(
    val success: Boolean,
    val message: String,
    val data: DailyExerciseTotal
)

data class DailyExerciseSummaryResponse(
    val success: Boolean,
    val message: String,
    val data: DailyExerciseSummary
)

data class MonthlyExerciseSummaryResponse(
    val success: Boolean,
    val message: String,
    val data: MonthlyExerciseSummary
)

data class RecentExerciseActivityResponse(
    val success: Boolean,
    val message: String,
    val data: RecentExerciseActivity
)

data class ExerciseTrendsResponse(
    val success: Boolean,
    val message: String,
    val data: ExerciseTrends
)

data class ExerciseDashboardResponse(
    val success: Boolean,
    val message: String,
    val data: ExerciseDashboard
)

// Data Classes
data class ExerciseLog(
    val id: String,
    val userId: String,
    val name: String,
    val date: String,
    val caloriesBurnt: Double,
    val type: String,
    val duration: Double?,
    val notes: String,
    val createdAt: String,
    val updatedAt: String
)

data class DailyExerciseTotal(
    val userId: String,
    val date: String,
    val totalCaloriesBurnt: Double,
    val totalDuration: Double,
    val totalExercises: Int,
    val cardioExercises: Int,
    val strengthExercises: Int,
    val entries: List<ExerciseLog>
)

data class DailyExerciseSummary(
    val userId: String,
    val date: String,
    val totalCaloriesBurnt: Double,
    val totalDuration: Double,
    val totalExercises: Int,
    val typeBreakdown: List<ExerciseTypeBreakdown>,
    val hourlyDistribution: List<HourlyExerciseDistribution>,
    val averageCaloriesPerExercise: Double,
    val averageDurationPerExercise: Double
)

data class ExerciseTypeBreakdown(
    val type: String,
    val calories: Int,
    val duration: Int,
    val count: Int,
    val percentage: Int
)

data class HourlyExerciseDistribution(
    val hour: Int,
    val calories: Int,
    val duration: Int,
    val count: Int
)

data class MonthlyExerciseSummary(
    val userId: String,
    val year: Int,
    val month: Int,
    val totalCalories: Double,
    val totalDuration: Double,
    val totalExercises: Int,
    val averageDailyCalories: Double,
    val averageDailyDuration: Double,
    val daysWithData: Int,
    val monthlyTypeBreakdown: List<ExerciseTypeBreakdown>,
    val dailyTotals: List<DailyExerciseTotalEntry>
)

data class DailyExerciseTotalEntry(
    val date: String,
    val calories: Int,
    val duration: Int,
    val exercises: Int,
    val cardioCount: Int,
    val strengthCount: Int
)

data class RecentExerciseActivity(
    val userId: String,
    val recentActivity: List<RecentExerciseActivityEntry>,
    val count: Int
)

data class RecentExerciseActivityEntry(
    val id: String,
    val name: String,
    val type: String,
    val caloriesBurnt: Int,
    val duration: Int?,
    val date: String,
    val createdAt: String,
    val time: String
)

data class ExerciseTrends(
    val userId: String,
    val startDate: String,
    val endDate: String,
    val groupBy: String,
    val trends: List<ExerciseTrendEntry>,
    val totalDays: Int
)

data class ExerciseTrendEntry(
    val date: String,
    val calories: Int,
    val duration: Int,
    val exercises: Int,
    val cardioCount: Int,
    val strengthCount: Int
)

data class ExerciseDashboard(
    val userId: String,
    val date: String,
    val year: Int,
    val month: Int,
    val daily: DailyExerciseDashboardData,
    val monthly: MonthlyExerciseDashboardData,
    val recentActivity: RecentExerciseActivityData,
    val summary: ExerciseSummaryDashboardData
)

data class DailyExerciseDashboardData(
    val totalCalories: Int,
    val totalDuration: Int,
    val totalExercises: Int,
    val cardioExercises: Int,
    val strengthExercises: Int,
    val averageCaloriesPerExercise: Int
)

data class MonthlyExerciseDashboardData(
    val totalCalories: Int,
    val totalDuration: Int,
    val averageDailyCalories: Int,
    val daysWithData: Int,
    val totalExercises: Int
)

data class RecentExerciseActivityData(
    val entries: List<RecentExerciseActivityEntry>,
    val count: Int
)

data class ExerciseSummaryDashboardData(
    val totalCaloriesBurnt: Int,
    val monthlyTotal: Int,
    val totalExercises: Int,
    val cardioVsStrength: CardioVsStrength
)

data class CardioVsStrength(
    val cardio: Int,
    val strength: Int
)
