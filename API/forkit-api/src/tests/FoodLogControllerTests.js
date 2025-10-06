const request = require("supertest");
const app = require("../server");
const { db } = require("../config/firebase");

describe("Food Log Controller Tests", () => {
  let testUserId = "test-user-foodlogs-123";
  let testFoodLogId;

  beforeAll(async () => {
    // Seed test user
    await db.collection("users").add({
      userId: testUserId,
      email: "test@example.com",
      name: "Test User",
    });
  });

  afterAll(async () => {
    // Cleanup
    const users = await db
      .collection("users")
      .where("userId", "==", testUserId)
      .get();
    users.forEach((doc) => doc.ref.delete());

    if (testFoodLogId) {
      await db.collection("foodLogs").doc(testFoodLogId).delete();
    }
  });

  describe("POST /api/food-logs", () => {
    it("should create food log successfully", async () => {
      const foodLogData = {
        userId: testUserId,
        foodId: "test-food-123",
        foodName: "Apple",
        servingSize: 1,
        measuringUnit: "Cup",
        date: new Date().toISOString(),
        mealType: "Breakfast",
        calories: 95,
        logDate: new Date().toISOString(),
      };

      const res = await request(app).post("/api/food-logs").send(foodLogData);

      expect(res.status).toBe(201);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      testFoodLogId = res.body.data.id;
    });

    it("should return 400 for missing userId", async () => {
      const res = await request(app)
        .post("/api/food-logs")
        .send({ foodName: "Apple" });

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });
  });

  describe("GET /api/food-logs/:userId", () => {
    it("should return food logs for user", async () => {
      const res = await request(app).get(`/api/food-logs/user/${testUserId}`);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(Array.isArray(res.body.data)).toBe(true);
    });
    it("should return 404 for missing userId", async () => {
      const res = await request(app).get(`/api/food-logs/`);
      expect(res.status).toBe(404);
      expect(res.body.error).toBe("Endpoint not found");
    });
  });

  describe("PUT /api/food-logs/:id", () => {
    it("should update food log successfully", async () => {
      if (!testFoodLogId) return;

      const updateData = {
        quantity: 2,
        calories: 190,
      };

      const res = await request(app)
        .put(`/api/food-logs/${testFoodLogId}`)
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
    });
    it("should return 404 for missing food log id", async () => {
      const res = await request(app).put(`/api/food-logs/`);
      expect(res.status).toBe(404);
      expect(res.body.error).toBe("Endpoint not found");
    });
  });

  describe("GET /api/food-logs/:id", () => {
    it("should return specific food log by ID", async () => {
      if (!testFoodLogId) return;

      const res = await request(app).get(`/api/food-logs/${testFoodLogId}`);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data.id).toBe(testFoodLogId);
    });

    it("should return 404 for non-existent food log", async () => {
      const res = await request(app).get("/api/food-logs/non-existent-id");

      expect(res.status).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe("Food log not found");
    });
  });

  describe("DELETE /api/food-logs/:id", () => {
    it("should delete food log successfully", async () => {
      if (!testFoodLogId) return;

      const res = await request(app).delete(`/api/food-logs/${testFoodLogId}`);
      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
    });
  });

  describe("GET /api/food-logs/date-range", () => {
    it("should return food logs by date range", async () => {
      const startDate = new Date();
      startDate.setDate(startDate.getDate() - 7);
      const endDate = new Date();

      const res = await request(app)
        .get("/api/food-logs/date-range")
        .query({
          userId: testUserId,
          startDate: startDate.toISOString().split("T")[0],
          endDate: endDate.toISOString().split("T")[0],
        });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(Array.isArray(res.body.data)).toBe(true);
    });

    it("should return 400 for missing required parameters", async () => {
      const res = await request(app)
        .get("/api/food-logs/date-range")
        .query({ userId: testUserId }); // Missing startDate and endDate

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain("required");
    });
  });

  describe("GET /api/food-logs/daily-summary", () => {
    it("should return daily calorie summary", async () => {
      const today = new Date().toISOString().split("T")[0];
      const res = await request(app)
        .get("/api/food-logs/daily-summary")
        .query({ userId: testUserId, date: today });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data).toHaveProperty("totalCalories");
      expect(res.body.data).toHaveProperty("mealDistribution");
    });

    it("should return 400 for missing parameters", async () => {
      const res = await request(app).get("/api/food-logs/daily-summary");

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });
  });

  describe("GET /api/food-logs/monthly-summary", () => {
    it("should return monthly calorie summary", async () => {
      const currentYear = new Date().getFullYear();
      const currentMonth = new Date().getMonth() + 1;

      const res = await request(app)
        .get("/api/food-logs/monthly-summary")
        .query({ userId: testUserId, year: currentYear, month: currentMonth });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data).toHaveProperty("totalCalories");
      expect(res.body.data).toHaveProperty("averageDailyCalories");
    });
  });

  describe("GET /api/food-logs/recent-activity", () => {
    it("should return recent food activity", async () => {
      const res = await request(app)
        .get("/api/food-logs/recent-activity")
        .query({ userId: testUserId });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data).toHaveProperty("recentActivity");
      expect(Array.isArray(res.body.data.recentActivity)).toBe(true);
    });
  });

  describe("GET /api/food-logs/trends", () => {
    it("should return calorie trends", async () => {
      const startDate = new Date();
      startDate.setDate(startDate.getDate() - 30);
      const endDate = new Date();

      const res = await request(app)
        .get("/api/food-logs/trends")
        .query({
          userId: testUserId,
          startDate: startDate.toISOString().split("T")[0],
          endDate: endDate.toISOString().split("T")[0],
        });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data).toHaveProperty("trends");
      expect(Array.isArray(res.body.data.trends)).toBe(true);
    });
  });

  describe("GET /api/food-logs/dashboard", () => {
    it("should return food dashboard data", async () => {
      const res = await request(app)
        .get("/api/food-logs/dashboard")
        .query({ userId: testUserId });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data).toHaveProperty("daily");
      expect(res.body.data).toHaveProperty("monthly");
      expect(res.body.data).toHaveProperty("recentActivity");
    });
  });
});
