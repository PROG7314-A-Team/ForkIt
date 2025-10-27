// Minimal test server to isolate the issue
const express = require("express");
const cors = require("cors");

const app = express();
const PORT = process.env.PORT || 8080;

// Basic middleware
app.use(cors());
app.use(express.json());

// Simple test endpoint
app.get("/", (req, res) => {
  res.json({
    message: "ForkIt API Test Server",
    status: "OK",
    timestamp: new Date().toISOString()
  });
});

// Health check
app.get("/api/health", (req, res) => {
  res.json({
    status: "OK",
    message: "Test server is running",
    timestamp: new Date().toISOString()
  });
});

// Error handling
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(500).json({
    error: "Internal Server Error",
    message: err.message
  });
});

// Start server
app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 Test server running on port ${PORT}`);
}).on('error', (err) => {
  console.error('❌ Server failed to start:', err);
  process.exit(1);
});
