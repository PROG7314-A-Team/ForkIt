const express = require("express");
const cors = require("cors");
const morgan = require("morgan");
const dotenv = require("dotenv");

// Load environment variables
dotenv.config();

// SSL Configuration for Node.js
const https = require("https");
const tls = require("tls");

// Configure TLS options
process.env.NODE_OPTIONS = "--tls-min-v1.0 --tls-max-v1.3";

// Initialize Firebase (this will be done in the config file)
try {
  require("./config/firebase");
} catch (error) {
  console.warn("Firebase initialization failed:", error.message);
  console.warn("Continuing without Firebase - some features may not work");
}

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(morgan("dev"));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Routes
const foodRoutes = require("./routes/food");
const userRoutes = require("./routes/users");
const foodLogRoutes = require("./routes/foodLogs");
const mealLogRoutes = require("./routes/mealLogs");
const waterLogRoutes = require("./routes/waterLogs");
const exerciseLogRoutes = require("./routes/exerciseLogs");
const calorieCalculatorRoutes = require("./routes/calorieCalculator");
const habitsRoutes = require("./routes/habits");

app.use("/api/food", foodRoutes);
app.use("/api/users", userRoutes);
app.use("/api/food-logs", foodLogRoutes);
app.use("/api/meal-logs", mealLogRoutes);
app.use("/api/water-logs", waterLogRoutes);
app.use("/api/exercise-logs", exerciseLogRoutes);
app.use("/api/calorie-calculator", calorieCalculatorRoutes);
app.use("/api/habits", habitsRoutes);

// Health check endpoint
app.get("/api/health", (req, res) => {
  res.json({
    status: "OK",
    message: "ForkIt API is running",
    timestamp: new Date().toISOString(),
  });
});

// Root endpoint
app.get("/", (req, res) => {
  res.json({
    message: "Welcome to ForkIt API",
    version: "1.0.0",
    endpoints: {
      health: "/api/health",
      food: "/api/food",
      users: "/api/users",
      foodLogs: "/api/food-logs",
      mealLogs: "/api/meal-logs",
      waterLogs: "/api/water-logs",
      exerciseLogs: "/api/exercise-logs",
      calorieCalculator: "/api/calorie-calculator",
      habits: "/api/habits",
    },
  });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({
    error: "Something went wrong!",
    message: err.message,
  });
});

// 404 handler
app.use("*", (req, res) => {
  res.status(404).json({
    error: "Endpoint not found",
  });
});

module.exports = app;

if (process.env.NODE_ENV !== "test") {
  app.listen(PORT, () => {
    console.log(`🚀 ForkIt API server running on port ${PORT}`);
    console.log(`🔄 Health check: http://localhost:${PORT}/api/health`);
    console.log(`🍽️ Food endpoints: http://localhost:${PORT}/api/food`);
    console.log(`👤 User endpoints: http://localhost:${PORT}/api/users`);
    console.log(`📝 Food logs: http://localhost:${PORT}/api/food-logs`);
    console.log(`🍳 Meal logs: http://localhost:${PORT}/api/meal-logs`);
    console.log(`💧 Water logs: http://localhost:${PORT}/api/water-logs`);
    console.log(`🏃 Exercise logs: http://localhost:${PORT}/api/exercise-logs`);
    console.log(
      `🧮 Calorie calculator: http://localhost:${PORT}/api/calorie-calculator`
    );
    console.log(`🏋️  Habits: http://localhost:${PORT}/api/habits`);
  });
}
