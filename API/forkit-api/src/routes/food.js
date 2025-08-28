const express = require("express");
const router = express.Router();

const foodController = require("../controllers/foodController");

// GET all food items (for food database search)
router.get("/", foodController.getAllFood);

// GET food by barcode (OpenFoodFacts integration)
router.get("/barcode/:code", foodController.getFoodByBarcode);

// GET food by ID
router.get("/:id", foodController.getFoodById);

// POST new custom food item
router.post("/", foodController.createFood);

// PUT update food item
router.put("/:id", foodController.updateFood);

// DELETE food item
router.delete("/:id", foodController.deleteFood);

module.exports = router;
