const FirebaseService = require("../services/firebaseService");
const userService = new FirebaseService("users");
exports.getUser = async (req, res) => {
  try {
    let user = await userService.getAll();

    if (!user) {
      return res.status(404).json({
        success: false,
        message: "User not found",
      });
    }

    res.json({
      success: true,
      data: user,
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
