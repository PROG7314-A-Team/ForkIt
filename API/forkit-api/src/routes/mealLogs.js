const express = require("express");
const router = express.Router();
const mealLogController = require("../controllers/mealLogController");

// GET meal logs by date range
router.get("/date-range", mealLogController.getMealLogsByDateRange);

// GET all meal logs (with optional filters)
router.get("/user/:userId", mealLogController.getMealLogs);

// GET meal templates (isTemplate = true)
router.get("/templates/:userId", mealLogController.getMealTemplates);

// GET logged meals (isTemplate = false)
router.get("/logged/:userId", mealLogController.getLoggedMeals);

// GET meal log by ID
router.get("/:id", mealLogController.getMealLogById);

// POST create new meal log entry
router.post("/", mealLogController.createMealLog);

// PUT update meal log entry
router.put("/:id", mealLogController.updateMealLog);

// DELETE meal log entry
router.delete("/:id", mealLogController.deleteMealLog);

module.exports = router;
