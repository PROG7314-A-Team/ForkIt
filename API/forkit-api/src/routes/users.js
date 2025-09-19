const express = require("express");
const router = express.Router();
const userController = require("../controllers/userController");

// GET users listing
router.get("/", userController.getUser);

// GET user by ID
router.get("/:id", userController.getUserById);

// GET user streak
router.get("/:id/streak", userController.getUserStreak);

// POST user
router.post("/", userController.createUser);

// PUT user
router.put("/:id", userController.updateUser);

// DELETE user
router.delete("/:id", userController.deleteUser);

module.exports = router;
