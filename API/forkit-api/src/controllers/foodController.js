const axios = require("axios");
const FirebaseService = require("../services/firebaseService");
const foodService = new FirebaseService("food");

// Helper function to check if food has valid macro nutrient values
const hasValidMacroNutrients = (nutrients) => {
  if (!nutrients) return false;

  const { carbs, protein, fat } = nutrients;

  // Check if at least one macro nutrient has a value greater than 0
  return (carbs && carbs > 0) || (protein && protein > 0) || (fat && fat > 0);
};

// Helper function to parse serving size using regex
const parseServingSize = (servingSizeString) => {
  if (!servingSizeString) {
    return { quantity: null, unit: null, original: null };
  }

  // Remove extra whitespace and convert to string
  const cleanSize = String(servingSizeString).trim();

  // Regex to match number (including decimals) followed by optional unit
  // Examples: "200g", "1 cup", "30.5ml", "1.5 serving", "250"
  const regex =
    /^(\d+(?:\.\d+)?)\s*([a-zA-Z\u00C0-\u017F\u0100-\u017F\u0180-\u024F\u1E00-\u1EFF]*)?$/;
  const match = cleanSize.match(regex);

  if (match) {
    const quantity = parseFloat(match[1]);
    const unit = match[2] ? match[2].trim() : null;

    return {
      quantity: quantity,
      unit: unit,
      original: cleanSize,
    };
  }

  // If regex doesn't match, try to extract just the number
  const numberMatch = cleanSize.match(/(\d+(?:\.\d+)?)/);
  if (numberMatch) {
    return {
      quantity: parseFloat(numberMatch[1]),
      unit: null,
      original: cleanSize,
    };
  }

  return { quantity: null, unit: null, original: cleanSize };
};

// Helper function to extract comprehensive nutritional data
const extractNutritionalData = (product) => {
  const nutriments = product.nutriments || {};

  // Parse serving size using regex
  const parsedServingSize = parseServingSize(product.serving_size);

  return {
    // Basic nutritional info per 100g
    nutrients: {
      carbs: nutriments.carbohydrates_100g || nutriments.carbohydrates || 0,
      protein: nutriments.proteins_100g || nutriments.proteins || 0,
      fat: nutriments.fat_100g || nutriments.fat || 0,
    },
    calories: nutriments["energy-kcal_100g"] || nutriments["energy-kcal"] || 0,

    // Enhanced serving size information with regex parsing
    servingSize: {
      size: product.serving_size || null,
      quantity: parsedServingSize.quantity,
      unit: parsedServingSize.unit,
      original: parsedServingSize.original,
      // Keep original API values as fallback
      apiQuantity: product.serving_quantity || null,
      apiUnit: product.serving_quantity_unit || null,
    },

    // Nutritional info per serving (when available)
    nutrientsPerServing: {
      carbs: nutriments.carbohydrates_serving || null,
      protein: nutriments.proteins_serving || null,
      fat: nutriments.fat_serving || null,
    },
    caloriesPerServing: nutriments["energy-kcal_serving"] || null,
  };
};

// Get food by barcode
exports.getFoodByBarcode = async (req, res) => {
  try {
    const { code } = req.params;

    const response = await axios.get(
      `https://world.openfoodfacts.net/api/v1/product/${code}`
    );

    const foodData = response.data;

    if (!foodData || foodData.status === 0) {
      return res.status(404).json({
        success: false,
        message: "Food item not found for this barcode",
      });
    }

    const nutritionalData = extractNutritionalData(foodData.product);

    const transformedFood = {
      id: foodData.code || code,
      name: foodData.product?.product_name || "Unknown Product",
      brand: foodData.product?.brands || "Unknown Brand",
      barcode: code,
      ...nutritionalData,
      image: foodData.product?.image_url || null,
      ingredients: foodData.product?.ingredients_text || null,
    };

    // Filter: Only return food items with valid macro nutrient values
    if (!hasValidMacroNutrients(transformedFood.nutrients)) {
      return res.status(404).json({
        success: false,
        message:
          "Food item found but lacks macro nutrient information (carbs, protein, fat)",
      });
    }

    res.json({
      success: true,
      data: transformedFood,
      message: "Food item found by barcode",
    });
  } catch (error) {
    console.error("OpenFoodFacts API Error:", error.message);
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve food by barcode",
    });
  }
};

// Get food by name
exports.getFoodByName = async (req, res) => {
  try {
    const { name } = req.params;
    const response = await axios.get(
      `https://world.openfoodfacts.org/cgi/search.pl?search_terms=${name}&json=1&countries_tags=South_Africa`
    );
    if (response.data.count > 0) {
      let foodData = {};
      let validProductIndex = 0;

      for (let i = 0; i < response.data.count; i++) {
        const product = response.data.products[i];

        // Only process products that have the required fields and valid macro nutrients
        if (product && product.product_name && product.nutriments) {
          const nutritionalData = extractNutritionalData(product);

          // Filter: Only include food items with valid macro nutrient values
          if (hasValidMacroNutrients(nutritionalData.nutrients)) {
            foodData[validProductIndex] = {
              name: product.product_name,
              brand: product.brands || "Unknown Brand",
              image: product.image_small_url || null,
              ...nutritionalData,
            };
            validProductIndex++;
          }
        }
      }

      // Check if we found any valid products
      if (Object.keys(foodData).length === 0) {
        // No valid products from OpenFoodFacts, try local database
        const localFoodData = await foodService.query([
          { field: "name", operator: "==", value: name },
        ]);

        // Filter local database results to only include items with macro nutrients
        const filteredLocalData = localFoodData.filter((food) =>
          hasValidMacroNutrients(food.nutrients)
        );

        //console.log("Filtered local data", filteredLocalData);

        if (filteredLocalData.length > 0) {
          return res.status(200).json({
            success: true,
            data: filteredLocalData,
            message: "Food item found by name from ForkIt Database",
          });
        }
        return res.status(404).json({
          success: false,
          message: "Food item not found by name",
        });
      }

      console.log("Food data", foodData);

      return res.status(200).json({
        success: true,
        data: foodData,
        message: "Food item found by name from OpenFoodFacts",
      });
    } else {
      // Query local database
      const foodData = await foodService.query([
        { field: "name", operator: "==", value: name },
      ]);

      // Filter local database results to only include items with macro nutrients
      const filteredFoodData = foodData.filter((food) =>
        hasValidMacroNutrients(food.nutrients)
      );

      console.log("Filtered food data", filteredFoodData);

      if (filteredFoodData.length > 0) {
        return res.status(200).json({
          success: true,
          data: filteredFoodData,
          message: "Food item found by name from ForkIt Database",
        });
      }
      return res.status(404).json({
        success: false,
        message: "Food item not found by name",
      });
    }
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve food by name",
    });
  }
};

// Create new food item
exports.createFood = async (req, res) => {
  try {
    const foodData = req.body;

    // Basic validation
    if (!foodData.name || !foodData.nutrients) {
      return res.status(400).json({
        success: false,
        message: "Food name and nutriments are required",
      });
    }

    // Create new food item
    const newFood = {
      id: Date.now().toString(),
      ...foodData,
      createdAt: new Date().toISOString(),
    };

    const foodId = await foodService.create(foodData);
    foodData.foodId = String(foodId.id);
    const updatedFood = await foodService.update(String(foodId.id), foodData);

    res.status(201).json({
      success: true,
      data: updatedFood,
      message: "Food item created successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to create food item",
    });
  }
};

// Update food item
exports.updateFood = async (req, res) => {
  try {
    const { id } = req.params;
    const updateData = req.body;

    const foodIndex = await foodService.query([
      { field: "foodId", operator: "==", value: id },
    ]);

    if (foodIndex.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Food item not found",
      });
    }

    // Update the food item
    foodIndex[0] = {
      ...foodIndex[0],
      ...updateData,
      updatedAt: new Date().toISOString(),
    };

    res.json({
      success: true,
      data: foodIndex[0],
      message: "Food item updated successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to update food item",
    });
  }
};

// Delete food item
exports.deleteFood = async (req, res) => {
  try {
    const { id } = req.params;

    const foodIndex = await foodService.query([
      { field: "foodId", operator: "==", value: id },
    ]);

    if (foodIndex.length === 0) {
      return res.status(404).json({
        success: false,
        message: "Food item not found",
      });
    }

    const deletedFood = await foodService.delete(foodIndex[0].id);

    res.json({
      success: true,
      data: { id: deletedFood.id } || deletedFood,
      message: "Food item deleted successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to delete food item",
    });
  }
};
