const FirebaseService = require("../services/firebaseService");
const CalorieCalculatorService = require("../services/calorieCalculatorService");
const StreakService = require("../services/streakService");

const foodLogService = new FirebaseService("foodLogs");
const mealLogService = new FirebaseService("mealLogs");
const calorieCalculator = new CalorieCalculatorService();
const streakService = new StreakService();

// Get all food logs
exports.getFoodLogs = async (req, res) => {
  try {
    console.log("Get food logs query", req.query);
    const { userId } = req.params; // Read userId from path parameters
    const { date } = req.query;    // Read date from query parameters
    let filters = [];

    if (userId) {
      filters.push({ field: "userId", operator: "==", value: userId });
    }

    if (date) {
      filters.push({ field: "date", operator: "==", value: date });
    }

    console.log("Filters for search", filters);
    const foodLogs = await foodLogService.query(filters, "createdAt", "desc");

    res.json({
      success: true,
      data: foodLogs,
      message: "Food logs retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve food logs",
    });
  }
};

// Get food log by ID
exports.getFoodLogById = async (req, res) => {
  try {
    const { id } = req.params;
    const foodLog = await foodLogService.getById(id);

    if (!foodLog || !foodLog.exists) {
      return res.status(404).json({
        success: false,
        message: "Food log not found",
      });
    }

    res.json({
      success: true,
      data: { id: foodLog.id, ...foodLog.data() },
      message: "Food log retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve food log",
    });
  }
};

// Create new food log entry
exports.createFoodLog = async (req, res) => {
  try {
    const {
      userId,
      foodName,
      servingSize,
      measuringUnit, // Cup, ML, Grams, etc.
      date,
      mealType, // Breakfast, Lunch, Dinner, Snacks
      calories,
      carbs,
      fat,
      protein,
      foodId, // Optional: reference to food database
    } = req.body;

    // Validation
    if (
      !userId ||
      !foodName ||
      !servingSize ||
      !measuringUnit ||
      !date ||
      !mealType
    ) {
      return res.status(400).json({
        success: false,
        message:
          "userId, foodName, servingSize, measuringUnit, date, and mealType are required",
      });
    }

    // Calculate calories using the calorie calculator
    const calorieData = calorieCalculator.calculateFoodCalories({
      calories: parseFloat(calories) || 0,
      carbs: parseFloat(carbs) || 0,
      fat: parseFloat(fat) || 0,
      protein: parseFloat(protein) || 0,
    });

    // Validate that we have valid calorie data
    if (!calorieData.validation.isValid) {
      return res.status(400).json({
        success: false,
        message: calorieData.validation.message,
        data: calorieData,
      });
    }

    const foodLogData = {
      userId,
      foodName,
      servingSize: parseFloat(servingSize),
      measuringUnit,
      date,
      mealType,
      calories: calorieData.totalCalories,
      carbs: parseFloat(carbs) || 0,
      fat: parseFloat(fat) || 0,
      protein: parseFloat(protein) || 0,
      foodId: foodId || null,
    };

    const foodLog = await foodLogService.create(foodLogData);

    // Update user streak
    const response = await streakService.updateUserStreak(userId, date);
    console.log("create food log response", response);

    res.status(201).json({
      success: true,
      data: {
        ...foodLog,
        calorieCalculation: calorieData,
      },
      message: "Food log created successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to create food log",
    });
  }
};

// Update food log entry
exports.updateFoodLog = async (req, res) => {
  try {
    const { id } = req.params;
    const updateData = req.body;

    if (!id) {
      return res.status(400).json({
        success: false,
        message: "Food log ID is required",
      });
    }

    const foodLog = await foodLogService.update(id, updateData);

    res.json({
      success: true,
      data: foodLog,
      message: "Food log updated successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to update food log",
    });
  }
};

// Delete food log entry
exports.deleteFoodLog = async (req, res) => {
  try {
    const { id } = req.params;

    if (!id) {
      return res.status(400).json({
        success: false,
        message: "Food log ID is required",
      });
    }

    const deletedFoodLog = await foodLogService.delete(id);

    res.json({
      success: true,
      data: deletedFoodLog,
      message: "Food log deleted successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to delete food log",
    });
  }
};

// Get food logs by user and date range
exports.getFoodLogsByDateRange = async (req, res) => {
  try {
    const { userId, startDate, endDate } = req.query;

    if (!userId || !startDate || !endDate) {
      return res.status(400).json({
        success: false,
        message: "userId, startDate, and endDate are required",
      });
    }

    const filters = [
      { field: "userId", operator: "==", value: userId },
      { field: "date", operator: ">=", value: startDate },
      { field: "date", operator: "<=", value: endDate },
    ];

    const foodLogs = await foodLogService.query(filters, "date", "asc");

    res.json({
      success: true,
      data: foodLogs,
      message: "Food logs retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve food logs",
    });
  }
};

// Get daily calorie summary for dashboard
exports.getDailyCalorieSummary = async (req, res) => {
  try {
    const { userId, date } = req.query;

    if (!userId || !date) {
      return res.status(400).json({
        success: false,
        message: "userId and date are required",
      });
    }

    const filters = [
      { field: "userId", operator: "==", value: userId },
      { field: "date", operator: "==", value: date },
    ];

    const foodLogs = await foodLogService.query(filters);

    // Calculate totals by meal type
    const mealTotals = {
      Breakfast: 0,
      Lunch: 0,
      Dinner: 0,
      Snacks: 0,
    };

    let totalCalories = 0;
    let totalCarbs = 0;
    let totalFat = 0;
    let totalProtein = 0;

    foodLogs.forEach((log) => {
      const calories = parseFloat(log.calories) || 0;
      const mealType = log.mealType || "Snacks";

      totalCalories += calories;
      totalCarbs += parseFloat(log.carbs) || 0;
      totalFat += parseFloat(log.fat) || 0;
      totalProtein += parseFloat(log.protein) || 0;

      if (mealTotals.hasOwnProperty(mealType)) {
        mealTotals[mealType] += calories;
      } else {
        mealTotals.Snacks += calories;
      }
    });

    // Create meal distribution data for donut chart
    const mealDistribution = Object.entries(mealTotals)
      .filter(([_, calories]) => calories > 0)
      .map(([mealType, calories]) => ({
        mealType,
        calories: Math.round(calories),
        percentage:
          totalCalories > 0 ? Math.round((calories / totalCalories) * 100) : 0,
      }));

    res.json({
      success: true,
      data: {
        userId,
        date,
        totalCalories: Math.round(totalCalories),
        totalCarbs: Math.round(totalCarbs),
        totalFat: Math.round(totalFat),
        totalProtein: Math.round(totalProtein),
        mealDistribution,
        mealTotals,
        entryCount: foodLogs.length,
      },
      message: "Daily calorie summary retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve daily calorie summary",
    });
  }
};

// Get monthly calorie summary for dashboard
exports.getMonthlyCalorieSummary = async (req, res) => {
  try {
    const { userId, year, month } = req.query;

    if (!userId || !year || !month) {
      return res.status(400).json({
        success: false,
        message: "userId, year, and month are required",
      });
    }

    // Create date range for the month
    const startDate = `${year}-${month.toString().padStart(2, "0")}-01`;
    const endDate = `${year}-${month.toString().padStart(2, "0")}-31`;

    const filters = [
      { field: "userId", operator: "==", value: userId },
      { field: "date", operator: ">=", value: startDate },
      { field: "date", operator: "<=", value: endDate },
    ];

    const foodLogs = await foodLogService.query(filters, "date", "asc");

    // Calculate daily totals
    const dailyTotals = {};
    let monthlyTotalCalories = 0;
    let monthlyTotalCarbs = 0;
    let monthlyTotalFat = 0;
    let monthlyTotalProtein = 0;

    foodLogs.forEach((log) => {
      const date = log.date;
      const calories = parseFloat(log.calories) || 0;

      if (!dailyTotals[date]) {
        dailyTotals[date] = {
          calories: 0,
          carbs: 0,
          fat: 0,
          protein: 0,
          entryCount: 0,
        };
      }

      dailyTotals[date].calories += calories;
      dailyTotals[date].carbs += parseFloat(log.carbs) || 0;
      dailyTotals[date].fat += parseFloat(log.fat) || 0;
      dailyTotals[date].protein += parseFloat(log.protein) || 0;
      dailyTotals[date].entryCount += 1;

      monthlyTotalCalories += calories;
      monthlyTotalCarbs += parseFloat(log.carbs) || 0;
      monthlyTotalFat += parseFloat(log.fat) || 0;
      monthlyTotalProtein += parseFloat(log.protein) || 0;
    });

    // Calculate average daily calories
    const daysWithData = Object.keys(dailyTotals).length;
    const averageDailyCalories =
      daysWithData > 0 ? monthlyTotalCalories / daysWithData : 0;

    res.json({
      success: true,
      data: {
        userId,
        year: parseInt(year),
        month: parseInt(month),
        totalCalories: Math.round(monthlyTotalCalories),
        totalCarbs: Math.round(monthlyTotalCarbs),
        totalFat: Math.round(monthlyTotalFat),
        totalProtein: Math.round(monthlyTotalProtein),
        averageDailyCalories: Math.round(averageDailyCalories),
        daysWithData,
        dailyTotals,
        entryCount: foodLogs.length,
      },
      message: "Monthly calorie summary retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve monthly calorie summary",
    });
  }
};

// Get recent food activity for dashboard
exports.getRecentFoodActivity = async (req, res) => {
  try {
    const { userId, limit = 10 } = req.query;

    if (!userId) {
      return res.status(400).json({
        success: false,
        message: "userId is required",
      });
    }

    const filters = [{ field: "userId", operator: "==", value: userId }];

    const foodLogs = await foodLogService.query(
      filters,
      "createdAt",
      "desc",
      parseInt(limit)
    );

    // Format for recent activity display
    const recentActivity = foodLogs.map((log) => ({
      id: log.id,
      foodName: log.foodName,
      servingSize: log.servingSize,
      measuringUnit: log.measuringUnit,
      calories: Math.round(parseFloat(log.calories) || 0),
      mealType: log.mealType,
      date: log.date,
      createdAt: log.createdAt,
      time: new Date(log.createdAt).toLocaleTimeString("en-US", {
        hour: "2-digit",
        minute: "2-digit",
        hour12: true,
      }),
    }));

    res.json({
      success: true,
      data: {
        userId,
        recentActivity,
        count: recentActivity.length,
      },
      message: "Recent food activity retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve recent food activity",
    });
  }
};

// Get calorie trends over time
exports.getCalorieTrends = async (req, res) => {
  try {
    const { userId, startDate, endDate, groupBy = "day" } = req.query;

    if (!userId || !startDate || !endDate) {
      return res.status(400).json({
        success: false,
        message: "userId, startDate, and endDate are required",
      });
    }

    const filters = [
      { field: "userId", operator: "==", value: userId },
      { field: "date", operator: ">=", value: startDate },
      { field: "date", operator: "<=", value: endDate },
    ];

    const [foodLogs, mealLogs] = await Promise.all([
      foodLogService.query(filters, "date", "asc"),
      mealLogService.query(
        [
          { field: "userId", operator: "==", value: userId },
          { field: "date", operator: ">=", value: startDate },
          { field: "date", operator: "<=", value: endDate },
        ],
        "date",
        "asc"
      ),
    ]);

    // Normalize logs to a common structure
    const normalizedLogs = [
      ...foodLogs.map((log) => ({
        date: log.date,
        calories: parseFloat(log.calories) || 0,
        carbs: parseFloat(log.carbs) || 0,
        fat: parseFloat(log.fat) || 0,
        protein: parseFloat(log.protein) || 0,
      })),
      ...mealLogs
        .filter((log) => !log.isTemplate && log.date)
        .map((log) => ({
          date: log.date,
          calories: parseFloat(log.totalCalories) || 0,
          carbs: parseFloat(log.totalCarbs) || 0,
          fat: parseFloat(log.totalFat) || 0,
          protein: parseFloat(log.totalProtein) || 0,
        })),
    ];

    // Group by day, week, or month
    const trends = {};

    normalizedLogs.forEach((log) => {
      if (!log.date) {
        return;
      }
      let groupKey;
      const logDate = new Date(log.date);

      switch (groupBy) {
        case "week":
          const weekStart = new Date(logDate);
          weekStart.setDate(logDate.getDate() - logDate.getDay());
          groupKey = weekStart.toISOString().split("T")[0];
          break;
        case "month":
          groupKey = `${logDate.getFullYear()}-${(logDate.getMonth() + 1)
            .toString()
            .padStart(2, "0")}`;
          break;
        default: // day
          groupKey = log.date;
      }

      if (!trends[groupKey]) {
        trends[groupKey] = {
          date: groupKey,
          calories: 0,
          carbs: 0,
          fat: 0,
          protein: 0,
          entryCount: 0,
        };
      }

      trends[groupKey].calories += parseFloat(log.calories) || 0;
      trends[groupKey].carbs += parseFloat(log.carbs) || 0;
      trends[groupKey].fat += parseFloat(log.fat) || 0;
      trends[groupKey].protein += parseFloat(log.protein) || 0;
      trends[groupKey].entryCount += 1;
    });

    // Convert to array and round values
    const trendData = Object.values(trends).map((trend) => ({
      ...trend,
      calories: Math.round(trend.calories),
      carbs: Math.round(trend.carbs),
      fat: Math.round(trend.fat),
      protein: Math.round(trend.protein),
    }));

    res.json({
      success: true,
      data: {
        userId,
        startDate,
        endDate,
        groupBy,
        trends: trendData,
        totalDays: trendData.length,
      },
      message: "Calorie trends retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve calorie trends",
    });
  }
};

// Get comprehensive dashboard data
exports.getDashboardData = async (req, res) => {
  try {
    const { userId, date, year, month } = req.query;

    if (!userId) {
      return res.status(400).json({
        success: false,
        message: "userId is required",
      });
    }

    // Use current date if not provided
    const currentDate = date || new Date().toISOString().split("T")[0];
    const currentYear = year || new Date().getFullYear();
    const currentMonth = month || new Date().getMonth() + 1;

    // Get daily summary
    const dailyFilters = [
      { field: "userId", operator: "==", value: userId },
      { field: "date", operator: "==", value: currentDate },
    ];
    const dailyFoodLogs = await foodLogService.query(dailyFilters);

    // Get monthly summary
    const startDate = `${currentYear}-${currentMonth
      .toString()
      .padStart(2, "0")}-01`;
    const endDate = `${currentYear}-${currentMonth
      .toString()
      .padStart(2, "0")}-31`;
    const monthlyFilters = [
      { field: "userId", operator: "==", value: userId },
      { field: "date", operator: ">=", value: startDate },
      { field: "date", operator: "<=", value: endDate },
    ];
    const monthlyFoodLogs = await foodLogService.query(
      monthlyFilters,
      "date",
      "asc"
    );

    // Get recent activity (last 10 entries)
    const recentFilters = [{ field: "userId", operator: "==", value: userId }];
    const recentFoodLogs = await foodLogService.query(
      recentFilters,
      "createdAt",
      "desc",
      10
    );

    // Calculate daily totals
    const dailyTotals = {
      calories: 0,
      carbs: 0,
      fat: 0,
      protein: 0,
      mealDistribution: {
        Breakfast: 0,
        Lunch: 0,
        Dinner: 0,
        Snacks: 0,
      },
    };

    dailyFoodLogs.forEach((log) => {
      const calories = parseFloat(log.calories) || 0;
      const mealType = log.mealType || "Snacks";

      dailyTotals.calories += calories;
      dailyTotals.carbs += parseFloat(log.carbs) || 0;
      dailyTotals.fat += parseFloat(log.fat) || 0;
      dailyTotals.protein += parseFloat(log.protein) || 0;

      if (dailyTotals.mealDistribution.hasOwnProperty(mealType)) {
        dailyTotals.mealDistribution[mealType] += calories;
      } else {
        dailyTotals.mealDistribution.Snacks += calories;
      }
    });

    // Calculate monthly totals
    const monthlyTotals = {
      consumed: 0,
      averageDaily: 0,
    };

    const dailyBreakdown = {};
    monthlyFoodLogs.forEach((log) => {
      const calories = parseFloat(log.calories) || 0;
      const logDate = log.date;

      monthlyTotals.consumed += calories;

      if (!dailyBreakdown[logDate]) {
        dailyBreakdown[logDate] = 0;
      }
      dailyBreakdown[logDate] += calories;
    });

    const daysWithData = Object.keys(dailyBreakdown).length;
    monthlyTotals.averageDaily =
      daysWithData > 0 ? monthlyTotals.consumed / daysWithData : 0;

    // Format meal distribution for donut chart
    const mealDistribution = Object.entries(dailyTotals.mealDistribution)
      .filter(([_, calories]) => calories > 0)
      .map(([mealType, calories]) => ({
        mealType,
        calories: Math.round(calories),
        percentage:
          dailyTotals.calories > 0
            ? Math.round((calories / dailyTotals.calories) * 100)
            : 0,
      }));

    // Format recent activity
    const recentActivity = recentFoodLogs.map((log) => ({
      id: log.id,
      foodName: log.foodName,
      servingSize: log.servingSize,
      measuringUnit: log.measuringUnit,
      calories: Math.round(parseFloat(log.calories) || 0),
      mealType: log.mealType,
      date: log.date,
      createdAt: log.createdAt,
      time: new Date(log.createdAt).toLocaleTimeString("en-US", {
        hour: "2-digit",
        minute: "2-digit",
        hour12: true,
      }),
    }));

    // Calculate remaining calories (assuming a daily goal of 2000 kcal)
    const dailyGoal = 2000; // This could be user-configurable
    const remainingCalories = Math.max(0, dailyGoal - dailyTotals.calories);

    res.json({
      success: true,
      data: {
        userId,
        date: currentDate,
        year: parseInt(currentYear),
        month: parseInt(currentMonth),

        // Daily data
        daily: {
          totalCalories: Math.round(dailyTotals.calories),
          totalCarbs: Math.round(dailyTotals.carbs),
          totalFat: Math.round(dailyTotals.fat),
          totalProtein: Math.round(dailyTotals.protein),
          mealDistribution,
          remainingCalories: Math.round(remainingCalories),
          dailyGoal,
          entryCount: dailyFoodLogs.length,
        },

        // Monthly data
        monthly: {
          consumed: Math.round(monthlyTotals.consumed),
          averageDaily: Math.round(monthlyTotals.averageDaily),
          daysWithData,
          dailyBreakdown: Object.entries(dailyBreakdown).map(
            ([date, calories]) => ({
              date,
              calories: Math.round(calories),
            })
          ),
        },

        // Recent activity
        recentActivity: {
          entries: recentActivity,
          count: recentActivity.length,
        },

        // Summary for dashboard cards
        summary: {
          totalCaloricIntake: Math.round(dailyTotals.calories),
          consumed: Math.round(monthlyTotals.consumed),
          remaining: Math.round(remainingCalories),
          mealBreakdown: mealDistribution,
        },
      },
      message: "Dashboard data retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve dashboard data",
    });
  }
};
