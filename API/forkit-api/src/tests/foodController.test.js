// Mock dependencies first
const mockFirebaseService = jest.fn();
jest.mock("../services/firebaseService", () => mockFirebaseService);

const foodController = require("../controllers/foodController");

describe("FoodController", () => {
  let mockFoodService;
  let mockReq;
  let mockRes;

  beforeEach(() => {
    // Reset mocks
    jest.clearAllMocks();

    // Setup mock service
    mockFoodService = {
      getAll: jest.fn(),
      getById: jest.fn(),
      create: jest.fn(),
      update: jest.fn(),
      delete: jest.fn(),
      query: jest.fn(),
    };

    // Setup mock request and response
    mockReq = {
      params: {},
      body: {},
      query: {},
    };
    mockRes = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn(),
    };

    // Mock service constructor
    mockFirebaseService.mockImplementation(() => mockFoodService);
  });

  describe("getAllFoods", () => {
    test("should return all foods successfully", async () => {
      const mockFoods = [
        { id: "1", name: "Apple", calories: 95 },
        { id: "2", name: "Banana", calories: 105 },
      ];

      mockFoodService.getAll.mockResolvedValue(mockFoods);

      await foodController.getAllFoods(mockReq, mockRes);

      expect(mockFoodService.getAll).toHaveBeenCalled();
      expect(mockRes.json).toHaveBeenCalledWith({
        success: true,
        data: mockFoods,
        message: "Foods retrieved successfully",
      });
    });

    test("should handle errors when retrieving foods", async () => {
      const error = new Error("Database error");
      mockFoodService.getAll.mockRejectedValue(error);

      await foodController.getAllFoods(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        error: "Database error",
        message: "Failed to retrieve foods",
      });
    });
  });

  describe("getFoodById", () => {
    test("should return food by ID successfully", async () => {
      const foodId = "food123";
      const mockFood = { id: "food123", name: "Chicken Breast", calories: 165 };

      mockReq.params.id = foodId;
      mockFoodService.getById.mockResolvedValue(mockFood);

      await foodController.getFoodById(mockReq, mockRes);

      expect(mockFoodService.getById).toHaveBeenCalledWith(foodId);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: true,
        data: mockFood,
        message: "Food retrieved successfully",
      });
    });

    test("should return 404 when food not found", async () => {
      const foodId = "non-existent";

      mockReq.params.id = foodId;
      mockFoodService.getById.mockResolvedValue(null);

      await foodController.getFoodById(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(404);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        message: "Food not found",
      });
    });

    test("should handle errors when retrieving food by ID", async () => {
      const foodId = "food123";
      const error = new Error("Database error");

      mockReq.params.id = foodId;
      mockFoodService.getById.mockRejectedValue(error);

      await foodController.getFoodById(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        error: "Database error",
        message: "Failed to retrieve food",
      });
    });
  });

  describe("createFood", () => {
    test("should create food successfully", async () => {
      const foodData = {
        name: "New Food",
        calories: 200,
        carbs: 30,
        protein: 10,
        fat: 5,
      };
      const mockCreatedFood = { id: "new-food-id", ...foodData };

      mockReq.body = foodData;
      mockFoodService.create.mockResolvedValue(mockCreatedFood);

      await foodController.createFood(mockReq, mockRes);

      expect(mockFoodService.create).toHaveBeenCalledWith(foodData);
      expect(mockRes.status).toHaveBeenCalledWith(201);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: true,
        data: mockCreatedFood,
        message: "Food created successfully",
      });
    });

    test("should return 400 when required fields are missing", async () => {
      const incompleteData = { name: "Incomplete Food" };

      mockReq.body = incompleteData;

      await foodController.createFood(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(400);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        message: "Name, calories, carbs, protein, and fat are required",
      });
    });

    test("should handle errors when creating food", async () => {
      const foodData = {
        name: "New Food",
        calories: 200,
        carbs: 30,
        protein: 10,
        fat: 5,
      };
      const error = new Error("Creation error");

      mockReq.body = foodData;
      mockFoodService.create.mockRejectedValue(error);

      await foodController.createFood(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        error: "Creation error",
        message: "Failed to create food",
      });
    });
  });

  describe("updateFood", () => {
    test("should update food successfully", async () => {
      const foodId = "food123";
      const updateData = { name: "Updated Food", calories: 250 };
      const mockUpdatedFood = { id: foodId, ...updateData };

      mockReq.params.id = foodId;
      mockReq.body = updateData;
      mockFoodService.getById.mockResolvedValue({ id: foodId });
      mockFoodService.update.mockResolvedValue(mockUpdatedFood);

      await foodController.updateFood(mockReq, mockRes);

      expect(mockFoodService.update).toHaveBeenCalledWith(foodId, updateData);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: true,
        data: mockUpdatedFood,
        message: "Food updated successfully",
      });
    });

    test("should return 404 when food not found for update", async () => {
      const foodId = "non-existent";
      const updateData = { name: "Updated Food" };

      mockReq.params.id = foodId;
      mockReq.body = updateData;
      mockFoodService.getById.mockResolvedValue(null);

      await foodController.updateFood(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(404);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        message: "Food not found",
      });
    });

    test("should handle errors when updating food", async () => {
      const foodId = "food123";
      const updateData = { name: "Updated Food" };
      const error = new Error("Update error");

      mockReq.params.id = foodId;
      mockReq.body = updateData;
      mockFoodService.getById.mockResolvedValue({ id: foodId });
      mockFoodService.update.mockRejectedValue(error);

      await foodController.updateFood(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        error: "Update error",
        message: "Failed to update food",
      });
    });
  });

  describe("deleteFood", () => {
    test("should delete food successfully", async () => {
      const foodId = "food123";
      const mockFood = { id: foodId, name: "Test Food" };

      mockReq.params.id = foodId;
      mockFoodService.getById.mockResolvedValue(mockFood);
      mockFoodService.delete.mockResolvedValue(true);

      await foodController.deleteFood(mockReq, mockRes);

      expect(mockFoodService.delete).toHaveBeenCalledWith(foodId);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: true,
        data: mockFood,
        message: "Food deleted successfully",
      });
    });

    test("should return 404 when food not found for deletion", async () => {
      const foodId = "non-existent";

      mockReq.params.id = foodId;
      mockFoodService.getById.mockResolvedValue(null);

      await foodController.deleteFood(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(404);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        message: "Food not found",
      });
    });

    test("should handle errors when deleting food", async () => {
      const foodId = "food123";
      const error = new Error("Delete error");

      mockReq.params.id = foodId;
      mockFoodService.getById.mockResolvedValue({ id: foodId });
      mockFoodService.delete.mockRejectedValue(error);

      await foodController.deleteFood(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        error: "Delete error",
        message: "Failed to delete food",
      });
    });
  });

  describe("searchFoods", () => {
    test("should search foods by name successfully", async () => {
      const searchTerm = "chicken";
      const mockFoods = [
        { id: "1", name: "Chicken Breast", calories: 165 },
        { id: "2", name: "Chicken Thigh", calories: 180 },
      ];

      mockReq.query.name = searchTerm;
      mockFoodService.query.mockResolvedValue(mockFoods);

      await foodController.searchFoods(mockReq, mockRes);

      expect(mockFoodService.query).toHaveBeenCalled();
      expect(mockRes.json).toHaveBeenCalledWith({
        success: true,
        data: mockFoods,
        message: "Food search completed",
      });
    });

    test("should return 400 when no search term provided", async () => {
      mockReq.query = {};

      await foodController.searchFoods(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(400);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        message: "Search term is required",
      });
    });

    test("should handle errors when searching foods", async () => {
      const searchTerm = "chicken";
      const error = new Error("Search error");

      mockReq.query.name = searchTerm;
      mockFoodService.query.mockRejectedValue(error);

      await foodController.searchFoods(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        error: "Search error",
        message: "Failed to search foods",
      });
    });
  });
});
