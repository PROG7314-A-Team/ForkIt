/**
 * Calorie Calculator Service
 * 
 * This service provides methods to calculate calories from macronutrients
 * and handle automatic calorie estimation when macronutrient values are provided.
 * 
 * Macronutrient Calorie Values (per gram):
 * - Carbohydrates: 4 calories per gram
 * - Protein: 4 calories per gram  
 * - Fat: 9 calories per gram
 */

class CalorieCalculatorService {
  constructor() {
    // Macronutrient calorie values per gram
    this.MACRONUTRIENT_CALORIES = {
      CARBS: 4,      // 4 calories per gram of carbohydrates
      PROTEIN: 4,    // 4 calories per gram of protein
      FAT: 9         // 9 calories per gram of fat
    };
  }

  /**
   * Calculate calories from carbohydrates
   * @param {number} carbs - Grams of carbohydrates
   * @returns {number} - Calories from carbohydrates
   */
  calculateCarbsCalories(carbs) {
    if (!carbs || isNaN(carbs) || carbs < 0) {
      return 0;
    }
    return parseFloat(carbs) * this.MACRONUTRIENT_CALORIES.CARBS;
  }

  /**
   * Calculate calories from protein
   * @param {number} protein - Grams of protein
   * @returns {number} - Calories from protein
   */
  calculateProteinCalories(protein) {
    if (!protein || isNaN(protein) || protein < 0) {
      return 0;
    }
    return parseFloat(protein) * this.MACRONUTRIENT_CALORIES.PROTEIN;
  }

  /**
   * Calculate calories from fat
   * @param {number} fat - Grams of fat
   * @returns {number} - Calories from fat
   */
  calculateFatCalories(fat) {
    if (!fat || isNaN(fat) || fat < 0) {
      return 0;
    }
    return parseFloat(fat) * this.MACRONUTRIENT_CALORIES.FAT;
  }

  /**
   * Calculate total calories from all macronutrients
   * @param {Object} macronutrients - Object containing carbs, protein, fat values
   * @returns {Object} - Detailed breakdown of calories from each macronutrient and total
   */
  calculateTotalCalories(macronutrients) {
    const { carbs = 0, protein = 0, fat = 0 } = macronutrients;

    const carbsCalories = this.calculateCarbsCalories(carbs);
    const proteinCalories = this.calculateProteinCalories(protein);
    const fatCalories = this.calculateFatCalories(fat);

    const totalCalories = carbsCalories + proteinCalories + fatCalories;

    return {
      totalCalories: Math.round(totalCalories * 100) / 100, // Round to 2 decimal places
      breakdown: {
        carbs: {
          grams: parseFloat(carbs) || 0,
          calories: Math.round(carbsCalories * 100) / 100
        },
        protein: {
          grams: parseFloat(protein) || 0,
          calories: Math.round(proteinCalories * 100) / 100
        },
        fat: {
          grams: parseFloat(fat) || 0,
          calories: Math.round(fatCalories * 100) / 100
        }
      },
      macronutrientCalories: {
        carbs: Math.round(carbsCalories * 100) / 100,
        protein: Math.round(proteinCalories * 100) / 100,
        fat: Math.round(fatCalories * 100) / 100
      }
    };
  }

  /**
   * Calculate calories for food log entry
   * This method handles the scenario where user provides either:
   * 1. Direct calorie value
   * 2. Macronutrient values (carbs, protein, fat) to calculate calories
   * 3. Combination of both (validates consistency)
   * 
   * @param {Object} foodData - Food data containing calories and/or macronutrients
   * @returns {Object} - Calculated calorie data with validation
   */
  calculateFoodCalories(foodData) {
    const { 
      calories = 0, 
      carbs = 0, 
      protein = 0, 
      fat = 0 
    } = foodData;

    const hasDirectCalories = calories && calories > 0;
    const hasMacronutrients = (carbs && carbs > 0) || (protein && protein > 0) || (fat && fat > 0);

    // If no calories and no macronutrients provided
    if (!hasDirectCalories && !hasMacronutrients) {
      return {
        totalCalories: 0,
        calculatedFromMacronutrients: false,
        breakdown: {
          carbs: { grams: 0, calories: 0 },
          protein: { grams: 0, calories: 0 },
          fat: { grams: 0, calories: 0 }
        },
        validation: {
          isValid: false,
          message: "Either calories or macronutrients must be provided"
        }
      };
    }

    // If only direct calories provided
    if (hasDirectCalories && !hasMacronutrients) {
      return {
        totalCalories: parseFloat(calories),
        calculatedFromMacronutrients: false,
        breakdown: {
          carbs: { grams: 0, calories: 0 },
          protein: { grams: 0, calories: 0 },
          fat: { grams: 0, calories: 0 }
        },
        validation: {
          isValid: true,
          message: "Using provided calorie value"
        }
      };
    }

    // If only macronutrients provided, calculate calories
    if (!hasDirectCalories && hasMacronutrients) {
      const calculated = this.calculateTotalCalories({ carbs, protein, fat });
      return {
        ...calculated,
        calculatedFromMacronutrients: true,
        validation: {
          isValid: true,
          message: "Calories calculated from macronutrients"
        }
      };
    }

    // If both provided, validate consistency
    if (hasDirectCalories && hasMacronutrients) {
      const calculated = this.calculateTotalCalories({ carbs, protein, fat });
      const directCalories = parseFloat(calories);
      const difference = Math.abs(calculated.totalCalories - directCalories);
      const tolerance = 5; // 5 calorie tolerance for rounding differences

      return {
        ...calculated,
        calculatedFromMacronutrients: difference <= tolerance,
        providedCalories: directCalories,
        validation: {
          isValid: difference <= tolerance,
          message: difference <= tolerance 
            ? "Provided calories match calculated macronutrient calories"
            : `Calorie mismatch: provided ${directCalories}, calculated ${calculated.totalCalories}`
        }
      };
    }

    return {
      totalCalories: 0,
      calculatedFromMacronutrients: false,
      breakdown: {
        carbs: { grams: 0, calories: 0 },
        protein: { grams: 0, calories: 0 },
        fat: { grams: 0, calories: 0 }
      },
      validation: {
        isValid: false,
        message: "Invalid input data"
      }
    };
  }

  /**
   * Get macronutrient calorie values
   * @returns {Object} - Macronutrient calorie values per gram
   */
  getMacronutrientCalorieValues() {
    return { ...this.MACRONUTRIENT_CALORIES };
  }
}

module.exports = CalorieCalculatorService;
