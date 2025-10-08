const express = require("express");
const router = express.Router();

const foodController = require("../controllers/foodController");

// GET food by barcode (OpenFoodFacts integration)
router.get("/barcode/:code", foodController.getFoodByBarcode);

// GET food by name
router.get("/:name", foodController.getFoodByName);

// POST new custom food item
router.post("/", foodController.createFood);

// PUT update food item
router.put("/:id", foodController.updateFood);

// DELETE food item
router.delete("/:id", foodController.deleteFood);

module.exports = router;
