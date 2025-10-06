const FirebaseService = require("../services/firebaseService");
const userService = new FirebaseService("users");

// GET user goals
exports.getUserGoals = async (req, res) => {
  try {
    const { id } = req.params;

    if (!id) {
      return res.status(400).json({
        success: false,
        message: "User ID is required",
      });
    }

    // Query user by userId field
    const users = await userService.query([
      { field: "userId", operator: "==", value: id },
    ]);

    if (users.length === 0) {
      return res.status(404).json({
        success: false,
        message: "User not found",
      });
    }

    const user = users[0];
    
    // Return goals or default values if not set
    const goals = user.goals || {
      dailyCalories: 2000,
      dailyWater: 2000,
      dailySteps: 8000,
      weeklyExercises: 3,
    };

    res.json({
      success: true,
      data: {
        userId: id,
        ...goals,
        updatedAt: user.goalsUpdatedAt || user.createdAt,
      },
      message: "User goals retrieved successfully",
    });
  } catch (error) {
    console.error("Error retrieving user goals:", error);
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve user goals",
    });
  }
};

// UPDATE user goals
exports.updateUserGoals = async (req, res) => {
  try {
    const { id } = req.params;
    const goalsData = req.body;

    if (!id) {
      return res.status(400).json({
        success: false,
        message: "User ID is required",
      });
    }

    if (!goalsData || Object.keys(goalsData).length === 0) {
      return res.status(400).json({
        success: false,
        message: "Goals data is required",
      });
    }

    // Validate goal values
    const validations = {
      dailyCalories: { min: 1200, max: 10000, name: "Daily Calories" },
      dailyWater: { min: 500, max: 10000, name: "Daily Water (ml)" },
      dailySteps: { min: 0, max: 50000, name: "Daily Steps" },
      weeklyExercises: { min: 0, max: 21, name: "Weekly Exercises" },
    };

    // Validate each provided goal
    for (const [key, value] of Object.entries(goalsData)) {
      if (validations[key]) {
        const { min, max, name } = validations[key];
        if (typeof value !== 'number' || value < min || value > max) {
          return res.status(400).json({
            success: false,
            message: `${name} must be a number between ${min} and ${max}`,
          });
        }
      }
    }

    // Query user to get document ID
    const users = await userService.query([
      { field: "userId", operator: "==", value: id },
    ]);

    if (users.length === 0) {
      return res.status(404).json({
        success: false,
        message: "User not found",
      });
    }

    const user = users[0];
    const currentGoals = user.goals || {};

    // Merge new goals with existing ones
    const updatedGoals = {
      ...currentGoals,
      ...goalsData,
    };

    // Update user document with new goals
    const updateData = {
      goals: updatedGoals,
      goalsUpdatedAt: new Date().toISOString(),
    };

    // Use the document ID (which is the same as userId) to update
    const updatedUser = await userService.update(id, updateData);

    if (updatedUser) {
      res.json({
        success: true,
        data: {
          userId: id,
          ...updatedGoals,
          updatedAt: updateData.goalsUpdatedAt,
        },
        message: "User goals updated successfully",
      });
    } else {
      res.status(400).json({
        success: false,
        message: "Failed to update user goals",
      });
    }
  } catch (error) {
    console.error("Error updating user goals:", error);
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to update user goals",
    });
  }
};

