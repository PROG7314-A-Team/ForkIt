const express = require("express");
const router = express.Router();
const exerciseLogController = require("../controllers/exerciseLogController");

// GET all exercise logs (with optional filters)
router.get("/", exerciseLogController.getExerciseLogs);

// GET daily exercise total
router.get("/daily-total", exerciseLogController.getDailyExerciseTotal);

// GET daily exercise summary for dashboard
router.get("/daily-summary", exerciseLogController.getDailyExerciseSummary);

// GET monthly exercise summary for dashboard
router.get("/monthly-summary", exerciseLogController.getMonthlyExerciseSummary);

// GET recent exercise activity for dashboard
router.get("/recent-activity", exerciseLogController.getRecentExerciseActivity);

// GET exercise trends over time
router.get("/trends", exerciseLogController.getExerciseTrends);

// GET comprehensive exercise dashboard data
router.get("/dashboard", exerciseLogController.getExerciseDashboardData);

// GET exercise logs by date range
router.get("/date-range", exerciseLogController.getExerciseLogsByDateRange);

// GET exercise log by ID
router.get("/:id", exerciseLogController.getExerciseLogById);

// POST create new exercise log entry
router.post("/", exerciseLogController.createExerciseLog);

// PUT update exercise log entry
router.put("/:id", exerciseLogController.updateExerciseLog);

// DELETE exercise log entry
router.delete("/:id", exerciseLogController.deleteExerciseLog);

module.exports = router;
