const express = require("express");
const router = express.Router();
const userController = require("../controllers/userController");
const authController = require("../controllers/authController"); // NEW
const goalsController = require("../controllers/goalsController"); // Goals controller

// GET users listing
router.get("/", userController.getUser);

// GET user by ID
router.get("/:id", userController.getUserById);

// GET user streak
router.get("/:id/streak", userController.getUserStreak);

// GET user goals
router.get("/:id/goals", goalsController.getUserGoals);

// PUT user goals
router.put("/:id/goals", goalsController.updateUserGoals);

// POST user -> Register endpoint
router.post("/register", authController.createUser);

// POST user -> Login endpoint
router.post("/login", authController.loginUser);

// PUT user
router.put("/:id", userController.updateUser);

// PUT user profile (age, height, weight)
router.put("/:id/profile", userController.updateUserProfile);

// DELETE user
router.delete("/:id", userController.deleteUser);

module.exports = router;
