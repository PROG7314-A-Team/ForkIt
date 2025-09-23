const CalorieCalculatorService = require("../services/calorieCalculatorService");

const calorieCalculator = new CalorieCalculatorService();

/**
 * Calculate calories from macronutrients
 * POST /api/calorie-calculator/calculate
 */
exports.calculateCalories = async (req, res) => {
  try {
    const { carbs, protein, fat } = req.body;

    // Validate input
    if (!carbs && !protein && !fat) {
      return res.status(400).json({
        success: false,
        message: "At least one macronutrient (carbs, protein, fat) must be provided",
        data: null
      });
    }

    const result = calorieCalculator.calculateTotalCalories({ carbs, protein, fat });

    res.json({
      success: true,
      data: result,
      message: "Calories calculated successfully"
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to calculate calories"
    });
  }
};

/**
 * Calculate calories for food log entry
 * POST /api/calorie-calculator/food-calories
 */
exports.calculateFoodCalories = async (req, res) => {
  try {
    const { calories, carbs, protein, fat } = req.body;

    // Validate input
    if (!calories && !carbs && !protein && !fat) {
      return res.status(400).json({
        success: false,
        message: "Either calories or macronutrients must be provided",
        data: null
      });
    }

    const result = calorieCalculator.calculateFoodCalories({
      calories,
      carbs,
      protein,
      fat
    });

    res.json({
      success: true,
      data: result,
      message: result.validation.isValid 
        ? "Food calories calculated successfully" 
        : "Calculation completed with warnings"
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to calculate food calories"
    });
  }
};

/**
 * Get macronutrient calorie values
 * GET /api/calorie-calculator/macronutrient-values
 */
exports.getMacronutrientValues = async (req, res) => {
  try {
    const values = calorieCalculator.getMacronutrientCalorieValues();

    res.json({
      success: true,
      data: {
        macronutrientCalories: values,
        description: {
          carbs: "4 calories per gram of carbohydrates",
          protein: "4 calories per gram of protein",
          fat: "9 calories per gram of fat"
        }
      },
      message: "Macronutrient calorie values retrieved successfully"
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve macronutrient values"
    });
  }
};

/**
 * Calculate individual macronutrient calories
 * POST /api/calorie-calculator/individual
 */
exports.calculateIndividualCalories = async (req, res) => {
  try {
    const { macronutrient, grams } = req.body;

    if (!macronutrient || grams === undefined) {
      return res.status(400).json({
        success: false,
        message: "Macronutrient type and grams are required",
        data: null
      });
    }

    if (!['carbs', 'protein', 'fat'].includes(macronutrient.toLowerCase())) {
      return res.status(400).json({
        success: false,
        message: "Macronutrient must be 'carbs', 'protein', or 'fat'",
        data: null
      });
    }

    let calories = 0;
    const macronutrientType = macronutrient.toLowerCase();

    switch (macronutrientType) {
      case 'carbs':
        calories = calorieCalculator.calculateCarbsCalories(grams);
        break;
      case 'protein':
        calories = calorieCalculator.calculateProteinCalories(grams);
        break;
      case 'fat':
        calories = calorieCalculator.calculateFatCalories(grams);
        break;
    }

    res.json({
      success: true,
      data: {
        macronutrient: macronutrientType,
        grams: parseFloat(grams) || 0,
        calories: Math.round(calories * 100) / 100,
        caloriesPerGram: calorieCalculator.getMacronutrientCalorieValues()[macronutrientType.toUpperCase()]
      },
      message: `${macronutrientType} calories calculated successfully`
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to calculate individual macronutrient calories"
    });
  }
};
