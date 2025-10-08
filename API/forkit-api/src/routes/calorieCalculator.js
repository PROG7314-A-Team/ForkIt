const express = require("express");
const router = express.Router();
const calorieCalculatorController = require("../controllers/calorieCalculatorController");

// GET macronutrient calorie values
router.get("/macronutrient-values", calorieCalculatorController.getMacronutrientValues);

// POST calculate total calories from macronutrients
router.post("/calculate", calorieCalculatorController.calculateCalories);

// POST calculate individual macronutrient calories
router.post("/individual", calorieCalculatorController.calculateIndividualCalories);

// POST calculate food calories (handles both direct calories and macronutrient calculation)
router.post("/food-calories", calorieCalculatorController.calculateFoodCalories);

module.exports = router;
