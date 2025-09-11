const express = require("express");
const cors = require("cors");
const morgan = require("morgan");
require("dotenv").config();

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

// --- ADDED: Firebase Auth Route ---
const authRoutes = require("./routes/auth"); // <--- new route import
app.use("/api/auth", authRoutes); // <--- new route usage

app.use("/api/food", foodRoutes);
app.use("/api/users", userRoutes);

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
      auth_register: "/api/auth/register" // <--- optional, for reference
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

app.listen(PORT, () => {
  console.log(`🚀 ForkIt API server running on port ${PORT}`);
  console.log(`🔄 Health check: http://localhost:${PORT}/api/health`);
  console.log(`🍽️  Food endpoints: http://localhost:${PORT}/api/food`);
  console.log(`🔑 Auth register endpoint: http://localhost:${PORT}/api/auth/register`);
});
