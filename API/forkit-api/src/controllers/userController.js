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

exports.createUser = async (req, res) => {
  try {
    const userData = req.body;
    if (!userData.email) {
      return res.status(400).json({
        success: false,
        message: "Email is required",
      });
    }

    let userId = await userService.create(userData);
    userData.userId = String(userId.id);
    let user = await userService.update(String(userId.id), userData);

    if (user) {
      res.json({
        success: true,
        data: user,
        message: "User created successfully",
      });
    } else {
      res.status(400).json({
        success: false,
        message: "Failed to create user",
      });
    }
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to create user",
    });
  }
};

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

exports.deleteUser = async (req, res) => {
  try {
    const { id } = req.params;
    if (!id) {
      return res.status(400).json({
        success: false,
        message: "User ID is required",
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
