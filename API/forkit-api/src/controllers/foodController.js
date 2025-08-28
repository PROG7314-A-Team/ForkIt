// Mock data for demonstration
const mockFoodDatabase = [
  {
    id: "1",
    name: "Apple",
    brand: "Fresh Market",
    barcode: "1234567890123",
    calories: 95,
    protein: 0.5,
    carbs: 25,
    fat: 0.3,
    fiber: 4.4,
    sugar: 19,
  },
  {
    id: "2",
    name: "Banana",
    brand: "Organic Valley",
    barcode: "9876543210987",
    calories: 105,
    protein: 1.3,
    carbs: 27,
    fat: 0.4,
    fiber: 3.1,
    sugar: 14,
  },
];

// Get all food items (for search functionality)
exports.getAllFood = async (req, res) => {
  try {
    const { search, limit = 10 } = req.query;

    let results = mockFoodDatabase;

    // Simple search functionality
    if (search) {
      results = mockFoodDatabase.filter(
        (food) =>
          food.name.toLowerCase().includes(search.toLowerCase()) ||
          food.brand.toLowerCase().includes(search.toLowerCase())
      );
    }

    // Limit results
    results = results.slice(0, parseInt(limit));

    res.json({
      success: true,
      data: results,
      count: results.length,
      message: "Food items retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve food items",
    });
  }
};

// Get food by barcode (OpenFoodFacts integration placeholder)
exports.getFoodByBarcode = async (req, res) => {
  try {
    const { code } = req.params;

    // Find food by barcode in mock database
    const food = mockFoodDatabase.find((item) => item.barcode === code);

    if (!food) {
      return res.status(404).json({
        success: false,
        message: "Food item not found for this barcode",
      });
    }

    res.json({
      success: true,
      data: food,
      message: "Food item found by barcode",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve food by barcode",
    });
  }
};

// Get food by ID
exports.getFoodById = async (req, res) => {
  try {
    const { id } = req.params;

    const food = mockFoodDatabase.find((item) => item.id === id);

    if (!food) {
      return res.status(404).json({
        success: false,
        message: "Food item not found",
      });
    }

    res.json({
      success: true,
      data: food,
      message: "Food item retrieved successfully",
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
      message: "Failed to retrieve food item",
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
