const FirebaseService = require("../services/firebaseService");
const StreakService = require("../services/streakService");
const mealLogService = new FirebaseService("mealLogs");
const streakService = new StreakService();

// Get all meal logs
exports.getMealLogs = async (req, res) => {
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

    const mealLogs = await mealLogService.query(filters, "createdAt", "desc");

    res.json({
      success: true,
      data: mealLogs,
      message: "Meal logs retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve meal logs",
    });
  }
};

// Get meal log by ID
exports.getMealLogById = async (req, res) => {
  try {
    const { id } = req.params;
    const mealLog = await mealLogService.getById(id);

    if (!mealLog || !mealLog.exists) {
      return res.status(404).json({
        success: false,
        message: "Meal log not found",
      });
    }

    res.json({
      success: true,
      data: { id: mealLog.id, ...mealLog.data() },
      message: "Meal log retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve meal log",
    });
  }
};

// Create new meal log entry
exports.createMealLog = async (req, res) => {
  try {
    const {
      userId,
      name,
      description,
      ingredients, // Array of ingredients with quantities
      instructions, // Array of cooking instructions
      totalCalories,
      totalCarbs,
      totalFat,
      totalProtein,
      servings,
      date,
      mealType, // Breakfast, Lunch, Dinner, Snacks
      isTemplate, // Boolean: true for meal templates, false for logged meals
      templateId, // String: reference to original template if this is a logged meal
    } = req.body;

    // Validation
    if (!userId || !name || !ingredients || !instructions) {
      return res.status(400).json({
        success: false,
        message:
          "userId, name, ingredients, and instructions are required",
      });
    }
    
    // For templates, date can be empty. For logged meals, date is required.
    if (!isTemplate && !date) {
      return res.status(400).json({
        success: false,
        message: "date is required for logged meals",
      });
    }

    // Validate ingredients array
    if (!Array.isArray(ingredients) || ingredients.length === 0) {
      return res.status(400).json({
        success: false,
        message: "ingredients must be a non-empty array",
      });
    }

    // Validate instructions array
    if (!Array.isArray(instructions) || instructions.length === 0) {
      return res.status(400).json({
        success: false,
        message: "instructions must be a non-empty array",
      });
    }

    const mealLogData = {
      userId,
      name,
      description: description || "",
      ingredients,
      instructions,
      totalCalories: parseFloat(totalCalories) || 0,
      totalCarbs: parseFloat(totalCarbs) || 0,
      totalFat: parseFloat(totalFat) || 0,
      totalProtein: parseFloat(totalProtein) || 0,
      servings: parseFloat(servings) || 1,
      date: date || "", // Empty string for templates
      mealType: mealType || null,
      isTemplate: Boolean(isTemplate) || false,
      templateId: templateId || null,
    };

    const mealLog = await mealLogService.create(mealLogData);

    // Only update streak for logged meals (not templates)
    if (!isTemplate && date) {
      await streakService.updateUserStreak(userId, date);
    }

    res.status(201).json({
      success: true,
      data: mealLog,
      message: "Meal log created successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to create meal log",
    });
  }
};

// Update meal log entry
exports.updateMealLog = async (req, res) => {
  try {
    const { id } = req.params;
    const updateData = req.body;

    if (!id) {
      return res.status(400).json({
        success: false,
        message: "Meal log ID is required",
      });
    }

    const mealLog = await mealLogService.update(id, updateData);

    res.json({
      success: true,
      data: mealLog,
      message: "Meal log updated successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to update meal log",
    });
  }
};

// Delete meal log entry
exports.deleteMealLog = async (req, res) => {
  try {
    const { id } = req.params;

    if (!id) {
      return res.status(400).json({
        success: false,
        message: "Meal log ID is required",
      });
    }

    const deletedMealLog = await mealLogService.delete(id);

    res.json({
      success: true,
      data: deletedMealLog,
      message: "Meal log deleted successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to delete meal log",
    });
  }
};

// Get meal logs by user and date range
exports.getMealLogsByDateRange = async (req, res) => {
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

    const mealLogs = await mealLogService.query(filters, "date", "asc");

    res.json({
      success: true,
      data: mealLogs,
      message: "Meal logs retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve meal logs",
    });
  }
};

// Get meal templates (isTemplate = true)
exports.getMealTemplates = async (req, res) => {
  try {
    const { userId } = req.params;

    if (!userId) {
      return res.status(400).json({
        success: false,
        message: "userId is required",
      });
    }

    const filters = [
      { field: "userId", operator: "==", value: userId },
      { field: "isTemplate", operator: "==", value: true },
    ];

    const templates = await mealLogService.query(filters, "createdAt", "desc");

    res.json({
      success: true,
      data: templates,
      message: "Meal templates retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve meal templates",
    });
  }
};

// Get logged meals (isTemplate = false) for a specific date
exports.getLoggedMeals = async (req, res) => {
  try {
    const { userId } = req.params;
    const { date } = req.query;

    if (!userId) {
      return res.status(400).json({
        success: false,
        message: "userId is required",
      });
    }

    const filters = [
      { field: "userId", operator: "==", value: userId },
      { field: "isTemplate", operator: "==", value: false },
    ];

    if (date) {
      filters.push({ field: "date", operator: "==", value: date });
    }

    const loggedMeals = await mealLogService.query(filters, "createdAt", "desc");

    res.json({
      success: true,
      data: loggedMeals,
      message: "Logged meals retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve logged meals",
    });
  }
};
