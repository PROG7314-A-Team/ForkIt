const FirebaseService = require("../services/firebaseService");
const waterLogService = new FirebaseService("waterLogs");

// Get all water logs
exports.getWaterLogs = async (req, res) => {
  try {
    const { userId } = req.params; // Read userId from path parameters
    const { date } = req.query;    // Read date from query parameters
    let filters = [];
    
    if (userId) {
      filters.push({ field: "userId", operator: "==", value: userId });
    }
    
    if (date) {
      filters.push({ field: "date", operator: "==", value: date });
    }

    const waterLogs = await waterLogService.query(filters, "createdAt", "desc");
    
    res.json({
      success: true,
      data: waterLogs,
      message: "Water logs retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve water logs",
    });
  }
};

// Get water log by ID
exports.getWaterLogById = async (req, res) => {
  try {
    const { id } = req.params;
    const waterLog = await waterLogService.getById(id);

    if (!waterLog || !waterLog.exists) {
      return res.status(404).json({
        success: false,
        message: "Water log not found",
      });
    }

    res.json({
      success: true,
      data: { id: waterLog.id, ...waterLog.data() },
      message: "Water log retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve water log",
    });
  }
};

// Create new water log entry
exports.createWaterLog = async (req, res) => {
  try {
    const {
      userId,
      amount, // Amount in milliliters
      date
    } = req.body;

    // Validation
    if (!userId || !amount || !date) {
      return res.status(400).json({
        success: false,
        message: "userId, amount (in ml), and date are required",
      });
    }

    // Validate amount is a positive number
    const amountInMl = parseFloat(amount);
    if (isNaN(amountInMl) || amountInMl <= 0) {
      return res.status(400).json({
        success: false,
        message: "amount must be a positive number in milliliters",
      });
    }

    const waterLogData = {
      userId,
      amount: amountInMl,
      date
    };

    const waterLog = await waterLogService.create(waterLogData);

    res.status(201).json({
      success: true,
      data: waterLog,
      message: "Water log created successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to create water log",
    });
  }
};

// Update water log entry
exports.updateWaterLog = async (req, res) => {
  try {
    const { id } = req.params;
    const updateData = req.body;

    if (!id) {
      return res.status(400).json({
        success: false,
        message: "Water log ID is required",
      });
    }

    // Validate amount if being updated
    if (updateData.amount !== undefined) {
      const amountInMl = parseFloat(updateData.amount);
      if (isNaN(amountInMl) || amountInMl <= 0) {
        return res.status(400).json({
          success: false,
          message: "amount must be a positive number in milliliters",
        });
      }
      updateData.amount = amountInMl;
    }

    const waterLog = await waterLogService.update(id, updateData);

    res.json({
      success: true,
      data: waterLog,
      message: "Water log updated successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to update water log",
    });
  }
};

// Delete water log entry
exports.deleteWaterLog = async (req, res) => {
  try {
    const { id } = req.params;

    if (!id) {
      return res.status(400).json({
        success: false,
        message: "Water log ID is required",
      });
    }

    const deletedWaterLog = await waterLogService.delete(id);

    res.json({
      success: true,
      data: deletedWaterLog,
      message: "Water log deleted successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to delete water log",
    });
  }
};

// Get water logs by user and date range
exports.getWaterLogsByDateRange = async (req, res) => {
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
      { field: "date", operator: "<=", value: endDate }
    ];

    const waterLogs = await waterLogService.query(filters, "date", "asc");

    res.json({
      success: true,
      data: waterLogs,
      message: "Water logs retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve water logs",
    });
  }
};

// Get daily water total for a user
exports.getDailyWaterTotal = async (req, res) => {
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
      { field: "date", operator: "==", value: date }
    ];

    const waterLogs = await waterLogService.query(filters);

    const totalAmount = waterLogs.reduce((sum, log) => sum + log.amount, 0);

    res.json({
      success: true,
      data: {
        userId,
        date,
        totalAmount,
        entries: waterLogs.length
      },
      message: "Daily water total retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve daily water total",
    });
  }
};

// Get daily water summary for dashboard
exports.getDailyWaterSummary = async (req, res) => {
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
      { field: "date", operator: "==", value: date }
    ];

    const waterLogs = await waterLogService.query(filters);

    const totalAmount = waterLogs.reduce((sum, log) => sum + log.amount, 0);
    const averagePerEntry = waterLogs.length > 0 ? totalAmount / waterLogs.length : 0;

    // Calculate hourly distribution
    const hourlyDistribution = {};
    waterLogs.forEach(log => {
      const hour = new Date(log.createdAt).getHours();
      if (!hourlyDistribution[hour]) {
        hourlyDistribution[hour] = { amount: 0, count: 0 };
      }
      hourlyDistribution[hour].amount += log.amount;
      hourlyDistribution[hour].count += 1;
    });

    // Convert to array and sort by hour
    const hourlyData = Object.entries(hourlyDistribution)
      .map(([hour, data]) => ({
        hour: parseInt(hour),
        amount: Math.round(data.amount),
        count: data.count,
        average: Math.round(data.amount / data.count)
      }))
      .sort((a, b) => a.hour - b.hour);

    res.json({
      success: true,
      data: {
        userId,
        date,
        totalAmount: Math.round(totalAmount),
        totalEntries: waterLogs.length,
        averagePerEntry: Math.round(averagePerEntry),
        hourlyDistribution: hourlyData,
        goal: 2000, // Default daily goal in ml
        remaining: Math.max(0, 2000 - totalAmount),
        goalPercentage: Math.round((totalAmount / 2000) * 100)
      },
      message: "Daily water summary retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve daily water summary",
    });
  }
};

// Get monthly water summary for dashboard
exports.getMonthlyWaterSummary = async (req, res) => {
  try {
    const { userId, year, month } = req.query;

    if (!userId || !year || !month) {
      return res.status(400).json({
        success: false,
        message: "userId, year, and month are required",
      });
    }

    // Create date range for the month
    const startDate = `${year}-${month.toString().padStart(2, '0')}-01`;
    const endDate = `${year}-${month.toString().padStart(2, '0')}-31`;

    const filters = [
      { field: "userId", operator: "==", value: userId },
      { field: "date", operator: ">=", value: startDate },
      { field: "date", operator: "<=", value: endDate }
    ];

    const waterLogs = await waterLogService.query(filters, "date", "asc");

    // Calculate daily totals
    const dailyTotals = {};
    let monthlyTotalAmount = 0;
    let totalEntries = 0;

    waterLogs.forEach(log => {
      const date = log.date;
      const amount = parseFloat(log.amount) || 0;
      
      if (!dailyTotals[date]) {
        dailyTotals[date] = {
          amount: 0,
          entries: 0
        };
      }

      dailyTotals[date].amount += amount;
      dailyTotals[date].entries += 1;

      monthlyTotalAmount += amount;
      totalEntries += 1;
    });

    // Calculate average daily water intake
    const daysWithData = Object.keys(dailyTotals).length;
    const averageDailyAmount = daysWithData > 0 ? monthlyTotalAmount / daysWithData : 0;

    // Calculate goal achievement
    const dailyGoal = 2000; // ml per day
    const totalGoal = dailyGoal * daysWithData;
    const goalAchievement = totalGoal > 0 ? (monthlyTotalAmount / totalGoal) * 100 : 0;

    res.json({
      success: true,
      data: {
        userId,
        year: parseInt(year),
        month: parseInt(month),
        totalAmount: Math.round(monthlyTotalAmount),
        totalEntries,
        averageDailyAmount: Math.round(averageDailyAmount),
        daysWithData,
        dailyGoal,
        goalAchievement: Math.round(goalAchievement),
        dailyTotals: Object.entries(dailyTotals).map(([date, data]) => ({
          date,
          amount: Math.round(data.amount),
          entries: data.entries,
          goalMet: data.amount >= dailyGoal
        }))
      },
      message: "Monthly water summary retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve monthly water summary",
    });
  }
};

// Get recent water activity for dashboard
exports.getRecentWaterActivity = async (req, res) => {
  try {
    const { userId, limit = 10 } = req.query;

    if (!userId) {
      return res.status(400).json({
        success: false,
        message: "userId is required",
      });
    }

    const filters = [
      { field: "userId", operator: "==", value: userId }
    ];

    const waterLogs = await waterLogService.query(
      filters, 
      "createdAt", 
      "desc", 
      parseInt(limit)
    );

    // Format for recent activity display
    const recentActivity = waterLogs.map(log => ({
      id: log.id,
      amount: Math.round(parseFloat(log.amount) || 0),
      date: log.date,
      createdAt: log.createdAt,
      time: new Date(log.createdAt).toLocaleTimeString('en-US', { 
        hour: '2-digit', 
        minute: '2-digit',
        hour12: true 
      })
    }));

    res.json({
      success: true,
      data: {
        userId,
        recentActivity,
        count: recentActivity.length
      },
      message: "Recent water activity retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve recent water activity",
    });
  }
};

// Get water trends over time
exports.getWaterTrends = async (req, res) => {
  try {
    const { userId, startDate, endDate, groupBy = 'day' } = req.query;

    if (!userId || !startDate || !endDate) {
      return res.status(400).json({
        success: false,
        message: "userId, startDate, and endDate are required",
      });
    }

    const filters = [
      { field: "userId", operator: "==", value: userId },
      { field: "date", operator: ">=", value: startDate },
      { field: "date", operator: "<=", value: endDate }
    ];

    const waterLogs = await waterLogService.query(filters, "date", "asc");

    // Group by day, week, or month
    const trends = {};
    
    waterLogs.forEach(log => {
      let groupKey;
      const logDate = new Date(log.date);
      
      switch (groupBy) {
        case 'week':
          const weekStart = new Date(logDate);
          weekStart.setDate(logDate.getDate() - logDate.getDay());
          groupKey = weekStart.toISOString().split('T')[0];
          break;
        case 'month':
          groupKey = `${logDate.getFullYear()}-${(logDate.getMonth() + 1).toString().padStart(2, '0')}`;
          break;
        default: // day
          groupKey = log.date;
      }

      if (!trends[groupKey]) {
        trends[groupKey] = {
          date: groupKey,
          amount: 0,
          entries: 0
        };
      }

      trends[groupKey].amount += parseFloat(log.amount) || 0;
      trends[groupKey].entries += 1;
    });

    // Convert to array and round values
    const trendData = Object.values(trends).map(trend => ({
      ...trend,
      amount: Math.round(trend.amount)
    }));

    res.json({
      success: true,
      data: {
        userId,
        startDate,
        endDate,
        groupBy,
        trends: trendData,
        totalDays: trendData.length
      },
      message: "Water trends retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve water trends",
    });
  }
};

// Get comprehensive water dashboard data
exports.getWaterDashboardData = async (req, res) => {
  try {
    const { userId, date, year, month } = req.query;

    if (!userId) {
      return res.status(400).json({
        success: false,
        message: "userId is required",
      });
    }

    // Use current date if not provided
    const currentDate = date || new Date().toISOString().split('T')[0];
    const currentYear = year || new Date().getFullYear();
    const currentMonth = month || new Date().getMonth() + 1;

    // Get daily summary
    const dailyFilters = [
      { field: "userId", operator: "==", value: userId },
      { field: "date", operator: "==", value: currentDate }
    ];
    const dailyWaterLogs = await waterLogService.query(dailyFilters);

    // Get monthly summary
    const startDate = `${currentYear}-${currentMonth.toString().padStart(2, '0')}-01`;
    const endDate = `${currentYear}-${currentMonth.toString().padStart(2, '0')}-31`;
    const monthlyFilters = [
      { field: "userId", operator: "==", value: userId },
      { field: "date", operator: ">=", value: startDate },
      { field: "date", operator: "<=", value: endDate }
    ];
    const monthlyWaterLogs = await waterLogService.query(monthlyFilters, "date", "asc");

    // Get recent activity (last 10 entries)
    const recentFilters = [{ field: "userId", operator: "==", value: userId }];
    const recentWaterLogs = await waterLogService.query(
      recentFilters, 
      "createdAt", 
      "desc", 
      10
    );

    // Calculate daily totals
    const dailyTotal = dailyWaterLogs.reduce((sum, log) => sum + (parseFloat(log.amount) || 0), 0);
    const dailyGoal = 2000; // ml
    const dailyRemaining = Math.max(0, dailyGoal - dailyTotal);

    // Calculate monthly totals
    const monthlyTotal = monthlyWaterLogs.reduce((sum, log) => sum + (parseFloat(log.amount) || 0), 0);
    const daysWithData = [...new Set(monthlyWaterLogs.map(log => log.date))].length;
    const averageDaily = daysWithData > 0 ? monthlyTotal / daysWithData : 0;

    // Format recent activity
    const recentActivity = recentWaterLogs.map(log => ({
      id: log.id,
      amount: Math.round(parseFloat(log.amount) || 0),
      date: log.date,
      createdAt: log.createdAt,
      time: new Date(log.createdAt).toLocaleTimeString('en-US', { 
        hour: '2-digit', 
        minute: '2-digit',
        hour12: true 
      })
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
          totalAmount: Math.round(dailyTotal),
          totalEntries: dailyWaterLogs.length,
          dailyGoal,
          remaining: Math.round(dailyRemaining),
          goalPercentage: Math.round((dailyTotal / dailyGoal) * 100),
          averagePerEntry: dailyWaterLogs.length > 0 ? Math.round(dailyTotal / dailyWaterLogs.length) : 0
        },

        // Monthly data
        monthly: {
          totalAmount: Math.round(monthlyTotal),
          averageDaily: Math.round(averageDaily),
          daysWithData,
          totalEntries: monthlyWaterLogs.length,
          goalAchievement: Math.round((monthlyTotal / (dailyGoal * daysWithData)) * 100)
        },

        // Recent activity
        recentActivity: {
          entries: recentActivity,
          count: recentActivity.length
        },

        // Summary for dashboard cards
        summary: {
          totalWaterIntake: Math.round(dailyTotal),
          monthlyTotal: Math.round(monthlyTotal),
          remaining: Math.round(dailyRemaining),
          goalMet: dailyTotal >= dailyGoal
        }
      },
      message: "Water dashboard data retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve water dashboard data",
    });
  }
};