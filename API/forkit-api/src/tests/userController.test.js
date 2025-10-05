// Mock dependencies first
const mockFirebaseService = jest.fn();
const mockStreakService = jest.fn();
jest.mock("../services/firebaseService", () => mockFirebaseService);
jest.mock("../services/streakService", () => mockStreakService);

const userController = require("../controllers/userController");

describe("UserController", () => {
  let mockUserService;
  let mockStreakService;
  let mockReq;
  let mockRes;

  beforeEach(() => {
    // Reset mocks
    jest.clearAllMocks();

    // Setup mock services
    mockUserService = {
      getAll: jest.fn(),
      getById: jest.fn(),
      query: jest.fn(),
      update: jest.fn(),
      delete: jest.fn(),
    };
    mockStreakService = {
      updateUserStreak: jest.fn(),
    };

    // Setup mock request and response
    mockReq = {
      params: {},
      body: {},
    };
    mockRes = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn(),
    };

    // Mock service constructors
    mockFirebaseService.mockImplementation(() => mockUserService);
    // Note: StreakService is not used in userController, so we don't need to mock it
  });

  describe("getUser", () => {
    test("should return all users successfully", async () => {
      const mockUsers = [
        { id: "1", name: "User 1", email: "user1@example.com" },
        { id: "2", name: "User 2", email: "user2@example.com" },
      ];

      mockUserService.getAll.mockResolvedValue(mockUsers);

      await userController.getUser(mockReq, mockRes);

      expect(mockUserService.getAll).toHaveBeenCalled();
      expect(mockRes.json).toHaveBeenCalledWith({
        success: true,
        data: mockUsers,
        message: "Users retrieved successfully",
      });
    });

    test("should handle errors when retrieving users", async () => {
      const error = new Error("Database error");
      mockUserService.getAll.mockRejectedValue(error);

      await userController.getUser(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        error: "Database error",
        message: "Failed to retrieve users",
      });
    });
  });

  describe("getUserById", () => {
    test("should return user by ID successfully", async () => {
      const userId = "user123";
      const mockUser = {
        id: "user123",
        name: "Test User",
        email: "test@example.com",
      };

      mockReq.params.id = userId;
      mockUserService.query.mockResolvedValue([mockUser]);

      await userController.getUserById(mockReq, mockRes);

      expect(mockUserService.query).toHaveBeenCalledWith([
        { field: "userId", operator: "==", value: userId },
      ]);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: true,
        data: [mockUser],
        message: "User retrieved successfully",
      });
    });

    test("should return 404 when user not found", async () => {
      const userId = "non-existent";

      mockReq.params.id = userId;
      mockUserService.query.mockResolvedValue([]);

      await userController.getUserById(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(404);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        message: "User not found",
      });
    });

    test("should handle errors when retrieving user by ID", async () => {
      const userId = "user123";
      const error = new Error("Query error");

      mockReq.params.id = userId;
      mockUserService.query.mockRejectedValue(error);

      await userController.getUserById(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        error: "Query error",
        message: "Failed to retrieve user",
      });
    });
  });

  describe("getUserStreak", () => {
    test("should return user streak data successfully", async () => {
      const userId = "user123";
      const mockUser = {
        id: "user123",
        userId: "user123",
        streakData: {
          currentStreak: 5,
          longestStreak: 10,
          lastLogDate: "2025-10-05",
          streakStartDate: "2025-10-01",
          isActive: true,
        },
      };

      mockReq.params.id = userId;
      mockUserService.query.mockResolvedValue([mockUser]);

      await userController.getUserStreak(mockReq, mockRes);

      expect(mockUserService.query).toHaveBeenCalledWith([
        { field: "userId", operator: "==", value: userId },
      ]);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: true,
        data: mockUser.streakData,
        message: "User streak retrieved successfully",
      });
    });

    test("should return 404 when user not found for streak", async () => {
      const userId = "non-existent";

      mockReq.params.id = userId;
      mockUserService.query.mockResolvedValue([]);

      await userController.getUserStreak(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(404);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        message: "User not found",
      });
    });

    test("should handle errors when retrieving user streak", async () => {
      const userId = "user123";
      const error = new Error("Streak query error");

      mockReq.params.id = userId;
      mockUserService.query.mockRejectedValue(error);

      await userController.getUserStreak(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        error: "Streak query error",
        message: "Failed to retrieve user streak",
      });
    });
  });

  describe("updateUser", () => {
    test("should update user successfully", async () => {
      const userId = "user123";
      const updateData = { name: "Updated User", age: 30 };
      const mockUpdatedUser = {
        id: "user123",
        userId: "user123",
        ...updateData,
      };

      mockReq.params.id = userId;
      mockReq.body = updateData;
      mockUserService.query.mockResolvedValue([{ id: "user123" }]);
      mockUserService.update.mockResolvedValue(mockUpdatedUser);

      await userController.updateUser(mockReq, mockRes);

      expect(mockUserService.update).toHaveBeenCalledWith(
        "user123",
        updateData
      );
      expect(mockRes.json).toHaveBeenCalledWith({
        success: true,
        data: mockUpdatedUser,
        message: "User updated successfully",
      });
    });

    test("should return 404 when user not found for update", async () => {
      const userId = "non-existent";
      const updateData = { name: "Updated User" };

      mockReq.params.id = userId;
      mockReq.body = updateData;
      mockUserService.query.mockResolvedValue([]);

      await userController.updateUser(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(404);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        message: "User not found",
      });
    });

    test("should handle errors when updating user", async () => {
      const userId = "user123";
      const updateData = { name: "Updated User" };
      const error = new Error("Update error");

      mockReq.params.id = userId;
      mockReq.body = updateData;
      mockUserService.query.mockResolvedValue([{ id: "user123" }]);
      mockUserService.update.mockRejectedValue(error);

      await userController.updateUser(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        error: "Update error",
        message: "Failed to update user",
      });
    });
  });

  describe("deleteUser", () => {
    test("should delete user successfully", async () => {
      const userId = "user123";
      const mockUser = { id: "user123", name: "Test User" };

      mockReq.params.id = userId;
      mockUserService.query.mockResolvedValue([mockUser]);
      mockUserService.delete.mockResolvedValue(true);

      await userController.deleteUser(mockReq, mockRes);

      expect(mockUserService.delete).toHaveBeenCalledWith("user123");
      expect(mockRes.json).toHaveBeenCalledWith({
        success: true,
        data: mockUser,
        message: "User deleted successfully",
      });
    });

    test("should return 404 when user not found for deletion", async () => {
      const userId = "non-existent";

      mockReq.params.id = userId;
      mockUserService.query.mockResolvedValue([]);

      await userController.deleteUser(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(404);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        message: "User not found",
      });
    });

    test("should handle errors when deleting user", async () => {
      const userId = "user123";
      const error = new Error("Delete error");

      mockReq.params.id = userId;
      mockUserService.query.mockResolvedValue([{ id: "user123" }]);
      mockUserService.delete.mockRejectedValue(error);

      await userController.deleteUser(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: false,
        error: "Delete error",
        message: "Failed to delete user",
      });
    });
  });
});
