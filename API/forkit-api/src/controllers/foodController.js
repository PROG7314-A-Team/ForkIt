const axios = require("axios");
const FirebaseService = require("../services/firebaseService");
const foodService = new FirebaseService("food");

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

    const transformedFood = {
      id: foodData.code || code,
      name: foodData.product?.product_name || "Unknown Product",
      brand: foodData.product?.brands || "Unknown Brand",
      barcode: code,
      calories: foodData.product?.nutriments?.["energy-kcal_100g"] || 0,
      protein: foodData.product?.nutriments?.proteins_100g || 0,
      carbs: foodData.product?.nutriments?.carbohydrates_100g || 0,
      fat: foodData.product?.nutriments?.fat_100g || 0,
      fiber: foodData.product?.nutriments?.fiber_100g || 0,
      sugar: foodData.product?.nutriments?.sugars_100g || 0,
      image: foodData.product?.image_url || null,
      ingredients: foodData.product?.ingredients_text || null,
      nutritionGrade: foodData.product?.nutrition_grade_fr || null,
    };

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

// Get food by ID
exports.getFoodByName = async (req, res) => {
  try {
    const { name } = req.params;
    const response = await axios.get(
      `https://world.openfoodfacts.org/cgi/search.pl?search_terms=${name}&json=1&fields=product_name,image_small_url,nutriments`
    );
    const foodData = response.data;
    res.json({
      success: true,
      data: foodData,
      message: "Food item found by name",
    });
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
