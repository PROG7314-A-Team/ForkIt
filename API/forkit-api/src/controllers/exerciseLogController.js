const FirebaseService = require("../services/firebaseService");
const exerciseLogService = new FirebaseService("exerciseLogs");

// Get all exercise logs
exports.getExerciseLogs = async (req, res) => {
  try {
    console.log("Get exercise logs query", req.query);
    const { userId, date, type } = req.query;
    let filters = [];

    if (userId) {
      filters.push({ field: "userId", operator: "==", value: userId });
    }

    if (date) {
      filters.push({ field: "date", operator: "==", value: date });
    }

    if (type) {
      filters.push({ field: "type", operator: "==", value: type });
    }

    console.log("Filters for exercise logs search", filters);

    const exerciseLogs = await exerciseLogService.query(
      filters,
      "createdAt",
      "desc"
    );

    res.json({
      success: true,
      data: exerciseLogs,
      message: "Exercise logs retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve exercise logs",
    });
  }
};

// Get exercise log by ID
exports.getExerciseLogById = async (req, res) => {
  try {
    const { id } = req.params;
    const exerciseLog = await exerciseLogService.getById(id);

    if (!exerciseLog || !exerciseLog.exists) {
      return res.status(404).json({
        success: false,
        message: "Exercise log not found",
      });
    }

    res.json({
      success: true,
      data: { id: exerciseLog.id, ...exerciseLog.data() },
      message: "Exercise log retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve exercise log",
    });
  }
};

// Create new exercise log entry
exports.createExerciseLog = async (req, res) => {
  try {
    const {
      userId,
      name,
      date,
      caloriesBurnt,
      type, // Cardio or Strength
      duration, // Optional: duration in minutes
      notes, // Optional: additional notes
    } = req.body;

    // Validation
    if (!userId || !name || !date || !caloriesBurnt || !type) {
      return res.status(400).json({
        success: false,
        message: "userId, name, date, caloriesBurnt, and type are required",
      });
    }

    // Validate type is either Cardio or Strength
    if (!["Cardio", "Strength"].includes(type)) {
      return res.status(400).json({
        success: false,
        message: "type must be either 'Cardio' or 'Strength'",
      });
    }

    // Validate caloriesBurnt is a positive number
    const caloriesBurntNum = parseFloat(caloriesBurnt);
    if (isNaN(caloriesBurntNum) || caloriesBurntNum <= 0) {
      return res.status(400).json({
        success: false,
        message: "caloriesBurnt must be a positive number",
      });
    }

    const exerciseLogData = {
      userId,
      name,
      date,
      caloriesBurnt: caloriesBurntNum,
      type,
      duration: duration ? parseFloat(duration) : null,
      notes: notes || "",
    };

    const exerciseLog = await exerciseLogService.create(exerciseLogData);

    res.status(201).json({
      success: true,
      data: exerciseLog,
      message: "Exercise log created successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to create exercise log",
    });
  }
};

// Update exercise log entry
exports.updateExerciseLog = async (req, res) => {
  try {
    const { id } = req.params;
    const updateData = req.body;

    if (!id) {
      return res.status(400).json({
        success: false,
        message: "Exercise log ID is required",
      });
    }

    // Validate type if being updated
    if (updateData.type && !["Cardio", "Strength"].includes(updateData.type)) {
      return res.status(400).json({
        success: false,
        message: "type must be either 'Cardio' or 'Strength'",
      });
    }

    // Validate caloriesBurnt if being updated
    if (updateData.caloriesBurnt !== undefined) {
      const caloriesBurntNum = parseFloat(updateData.caloriesBurnt);
      if (isNaN(caloriesBurntNum) || caloriesBurntNum <= 0) {
        return res.status(400).json({
          success: false,
          message: "caloriesBurnt must be a positive number",
        });
      }
      updateData.caloriesBurnt = caloriesBurntNum;
    }

    // Validate duration if being updated
    if (updateData.duration !== undefined && updateData.duration !== null) {
      const durationNum = parseFloat(updateData.duration);
      if (isNaN(durationNum) || durationNum <= 0) {
        return res.status(400).json({
          success: false,
          message: "duration must be a positive number",
        });
      }
      updateData.duration = durationNum;
    }

    const exerciseLog = await exerciseLogService.update(id, updateData);

    res.json({
      success: true,
      data: exerciseLog,
      message: "Exercise log updated successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to update exercise log",
    });
  }
};

// Delete exercise log entry
exports.deleteExerciseLog = async (req, res) => {
  try {
    const { id } = req.params;

    if (!id) {
      return res.status(400).json({
        success: false,
        message: "Exercise log ID is required",
      });
    }

    const deletedExerciseLog = await exerciseLogService.delete(id);

    res.json({
      success: true,
      data: deletedExerciseLog,
      message: "Exercise log deleted successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to delete exercise log",
    });
  }
};

// Get exercise logs by user and date range
exports.getExerciseLogsByDateRange = async (req, res) => {
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

    const exerciseLogs = await exerciseLogService.query(filters, "date", "asc");

    res.json({
      success: true,
      data: exerciseLogs,
      message: "Exercise logs retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve exercise logs",
    });
  }
};

// Get daily exercise total for a user
exports.getDailyExerciseTotal = async (req, res) => {
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

    const exerciseLogs = await exerciseLogService.query(filters);

    const totalCaloriesBurnt = exerciseLogs.reduce(
      (sum, log) => sum + log.caloriesBurnt,
      0
    );
    const totalDuration = exerciseLogs.reduce(
      (sum, log) => sum + (log.duration || 0),
      0
    );

    // Count by type
    const cardioCount = exerciseLogs.filter(
      (log) => log.type === "Cardio"
    ).length;
    const strengthCount = exerciseLogs.filter(
      (log) => log.type === "Strength"
    ).length;

    res.json({
      success: true,
      data: {
        userId,
        date,
        totalCaloriesBurnt,
        totalDuration,
        totalExercises: exerciseLogs.length,
        cardioExercises: cardioCount,
        strengthExercises: strengthCount,
        entries: exerciseLogs,
      },
      message: "Daily exercise total retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve daily exercise total",
    });
  }
};

// Get daily exercise summary for dashboard
exports.getDailyExerciseSummary = async (req, res) => {
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

    const exerciseLogs = await exerciseLogService.query(filters);

    const totalCaloriesBurnt = exerciseLogs.reduce(
      (sum, log) => sum + log.caloriesBurnt,
      0
    );
    const totalDuration = exerciseLogs.reduce(
      (sum, log) => sum + (log.duration || 0),
      0
    );

    // Calculate by exercise type
    const typeBreakdown = {
      Cardio: { calories: 0, duration: 0, count: 0 },
      Strength: { calories: 0, duration: 0, count: 0 },
    };

    exerciseLogs.forEach((log) => {
      const type = log.type || "Cardio";
      if (typeBreakdown[type]) {
        typeBreakdown[type].calories += log.caloriesBurnt;
        typeBreakdown[type].duration += log.duration || 0;
        typeBreakdown[type].count += 1;
      }
    });

    // Calculate hourly distribution
    const hourlyDistribution = {};
    exerciseLogs.forEach((log) => {
      const hour = new Date(log.createdAt).getHours();
      if (!hourlyDistribution[hour]) {
        hourlyDistribution[hour] = { calories: 0, duration: 0, count: 0 };
      }
      hourlyDistribution[hour].calories += log.caloriesBurnt;
      hourlyDistribution[hour].duration += log.duration || 0;
      hourlyDistribution[hour].count += 1;
    });

    // Convert to array and sort by hour
    const hourlyData = Object.entries(hourlyDistribution)
      .map(([hour, data]) => ({
        hour: parseInt(hour),
        calories: Math.round(data.calories),
        duration: Math.round(data.duration),
        count: data.count,
      }))
      .sort((a, b) => a.hour - b.hour);

    res.json({
      success: true,
      data: {
        userId,
        date,
        totalCaloriesBurnt: Math.round(totalCaloriesBurnt),
        totalDuration: Math.round(totalDuration),
        totalExercises: exerciseLogs.length,
        typeBreakdown: Object.entries(typeBreakdown).map(([type, data]) => ({
          type,
          calories: Math.round(data.calories),
          duration: Math.round(data.duration),
          count: data.count,
          percentage:
            totalCaloriesBurnt > 0
              ? Math.round((data.calories / totalCaloriesBurnt) * 100)
              : 0,
        })),
        hourlyDistribution: hourlyData,
        averageCaloriesPerExercise:
          exerciseLogs.length > 0
            ? Math.round(totalCaloriesBurnt / exerciseLogs.length)
            : 0,
        averageDurationPerExercise:
          exerciseLogs.length > 0
            ? Math.round(totalDuration / exerciseLogs.length)
            : 0,
      },
      message: "Daily exercise summary retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve daily exercise summary",
    });
  }
};

// Get monthly exercise summary for dashboard
exports.getMonthlyExerciseSummary = async (req, res) => {
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

    const exerciseLogs = await exerciseLogService.query(filters, "date", "asc");

    // Calculate daily totals
    const dailyTotals = {};
    let monthlyTotalCalories = 0;
    let monthlyTotalDuration = 0;
    let totalExercises = 0;

    exerciseLogs.forEach((log) => {
      const date = log.date;
      const calories = parseFloat(log.caloriesBurnt) || 0;
      const duration = parseFloat(log.duration) || 0;

      if (!dailyTotals[date]) {
        dailyTotals[date] = {
          calories: 0,
          duration: 0,
          exercises: 0,
          cardioCount: 0,
          strengthCount: 0,
        };
      }

      dailyTotals[date].calories += calories;
      dailyTotals[date].duration += duration;
      dailyTotals[date].exercises += 1;

      if (log.type === "Cardio") {
        dailyTotals[date].cardioCount += 1;
      } else if (log.type === "Strength") {
        dailyTotals[date].strengthCount += 1;
      }

      monthlyTotalCalories += calories;
      monthlyTotalDuration += duration;
      totalExercises += 1;
    });

    // Calculate averages
    const daysWithData = Object.keys(dailyTotals).length;
    const averageDailyCalories =
      daysWithData > 0 ? monthlyTotalCalories / daysWithData : 0;
    const averageDailyDuration =
      daysWithData > 0 ? monthlyTotalDuration / daysWithData : 0;

    // Calculate type breakdown for the month
    const monthlyTypeBreakdown = {
      Cardio: { calories: 0, duration: 0, count: 0 },
      Strength: { calories: 0, duration: 0, count: 0 },
    };

    exerciseLogs.forEach((log) => {
      const type = log.type || "Cardio";
      if (monthlyTypeBreakdown[type]) {
        monthlyTypeBreakdown[type].calories += log.caloriesBurnt;
        monthlyTypeBreakdown[type].duration += log.duration || 0;
        monthlyTypeBreakdown[type].count += 1;
      }
    });

    res.json({
      success: true,
      data: {
        userId,
        year: parseInt(year),
        month: parseInt(month),
        totalCalories: Math.round(monthlyTotalCalories),
        totalDuration: Math.round(monthlyTotalDuration),
        totalExercises,
        averageDailyCalories: Math.round(averageDailyCalories),
        averageDailyDuration: Math.round(averageDailyDuration),
        daysWithData,
        monthlyTypeBreakdown: Object.entries(monthlyTypeBreakdown).map(
          ([type, data]) => ({
            type,
            calories: Math.round(data.calories),
            duration: Math.round(data.duration),
            count: data.count,
            percentage:
              monthlyTotalCalories > 0
                ? Math.round((data.calories / monthlyTotalCalories) * 100)
                : 0,
          })
        ),
        dailyTotals: Object.entries(dailyTotals).map(([date, data]) => ({
          date,
          calories: Math.round(data.calories),
          duration: Math.round(data.duration),
          exercises: data.exercises,
          cardioCount: data.cardioCount,
          strengthCount: data.strengthCount,
        })),
      },
      message: "Monthly exercise summary retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve monthly exercise summary",
    });
  }
};

// Get recent exercise activity for dashboard
exports.getRecentExerciseActivity = async (req, res) => {
  try {
    const { userId, limit = 10 } = req.query;

    if (!userId) {
      return res.status(400).json({
        success: false,
        message: "userId is required",
      });
    }

    const filters = [{ field: "userId", operator: "==", value: userId }];

    const exerciseLogs = await exerciseLogService.query(
      filters,
      "createdAt",
      "desc",
      parseInt(limit)
    );

    // Format for recent activity display
    const recentActivity = exerciseLogs.map((log) => ({
      id: log.id,
      name: log.name,
      type: log.type,
      caloriesBurnt: Math.round(parseFloat(log.caloriesBurnt) || 0),
      duration: log.duration ? Math.round(parseFloat(log.duration)) : null,
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
      message: "Recent exercise activity retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve recent exercise activity",
    });
  }
};

// Get exercise trends over time
exports.getExerciseTrends = async (req, res) => {
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

    const exerciseLogs = await exerciseLogService.query(filters, "date", "asc");

    // Group by day, week, or month
    const trends = {};

    exerciseLogs.forEach((log) => {
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
          duration: 0,
          exercises: 0,
          cardioCount: 0,
          strengthCount: 0,
        };
      }

      trends[groupKey].calories += parseFloat(log.caloriesBurnt) || 0;
      trends[groupKey].duration += parseFloat(log.duration) || 0;
      trends[groupKey].exercises += 1;

      if (log.type === "Cardio") {
        trends[groupKey].cardioCount += 1;
      } else if (log.type === "Strength") {
        trends[groupKey].strengthCount += 1;
      }
    });

    // Convert to array and round values
    const trendData = Object.values(trends).map((trend) => ({
      ...trend,
      calories: Math.round(trend.calories),
      duration: Math.round(trend.duration),
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
      message: "Exercise trends retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve exercise trends",
    });
  }
};

// Get comprehensive exercise dashboard data
exports.getExerciseDashboardData = async (req, res) => {
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
    const dailyExerciseLogs = await exerciseLogService.query(dailyFilters);

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
    const monthlyExerciseLogs = await exerciseLogService.query(
      monthlyFilters,
      "date",
      "asc"
    );

    // Get recent activity (last 10 entries)
    const recentFilters = [{ field: "userId", operator: "==", value: userId }];
    const recentExerciseLogs = await exerciseLogService.query(
      recentFilters,
      "createdAt",
      "desc",
      10
    );

    // Calculate daily totals
    const dailyTotalCalories = dailyExerciseLogs.reduce(
      (sum, log) => sum + (parseFloat(log.caloriesBurnt) || 0),
      0
    );
    const dailyTotalDuration = dailyExerciseLogs.reduce(
      (sum, log) => sum + (parseFloat(log.duration) || 0),
      0
    );
    const dailyCardioCount = dailyExerciseLogs.filter(
      (log) => log.type === "Cardio"
    ).length;
    const dailyStrengthCount = dailyExerciseLogs.filter(
      (log) => log.type === "Strength"
    ).length;

    // Calculate monthly totals
    const monthlyTotalCalories = monthlyExerciseLogs.reduce(
      (sum, log) => sum + (parseFloat(log.caloriesBurnt) || 0),
      0
    );
    const monthlyTotalDuration = monthlyExerciseLogs.reduce(
      (sum, log) => sum + (parseFloat(log.duration) || 0),
      0
    );
    const daysWithData = [
      ...new Set(monthlyExerciseLogs.map((log) => log.date)),
    ].length;
    const averageDailyCalories =
      daysWithData > 0 ? monthlyTotalCalories / daysWithData : 0;

    // Format recent activity
    const recentActivity = recentExerciseLogs.map((log) => ({
      id: log.id,
      name: log.name,
      type: log.type,
      caloriesBurnt: Math.round(parseFloat(log.caloriesBurnt) || 0),
      duration: log.duration ? Math.round(parseFloat(log.duration)) : null,
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
        date: currentDate,
        year: parseInt(currentYear),
        month: parseInt(currentMonth),

        // Daily data
        daily: {
          totalCalories: Math.round(dailyTotalCalories),
          totalDuration: Math.round(dailyTotalDuration),
          totalExercises: dailyExerciseLogs.length,
          cardioExercises: dailyCardioCount,
          strengthExercises: dailyStrengthCount,
          averageCaloriesPerExercise:
            dailyExerciseLogs.length > 0
              ? Math.round(dailyTotalCalories / dailyExerciseLogs.length)
              : 0,
        },

        // Monthly data
        monthly: {
          totalCalories: Math.round(monthlyTotalCalories),
          totalDuration: Math.round(monthlyTotalDuration),
          averageDailyCalories: Math.round(averageDailyCalories),
          daysWithData,
          totalExercises: monthlyExerciseLogs.length,
        },

        // Recent activity
        recentActivity: {
          entries: recentActivity,
          count: recentActivity.length,
        },

        // Summary for dashboard cards
        summary: {
          totalCaloriesBurnt: Math.round(dailyTotalCalories),
          monthlyTotal: Math.round(monthlyTotalCalories),
          totalExercises: dailyExerciseLogs.length,
          cardioVsStrength: {
            cardio: dailyCardioCount,
            strength: dailyStrengthCount,
          },
        },
      },
      message: "Exercise dashboard data retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve exercise dashboard data",
    });
  }
};
