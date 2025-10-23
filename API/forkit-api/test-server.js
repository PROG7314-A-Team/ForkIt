const express = require("express");
const app = express();
const PORT = process.env.PORT || 3000;

// Basic middleware
app.use(express.json());

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
    status: "running"
  });
});

// Start server
app.listen(PORT, () => {
  console.log(`🚀 ForkIt API server running on port ${PORT}`);
});

module.exports = app;
