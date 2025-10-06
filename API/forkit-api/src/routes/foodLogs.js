const express = require("express");
const router = express.Router();
const foodLogController = require("../controllers/foodLogController");

// GET food logs by date range
router.get("/date-range", foodLogController.getFoodLogsByDateRange);

// GET daily calorie summary for dashboard
router.get("/daily-summary", foodLogController.getDailyCalorieSummary);

// GET monthly calorie summary for dashboard
router.get("/monthly-summary", foodLogController.getMonthlyCalorieSummary);

// GET recent food activity for dashboard
router.get("/recent-activity", foodLogController.getRecentFoodActivity);

// GET calorie trends over time
router.get("/trends", foodLogController.getCalorieTrends);

// GET comprehensive dashboard data
router.get("/dashboard", foodLogController.getDashboardData);

// GET all food logs (with optional filters)
router.get("/user/:userId", foodLogController.getFoodLogs);

// GET food log by ID
router.get("/:id", foodLogController.getFoodLogById);

// POST create new food log entry
router.post("/", foodLogController.createFoodLog);

// PUT update food log entry
router.put("/:id", foodLogController.updateFoodLog);

// DELETE food log entry
router.delete("/:id", foodLogController.deleteFoodLog);

module.exports = router;
