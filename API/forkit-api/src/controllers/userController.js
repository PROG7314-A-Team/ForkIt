const { auth } = require("../config/firebase"); // Firebase Admin
const FirebaseService = require("../services/firebaseService");
const StreakService = require("../services/streakService");
const streakService = new StreakService();
const userService = new FirebaseService("users");

// GET all users (Firestore)
exports.getUser = async (req, res) => {
  try {
    let users = await userService.getAll();

    if (!users) {
      return res.status(404).json({
        success: false,
        message: "Users not found",
      });
    }

    res.json({
      success: true,
      data: users,
      message: "Users retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve users",
    });
  }
};

// GET user by ID (Firestore)
exports.getUserById = async (req, res) => {
  try {
    const { id } = req.params;

    // Use query method to search by userId field
    const users = await userService.query([
      { field: "userId", operator: "==", value: id },
    ]);

    if (users.length === 0) {
      return res.status(404).json({
        success: false,
        message: "User not found",
      });
    }

    res.json({
      success: true,
      data: users,
      message: "User retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve user",
    });
  }
};

exports.getUserStreak = async (req, res) => {
  try {
    const { id } = req.params;
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
    const streakData = user.streakData || {
      currentStreak: 0,
      longestStreak: 0,
      lastLogDate: null,
      streakStartDate: null,
    };

    res.json({
      success: true,
      data: {
        userId: id,
        currentStreak: streakData.currentStreak,
        longestStreak: streakData.longestStreak,
        lastLogDate: streakData.lastLogDate,
        streakStartDate: streakData.streakStartDate,
        isActive: streakService.isStreakActive(streakData.lastLogDate),
      },
      message: "User streak retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve user streak",
    });
  }
};

// UPDATE user (Firestore)
exports.updateUser = async (req, res) => {
  try {
    const { id } = req.params;
    if (!id) {
      return res.status(400).json({
        success: false,
        message: "User ID is required",
      });
    }

    const userData = req.body;
    if (!userData) {
      return res.status(400).json({
        success: false,
        message: "User data is required",
      });
    }

    // Check if user exists first
    const existingUser = await userService.getById(id);
    if (!existingUser) {
      return res.status(404).json({
        success: false,
        message: "User not found",
      });
    }

    let user = await userService.update(id, userData);
    if (user) {
      res.json({
        success: true,
        data: user,
        message: "User updated successfully",
      });
    } else {
      res.status(400).json({
        success: false,
        message: "Failed to update user",
      });
    }
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to update user",
    });
  }
};

// DELETE user (Firestore)
exports.deleteUser = async (req, res) => {
  try {
    const { id } = req.params;
    if (!id) {
      return res.status(400).json({
        success: false,
        message: "User ID is required",
      });
    }

    // Check if user exists first
    const existingUser = await userService.getById(id);
    if (!existingUser) {
      return res.status(404).json({
        success: false,
        message: "User not found",
      });
    }

    let user = await userService.delete(id);
    if (user) {
      res.json({
        success: true,
        data: user,
        message: "User deleted successfully",
      });
    } else {
      res.status(400).json({
        success: false,
        message: "Failed to delete user",
      });
    }
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to delete user",
    });
  }
};
