const axios = require("axios");

const openFoodFactsUrl = "https://world.openfoodfacts.net/api/v2/";

// Get food by barcode
exports.getFoodByBarcode = async (req, res) => {
  try {
    const { code } = req.params;

    const response = await axios.get(`${openFoodFactsUrl}product/${code}`);

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
    const response = await axios.get(`${openFoodFactsUrl}search?${name}`);
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
    if (!foodData.name) {
      return res.status(400).json({
        success: false,
        message: "Food name is required",
      });
    }

    // Create new food item
    const newFood = {
      id: Date.now().toString(),
      ...foodData,
      createdAt: new Date().toISOString(),
    };

    // In a real app, you'd save to database here
    mockFoodDatabase.push(newFood);

    res.status(201).json({
      success: true,
      data: newFood,
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

    const foodIndex = mockFoodDatabase.findIndex((item) => item.id === id);

    if (foodIndex === -1) {
      return res.status(404).json({
        success: false,
        message: "Food item not found",
      });
    }

    // Update the food item
    mockFoodDatabase[foodIndex] = {
      ...mockFoodDatabase[foodIndex],
      ...updateData,
      updatedAt: new Date().toISOString(),
    };

    res.json({
      success: true,
      data: mockFoodDatabase[foodIndex],
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

    const foodIndex = mockFoodDatabase.findIndex((item) => item.id === id);

    if (foodIndex === -1) {
      return res.status(404).json({
        success: false,
        message: "Food item not found",
      });
    }

    const deletedFood = mockFoodDatabase.splice(foodIndex, 1)[0];

    res.json({
      success: true,
      data: deletedFood,
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
