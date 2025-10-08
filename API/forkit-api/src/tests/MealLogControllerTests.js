const request = require("supertest");
const app = require("../server");
const { db } = require("../config/firebase");

describe("Meal Log Controller Tests", () => {
  let testUserId = "test-user-meals-123";
  let testMealLogId;

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

    if (testMealLogId) {
      await db.collection("mealLogs").doc(testMealLogId).delete();
    }
  });

  describe("POST /api/meal-logs", () => {
    it("should create meal log successfully", async () => {
      const mealData = {
        userId: testUserId,
        name: "Test Meal",
        description: "A test meal",
        ingredients: [
          {
            name: "Apple",
            quantity: 1,
            unit: "piece",
            calories: 95,
          },
        ],
        instructions: ["Cut apple", "Serve"],
        totalCalories: 95,
        totalCarbs: 25,
        totalFat: 0.3,
        totalProtein: 0.5,
        servings: 1,
        date: new Date().toISOString().split("T")[0],
        mealType: "Breakfast",
      };

      const res = await request(app).post("/api/meal-logs").send(mealData);

      expect(res.status).toBe(201);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data.name).toBe(mealData.name);
      testMealLogId = res.body.data.id;
    });

    it("should return 400 for missing required fields", async () => {
      const res = await request(app)
        .post("/api/meal-logs")
        .send({ userId: testUserId }); // Missing name, ingredients, etc.

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain("required");
    });

    it("should return 400 for invalid ingredients array", async () => {
      const mealData = {
        userId: testUserId,
        name: "Test Meal",
        ingredients: [], // Empty array
        instructions: ["Test"],
        date: new Date().toISOString().split("T")[0],
      };

      const res = await request(app).post("/api/meal-logs").send(mealData);

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });

    it("should return 400 for invalid instructions array", async () => {
      const mealData = {
        userId: testUserId,
        name: "Test Meal",
        ingredients: [{ name: "Apple", quantity: 1 }],
        instructions: [], // Empty array
        date: new Date().toISOString().split("T")[0],
      };

      const res = await request(app).post("/api/meal-logs").send(mealData);

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });
  });

  describe("GET /api/meal-logs/:userId", () => {
    it("should return meal logs for user", async () => {
      const res = await request(app).get(`/api/meal-logs/user/${testUserId}`);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(Array.isArray(res.body.data)).toBe(true);
    });

    it("should return meal logs with date filter", async () => {
      const today = new Date().toISOString().split("T")[0];
      const res = await request(app)
        .get(`/api/meal-logs/user/${testUserId}`)
        .query({ date: today });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(Array.isArray(res.body.data)).toBe(true);
    });
  });

  describe("GET /api/meal-logs/:id", () => {
    it("should return specific meal log by ID", async () => {
      if (!testMealLogId) return;

      const res = await request(app).get(`/api/meal-logs/${testMealLogId}`);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data.id).toBe(testMealLogId);
    });

    it("should return 404 for non-existent meal log", async () => {
      const res = await request(app).get("/api/meal-logs/non-existent-id");

      expect(res.status).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe("Meal log not found");
    });
  });

  describe("PUT /api/meal-logs/:id", () => {
    it("should update meal log successfully", async () => {
      if (!testMealLogId) return;

      const updateData = {
        name: "Updated Meal",
        description: "Updated description",
      };

      const res = await request(app)
        .put(`/api/meal-logs/${testMealLogId}`)
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
    });

    it("should return 400 for missing meal log ID", async () => {
      const res = await request(app).put("/api/meal-logs/").send({});

      expect(res.status).toBe(404);
    });
  });

  describe("DELETE /api/meal-logs/:id", () => {
    it("should delete meal log successfully", async () => {
      if (!testMealLogId) return;

      const res = await request(app).delete(`/api/meal-logs/${testMealLogId}`);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe("Meal log deleted successfully");
    });

    it("should return 400 for missing meal log ID", async () => {
      const res = await request(app).delete("/api/meal-logs/");

      expect(res.status).toBe(404);
    });
  });

  describe("GET /api/meal-logs/date-range", () => {
    it("should return meal logs by date range", async () => {
      const startDate = new Date();
      startDate.setDate(startDate.getDate() - 7);
      const endDate = new Date();

      const res = await request(app)
        .get("/api/meal-logs/date-range")
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
        .get("/api/meal-logs/date-range")
        .query({ userId: testUserId }); // Missing startDate and endDate

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain("required");
    });
  });
});
