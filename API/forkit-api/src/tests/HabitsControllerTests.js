const request = require("supertest");
const app = require("../server");
const { db } = require("../config/firebase");

describe("Habits Controller Tests", () => {
  let testUserId = "test-user-habits-123";
  let testHabitId;

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

    if (testHabitId) {
      await db.collection("habits").doc(testHabitId).delete();
    }
  });

  describe("GET /api/habits/daily/:userId", () => {
    it("should return daily habits for user", async () => {
      const res = await request(app).get(`/api/habits/daily/${testUserId}`);
      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(Array.isArray(res.body.data)).toBe(true);
    });

    it("should return 404 for missing userId", async () => {
      const res = await request(app).get(`/api/habits/daily/`);
      expect(res.status).toBe(404);
      expect(res.body.error).toBe("Endpoint not found");
    });
  });

  describe("GET /api/habits/weekly/:userId", () => {
    it("should return weekly habits for user", async () => {
      const res = await request(app).get(`/api/habits/weekly/${testUserId}`);
      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(Array.isArray(res.body.data)).toBe(true);
    });

    it("should return 404 for missing userId", async () => {
      const res = await request(app).get(`/api/habits/weekly/`);
      expect(res.status).toBe(404);
      expect(res.body.error).toBe("Endpoint not found");
    });
  });

  describe("GET /api/habits/monthly/:userId", () => {
    it("should return monthly habits for user", async () => {
      const res = await request(app).get(`/api/habits/monthly/${testUserId}`);
      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(Array.isArray(res.body.data)).toBe(true);
    });

    it("should return 404 for missing userId", async () => {
      const res = await request(app).get(`/api/habits/monthly/`);
      expect(res.status).toBe(404);
      expect(res.body.error).toBe("Endpoint not found");
    });
  });

  describe("POST /api/habits/", () => {
    it("should create habit successfully", async () => {
      const habitData = {
        userId: testUserId,
        habit: {
          category: "GENERAL",
          title: "Drink Water",
          description: "Drink 8 glasses of water daily",
          frequency: "daily",
          targetValue: 8,
          unit: "glasses",
        },
      };

      const res = await request(app).post("/api/habits").send(habitData);
      expect(res.status).toBe(201);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      testHabitId = res.body.data.id;
    });

    it("should return 400 for missing userId", async () => {
      const habitData = {
        habit: { title: "Test Habit" },
      };

      const res = await request(app).post("/api/habits").send(habitData);

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });

    it("should return 400 for missing habit title", async () => {
      const habitData = {
        userId: testUserId,
        habit: { title: "" },
      };

      const res = await request(app).post("/api/habits").send(habitData);

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });
  });

  describe("PUT /api/habits/:id", () => {
    it("should update habit successfully", async () => {
      if (!testHabitId) return; // Skip if no habit was created

      const updateData = {
        title: "Updated Habit",
        description: "Updated description",
      };

      const res = await request(app)
        .put(`/api/habits/${testHabitId}`)
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
    });
  });

  describe("PUT /api/habits/:id", () => {
    it("should update habit successfully", async () => {
      if (!testHabitId) return; // Skip if no habit was created

      const updateData = {
        title: "Updated Habit",
        description: "Updated description",
      };

      const res = await request(app)
        .put(`/api/habits/${testHabitId}`)
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
    });

    it("should complete habit successfully", async () => {
      if (!testHabitId) return; // Skip if no habit was created

      const res = await request(app)
        .put(`/api/habits/${testHabitId}`)
        .send({ isCompleted: true });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toContain("completed successfully");
    });

    it("should uncomplete habit successfully", async () => {
      if (!testHabitId) return; // Skip if no habit was created

      const res = await request(app)
        .put(`/api/habits/${testHabitId}`)
        .send({ isCompleted: false });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toContain("uncompleted successfully");
    });

    it("should return 500 for non-existent habit (Firebase update error)", async () => {
      const res = await request(app)
        .put("/api/habits/non-existent-id")
        .send({ title: "Updated" });

      expect(res.status).toBe(500);
      expect(res.body.success).toBe(false);
    });
  });

  describe("DELETE /api/habits/:id", () => {
    it("should delete habit successfully", async () => {
      if (!testHabitId) return; // Skip if no habit was created

      const res = await request(app).delete(`/api/habits/${testHabitId}`);
      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
    });

    it("should return 200 for non-existent habit (Firebase idempotent delete)", async () => {
      const res = await request(app).delete("/api/habits/non-existent-id");

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
    });
  });
});
