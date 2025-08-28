const express = require("express");
const router = express.Router();

// GET users listing
router.get("/", (req, res) => {
  res.json({
    success: true,
    message: "Users endpoint - to be implemented",
    data: [],
  });
});

module.exports = router;
