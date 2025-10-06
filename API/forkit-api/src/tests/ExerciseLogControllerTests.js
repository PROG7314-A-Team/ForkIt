const request = require("supertest");
const app = require("../server");
const { db } = require("../config/firebase");

describe("Exercise Log Controller Tests", () => {
  let testUserId = "test-user-exercise-123";
  let testExerciseLogId;

  beforeAll(async () => {
    await db.collection("users").add({
      userId: testUserId,
      email: "test@example.com",
      name: "Test User",
    });
  });

  afterAll(async () => {
    const users = await db
      .collection("users")
      .where("userId", "==", testUserId)
      .get();
    users.forEach((doc) => doc.ref.delete());

    if (testExerciseLogId) {
      await db.collection("exerciseLogs").doc(testExerciseLogId).delete();
    }
  });

  describe("POST /api/exercise-logs", () => {
    it("should create exercise log successfully", async () => {
      const exerciseData = {
        userId: testUserId,
        name: "Running",
        date: new Date().toISOString().split("T")[0],
        caloriesBurnt: 300,
        type: "Cardio",
        duration: 30,
        notes: "Morning run",
      };

      const res = await request(app)
        .post("/api/exercise-logs")
        .send(exerciseData);

      expect(res.status).toBe(201);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data.name).toBe("Running");
      testExerciseLogId = res.body.data.id;
    });

    it("should return 400 for missing required fields", async () => {
      const res = await request(app)
        .post("/api/exercise-logs")
        .send({ userId: testUserId }); // Missing other required fields

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain("required");
    });

    it("should return 400 for invalid exercise type", async () => {
      const exerciseData = {
        userId: testUserId,
        name: "Running",
        date: new Date().toISOString().split("T")[0],
        caloriesBurnt: 300,
        type: "InvalidType", // Invalid type
        duration: 30,
      };

      const res = await request(app)
        .post("/api/exercise-logs")
        .send(exerciseData);

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain("'Cardio' or 'Strength'");
    });

    it("should return 400 for negative calories", async () => {
      const exerciseData = {
        userId: testUserId,
        name: "Running",
        date: new Date().toISOString().split("T")[0],
        caloriesBurnt: -100, // Negative calories
        type: "Cardio",
        duration: 30,
      };

      const res = await request(app)
        .post("/api/exercise-logs")
        .send(exerciseData);

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });
  });

  describe("GET /api/exercise-logs/:userId", () => {
    it("should return exercise logs for user", async () => {
      const res = await request(app).get(
        `/api/exercise-logs/user/${testUserId}`
      );
      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(Array.isArray(res.body.data)).toBe(true);
    });

    it("should return exercise logs with date filter", async () => {
      const today = new Date().toISOString().split("T")[0];
      const res = await request(app)
        .get(`/api/exercise-logs/user/${testUserId}`)
        .query({ date: today });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(Array.isArray(res.body.data)).toBe(true);
    });

    it("should return exercise logs with type filter", async () => {
      const res = await request(app)
        .get(`/api/exercise-logs/user/${testUserId}`)
        .query({ type: "Cardio" });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(Array.isArray(res.body.data)).toBe(true);
    });
  });

  describe("GET /api/exercise-logs/:id", () => {
    it("should return specific exercise log by ID", async () => {
      if (!testExerciseLogId) return;

      const res = await request(app).get(
        `/api/exercise-logs/${testExerciseLogId}`
      );

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data.id).toBe(testExerciseLogId);
    });

    it("should return 404 for non-existent exercise log", async () => {
      const res = await request(app).get("/api/exercise-logs/non-existent-id");

      expect(res.status).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe("Exercise log not found");
    });
  });

  describe("PUT /api/exercise-logs/:id", () => {
    it("should update exercise log successfully", async () => {
      if (!testExerciseLogId) return;

      const updateData = {
        name: "Updated Running",
        caloriesBurnt: 350,
      };

      const res = await request(app)
        .put(`/api/exercise-logs/${testExerciseLogId}`)
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
    });

    it("should return 400 for invalid type in update", async () => {
      if (!testExerciseLogId) return;

      const res = await request(app)
        .put(`/api/exercise-logs/${testExerciseLogId}`)
        .send({ type: "InvalidType" });

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });

    it("should return 400 for negative calories in update", async () => {
      if (!testExerciseLogId) return;

      const res = await request(app)
        .put(`/api/exercise-logs/${testExerciseLogId}`)
        .send({ caloriesBurnt: -50 });

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });
  });

  describe("DELETE /api/exercise-logs/:id", () => {
    it("should delete exercise log successfully", async () => {
      if (!testExerciseLogId) return;

      const res = await request(app).delete(
        `/api/exercise-logs/${testExerciseLogId}`
      );

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe("Exercise log deleted successfully");
    });

    it("should return 400 for missing exercise log ID", async () => {
      const res = await request(app).delete("/api/exercise-logs/");

      expect(res.status).toBe(404);
    });
  });

  describe("GET /api/exercise-logs/date-range", () => {
    it("should return exercise logs by date range", async () => {
      const startDate = new Date();
      startDate.setDate(startDate.getDate() - 7);
      const endDate = new Date();

      const res = await request(app)
        .get("/api/exercise-logs/date-range")
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
        .get("/api/exercise-logs/date-range")
        .query({ userId: testUserId }); // Missing startDate and endDate

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain("required");
    });
  });

  describe("GET /api/exercise-logs/daily-total", () => {
    it("should return daily exercise total", async () => {
      const today = new Date().toISOString().split("T")[0];
      const res = await request(app)
        .get("/api/exercise-logs/daily-total")
        .query({ userId: testUserId, date: today });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data).toHaveProperty("totalCaloriesBurnt");
      expect(res.body.data).toHaveProperty("totalExercises");
    });

    it("should return 400 for missing parameters", async () => {
      const res = await request(app).get("/api/exercise-logs/daily-total");

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });
  });

  describe("GET /api/exercise-logs/daily-summary", () => {
    it("should return daily exercise summary", async () => {
      const today = new Date().toISOString().split("T")[0];
      const res = await request(app)
        .get("/api/exercise-logs/daily-summary")
        .query({ userId: testUserId, date: today });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data).toHaveProperty("totalCaloriesBurnt");
      expect(res.body.data).toHaveProperty("typeBreakdown");
    });
  });

  describe("GET /api/exercise-logs/monthly-summary", () => {
    it("should return monthly exercise summary", async () => {
      const currentYear = new Date().getFullYear();
      const currentMonth = new Date().getMonth() + 1;

      const res = await request(app)
        .get("/api/exercise-logs/monthly-summary")
        .query({ userId: testUserId, year: currentYear, month: currentMonth });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data).toHaveProperty("totalCalories");
      expect(res.body.data).toHaveProperty("averageDailyCalories");
    });
  });

  describe("GET /api/exercise-logs/recent-activity", () => {
    it("should return recent exercise activity", async () => {
      const res = await request(app)
        .get("/api/exercise-logs/recent-activity")
        .query({ userId: testUserId });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data).toHaveProperty("recentActivity");
      expect(Array.isArray(res.body.data.recentActivity)).toBe(true);
    });
  });

  describe("GET /api/exercise-logs/trends", () => {
    it("should return exercise trends", async () => {
      const startDate = new Date();
      startDate.setDate(startDate.getDate() - 30);
      const endDate = new Date();

      const res = await request(app)
        .get("/api/exercise-logs/trends")
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

  describe("GET /api/exercise-logs/dashboard", () => {
    it("should return exercise dashboard data", async () => {
      const res = await request(app)
        .get("/api/exercise-logs/dashboard")
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
