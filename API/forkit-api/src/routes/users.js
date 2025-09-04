const express = require("express");
const router = express.Router();
const userController = require("../controllers/userController");

// GET users listing
router.get("/", userController.getUser);

module.exports = router;
