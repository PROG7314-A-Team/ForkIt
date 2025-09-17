const express = require("express");
const router = express.Router();
const waterLogController = require("../controllers/waterLogController");

// GET all water logs (with optional filters)
router.get("/", waterLogController.getWaterLogs);

// GET daily water total
router.get("/daily-total", waterLogController.getDailyWaterTotal);

// GET daily water summary for dashboard
router.get("/daily-summary", waterLogController.getDailyWaterSummary);

// GET monthly water summary for dashboard
router.get("/monthly-summary", waterLogController.getMonthlyWaterSummary);

// GET recent water activity for dashboard
router.get("/recent-activity", waterLogController.getRecentWaterActivity);

// GET water trends over time
router.get("/trends", waterLogController.getWaterTrends);

// GET comprehensive water dashboard data
router.get("/dashboard", waterLogController.getWaterDashboardData);

// GET water logs by date range
router.get("/date-range", waterLogController.getWaterLogsByDateRange);

// GET water log by ID
router.get("/:id", waterLogController.getWaterLogById);

// POST create new water log entry
router.post("/", waterLogController.createWaterLog);

// PUT update water log entry
router.put("/:id", waterLogController.updateWaterLog);

// DELETE water log entry
router.delete("/:id", waterLogController.deleteWaterLog);

module.exports = router;
