// Mock dependencies first
const mockFirebaseService = jest.fn();
jest.mock("../services/firebaseService", () => mockFirebaseService);

const habitsController = require("../controllers/habitsController");

describe("HabitsController", () => {
  let mockHabitsService;
  let mockReq;
  let mockRes;

  beforeEach(() => {
    // Reset mocks
    jest.clearAllMocks();

    // Setup mock service
    mockHabitsService = {
      getByUserId: jest.fn(),
      create: jest.fn(),
      update: jest.fn(),
      delete: jest.fn(),
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

    // Mock service constructor
    mockFirebaseService.mockImplementation(() => mockHabitsService);
  });

  describe("getDailyHabits", () => {
    test("should return daily habits successfully", async () => {
      const userId = "user123";
      const mockHabits = [
        {
          id: "1",
          title: "Drink Water",
          frequency: "daily",
          isCompleted: false,
        },
        { id: "2", title: "Exercise", frequency: "daily", isCompleted: true },
      ];

      mockReq.params.userId = userId;
      mockHabitsService.getByUserId.mockResolvedValue(mockHabits);

      await habitsController.getDailyHabits(mockReq, mockRes);

      expect(mockHabitsService.getByUserId).toHaveBeenCalledWith(userId);
      expect(mockRes.json).toHaveBeenCalledWith(mockHabits);
    });

    test("should return 400 when userId is missing", async () => {
      mockReq.params = {};

      await habitsController.getDailyHabits(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(400);
      expect(mockRes.json).toHaveBeenCalledWith({
        error: "User ID is required",
      });
    });

    test("should handle errors when retrieving daily habits", async () => {
      const userId = "user123";
      const error = new Error("Database error");

      mockReq.params.userId = userId;
      mockHabitsService.getByUserId.mockRejectedValue(error);

      await habitsController.getDailyHabits(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({ error: "Database error" });
    });
  });

  describe("getWeeklyHabits", () => {
    test("should return weekly habits successfully", async () => {
      const userId = "user123";
      const mockHabits = [
        {
          id: "1",
          title: "Weekly Review",
          frequency: "weekly",
          isCompleted: false,
        },
      ];

      mockReq.params.userId = userId;
      mockHabitsService.getByUserId.mockResolvedValue(mockHabits);

      await habitsController.getWeeklyHabits(mockReq, mockRes);

      expect(mockHabitsService.getByUserId).toHaveBeenCalledWith(userId);
      expect(mockRes.json).toHaveBeenCalledWith(mockHabits);
    });

    test("should return 400 when userId is missing", async () => {
      mockReq.params = {};

      await habitsController.getWeeklyHabits(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(400);
      expect(mockRes.json).toHaveBeenCalledWith({
        error: "User ID is required",
      });
    });

    test("should handle errors when retrieving weekly habits", async () => {
      const userId = "user123";
      const error = new Error("Database error");

      mockReq.params.userId = userId;
      mockHabitsService.getByUserId.mockRejectedValue(error);

      await habitsController.getWeeklyHabits(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({ error: "Database error" });
    });
  });

  describe("getMonthlyHabits", () => {
    test("should return monthly habits successfully", async () => {
      const userId = "user123";
      const mockHabits = [
        {
          id: "1",
          title: "Monthly Goal Review",
          frequency: "monthly",
          isCompleted: false,
        },
      ];

      mockReq.params.userId = userId;
      mockHabitsService.getByUserId.mockResolvedValue(mockHabits);

      await habitsController.getMonthlyHabits(mockReq, mockRes);

      expect(mockHabitsService.getByUserId).toHaveBeenCalledWith(userId);
      expect(mockRes.json).toHaveBeenCalledWith(mockHabits);
    });

    test("should return 400 when userId is missing", async () => {
      mockReq.params = {};

      await habitsController.getMonthlyHabits(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(400);
      expect(mockRes.json).toHaveBeenCalledWith({
        error: "User ID is required",
      });
    });

    test("should handle errors when retrieving monthly habits", async () => {
      const userId = "user123";
      const error = new Error("Database error");

      mockReq.params.userId = userId;
      mockHabitsService.getByUserId.mockRejectedValue(error);

      await habitsController.getMonthlyHabits(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({ error: "Database error" });
    });
  });

  describe("createHabit", () => {
    test("should create habit successfully", async () => {
      const userId = "user123";
      const habitData = {
        title: "New Habit",
        description: "A new daily habit",
        frequency: "daily",
        category: "health",
      };
      const mockCreatedHabit = { id: "habit123", userId, ...habitData };

      mockReq.body = { userId, habit: habitData };
      mockHabitsService.create.mockResolvedValue(mockCreatedHabit);

      await habitsController.createHabit(mockReq, mockRes);

      expect(mockHabitsService.create).toHaveBeenCalledWith({
        userId,
        habit: habitData,
      });
      expect(mockRes.json).toHaveBeenCalledWith(mockCreatedHabit);
    });

    test("should return 400 when userId is missing", async () => {
      mockReq.body = { habit: { title: "New Habit" } };

      await habitsController.createHabit(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(400);
      expect(mockRes.json).toHaveBeenCalledWith({
        error: "User ID is required",
      });
    });

    test("should return 400 when habit data is missing", async () => {
      mockReq.body = { userId: "user123" };

      await habitsController.createHabit(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(400);
      expect(mockRes.json).toHaveBeenCalledWith({ error: "Habit is required" });
    });

    test("should handle errors when creating habit", async () => {
      const userId = "user123";
      const habitData = { title: "New Habit" };
      const error = new Error("Creation error");

      mockReq.body = { userId, habit: habitData };
      mockHabitsService.create.mockRejectedValue(error);

      await habitsController.createHabit(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({ error: "Creation error" });
    });
  });

  describe("updateHabit", () => {
    test("should update habit successfully", async () => {
      const habitId = "habit123";
      const updateData = {
        title: "Updated Habit",
        isCompleted: true,
      };
      const mockUpdatedHabit = { id: habitId, ...updateData };

      mockReq.params.id = habitId;
      mockReq.body = updateData;
      mockHabitsService.update.mockResolvedValue(mockUpdatedHabit);

      await habitsController.updateHabit(mockReq, mockRes);

      expect(mockHabitsService.update).toHaveBeenCalledWith(
        habitId,
        updateData
      );
      expect(mockRes.json).toHaveBeenCalledWith(mockUpdatedHabit);
    });

    test("should handle errors when updating habit", async () => {
      const habitId = "habit123";
      const updateData = { title: "Updated Habit" };
      const error = new Error("Update error");

      mockReq.params.id = habitId;
      mockReq.body = updateData;
      mockHabitsService.update.mockRejectedValue(error);

      await habitsController.updateHabit(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({ error: "Update error" });
    });
  });

  describe("deleteHabit", () => {
    test("should delete habit successfully", async () => {
      const habitId = "habit123";

      mockReq.params.id = habitId;
      mockHabitsService.delete.mockResolvedValue(true);

      await habitsController.deleteHabit(mockReq, mockRes);

      expect(mockHabitsService.delete).toHaveBeenCalledWith(habitId);
      expect(mockRes.json).toHaveBeenCalledWith({
        success: true,
        message: "Habit deleted successfully",
      });
    });

    test("should handle errors when deleting habit", async () => {
      const habitId = "habit123";
      const error = new Error("Delete error");

      mockReq.params.id = habitId;
      mockHabitsService.delete.mockRejectedValue(error);

      await habitsController.deleteHabit(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(500);
      expect(mockRes.json).toHaveBeenCalledWith({ error: "Delete error" });
    });
  });
});
