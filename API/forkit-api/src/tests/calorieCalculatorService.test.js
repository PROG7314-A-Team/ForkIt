const CalorieCalculatorService = require("../services/calorieCalculatorService");

describe("CalorieCalculatorService", () => {
  let calorieCalculator;

  beforeEach(() => {
    calorieCalculator = new CalorieCalculatorService();
  });

  describe("calculateCarbsCalories", () => {
    test("should calculate calories from carbs correctly", () => {
      const carbs = 50;
      const expectedCalories = 200; // 50g * 4 cal/g

      const result = calorieCalculator.calculateCarbsCalories(carbs);

      expect(result).toBe(expectedCalories);
    });

    test("should return 0 for negative carbs", () => {
      const result = calorieCalculator.calculateCarbsCalories(-10);
      expect(result).toBe(0);
    });

    test("should return 0 for null carbs", () => {
      const result = calorieCalculator.calculateCarbsCalories(null);
      expect(result).toBe(0);
    });

    test("should return 0 for NaN carbs", () => {
      const result = calorieCalculator.calculateCarbsCalories("invalid");
      expect(result).toBe(0);
    });
  });

  describe("calculateProteinCalories", () => {
    test("should calculate calories from protein correctly", () => {
      const protein = 30;
      const expectedCalories = 120; // 30g * 4 cal/g

      const result = calorieCalculator.calculateProteinCalories(protein);

      expect(result).toBe(expectedCalories);
    });

    test("should return 0 for invalid protein values", () => {
      expect(calorieCalculator.calculateProteinCalories(-10)).toBe(0);
      expect(calorieCalculator.calculateProteinCalories(null)).toBe(0);
      expect(calorieCalculator.calculateProteinCalories("invalid")).toBe(0);
    });
  });

  describe("calculateFatCalories", () => {
    test("should calculate calories from fat correctly", () => {
      const fat = 20;
      const expectedCalories = 180; // 20g * 9 cal/g

      const result = calorieCalculator.calculateFatCalories(fat);

      expect(result).toBe(expectedCalories);
    });

    test("should return 0 for invalid fat values", () => {
      expect(calorieCalculator.calculateFatCalories(-10)).toBe(0);
      expect(calorieCalculator.calculateFatCalories(null)).toBe(0);
    });
  });

  describe("calculateTotalCalories", () => {
    test("should calculate total calories from all macronutrients", () => {
      const macronutrients = {
        carbs: 50, // 200 cal
        protein: 30, // 120 cal
        fat: 20, // 180 cal
      };
      const expectedTotal = 500;

      const result = calorieCalculator.calculateTotalCalories(macronutrients);

      expect(result.totalCalories).toBe(expectedTotal);
      expect(result.breakdown.carbs.grams).toBe(50);
      expect(result.breakdown.carbs.calories).toBe(200);
      expect(result.breakdown.protein.grams).toBe(30);
      expect(result.breakdown.protein.calories).toBe(120);
      expect(result.breakdown.fat.grams).toBe(20);
      expect(result.breakdown.fat.calories).toBe(180);
    });

    test("should handle missing macronutrients with defaults", () => {
      const macronutrients = {
        carbs: 50,
        // protein and fat missing
      };

      const result = calorieCalculator.calculateTotalCalories(macronutrients);

      expect(result.totalCalories).toBe(200); // Only carbs
      expect(result.breakdown.protein.grams).toBe(0);
      expect(result.breakdown.fat.grams).toBe(0);
    });

    test("should return correct macronutrient breakdown", () => {
      const macronutrients = {
        carbs: 100,
        protein: 50,
        fat: 25,
      };

      const result = calorieCalculator.calculateTotalCalories(macronutrients);

      expect(result.macronutrientCalories.carbs).toBe(400);
      expect(result.macronutrientCalories.protein).toBe(200);
      expect(result.macronutrientCalories.fat).toBe(225);
    });
  });

  describe("calculateFoodCalories", () => {
    test("should return error when no calories or macronutrients provided", () => {
      const foodData = {
        calories: 0,
        carbs: 0,
        protein: 0,
        fat: 0,
      };

      const result = calorieCalculator.calculateFoodCalories(foodData);

      expect(result.validation.isValid).toBe(false);
      expect(result.validation.message).toBe(
        "Either calories or macronutrients must be provided"
      );
    });

    test("should use direct calories when only calories provided", () => {
      const foodData = {
        calories: 250,
        carbs: 0,
        protein: 0,
        fat: 0,
      };

      const result = calorieCalculator.calculateFoodCalories(foodData);

      expect(result.totalCalories).toBe(250);
      expect(result.calculatedFromMacronutrients).toBe(false);
      expect(result.validation.isValid).toBe(true);
      expect(result.validation.message).toBe("Using provided calorie value");
    });

    test("should calculate from macronutrients when only macros provided", () => {
      const foodData = {
        calories: 0,
        carbs: 50,
        protein: 30,
        fat: 20,
      };

      const result = calorieCalculator.calculateFoodCalories(foodData);

      expect(result.totalCalories).toBe(500);
      expect(result.calculatedFromMacronutrients).toBe(true);
      expect(result.validation.isValid).toBe(true);
      expect(result.validation.message).toBe(
        "Calories calculated from macronutrients"
      );
    });

    test("should validate consistency when both calories and macros provided", () => {
      const foodData = {
        calories: 500,
        carbs: 50,
        protein: 30,
        fat: 20,
      };

      const result = calorieCalculator.calculateFoodCalories(foodData);

      expect(result.validation.isValid).toBe(true);
      expect(result.providedCalories).toBe(500);
    });

    test("should detect calorie mismatch when values do not match", () => {
      const foodData = {
        calories: 300, // Incorrect
        carbs: 50, // 200
        protein: 30, // 120
        fat: 20, // 180
        // Total should be 500, not 300
      };

      const result = calorieCalculator.calculateFoodCalories(foodData);

      expect(result.validation.isValid).toBe(false);
      expect(result.validation.message).toContain("Calorie mismatch");
    });

    test("should allow small tolerance in calorie matching", () => {
      const foodData = {
        calories: 503, // Within 5 calorie tolerance
        carbs: 50,
        protein: 30,
        fat: 20,
        // Calculated: 500 calories
      };

      const result = calorieCalculator.calculateFoodCalories(foodData);

      expect(result.validation.isValid).toBe(true);
    });
  });

  describe("getMacronutrientCalorieValues", () => {
    test("should return correct macronutrient calorie values", () => {
      const values = calorieCalculator.getMacronutrientCalorieValues();

      expect(values.CARBS).toBe(4);
      expect(values.PROTEIN).toBe(4);
      expect(values.FAT).toBe(9);
    });

    test("should return a copy of values to prevent modification", () => {
      const values = calorieCalculator.getMacronutrientCalorieValues();
      values.CARBS = 100;

      const newValues = calorieCalculator.getMacronutrientCalorieValues();
      expect(newValues.CARBS).toBe(4);
    });
  });
});
