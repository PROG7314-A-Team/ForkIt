const express = require("express");
const router = express.Router();

const habitsController = require("../controllers/habitsController");

// GET daily habits
router.get("/daily/:userId", habitsController.getDailyHabits);

// GET weekly habits
router.get("/weekly/:userId", habitsController.getWeeklyHabits);

// GET monthly habits
router.get("/monthly/:userId", habitsController.getMonthlyHabits);

// POST create habit
router.post("/", habitsController.createHabit); 

// PUT update habit
router.put("/:id", habitsController.updateHabit);

module.exports = router;