const express = require("express");
const router = express.Router();
const userController = require("../controllers/userController");
const authController = require("../controllers/authController"); // NEW

// GET users listing
router.get("/", userController.getUser);

// GET user by ID
router.get("/:id", userController.getUserById);

// GET user streak
router.get("/:id/streak", userController.getUserStreak);

// POST user -> Register endpoint
router.post("/register", authController.createUser);

// POST user -> Login endpoint
router.post("/login", authController.loginUser);

// POST user -> Google SSO Register endpoint
router.post("/registerGoogleUser", authController.registerGoogleUser);

// POST user -> Google SSO Login endpoint
router.post("/loginGoogleUser", authController.loginGoogleUser);

// PUT user
router.put("/:id", userController.updateUser);

// DELETE user
router.delete("/:id", userController.deleteUser);

module.exports = router;
