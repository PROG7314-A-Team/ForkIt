const request = require("supertest");
const app = require("../server");
const { db } = require("../config/firebase");

describe("Goals Controller Tests", () => {
  let testUserId = "test-user-goals-123";
  let testGoalId;

  beforeAll(async () => {
    // Create user document with userId as the document ID
    await db.collection("users").doc(testUserId).set({
      userId: testUserId,
      email: "test@example.com",
      name: "Test User",
    });
  });

  afterAll(async () => {
    // Delete the user document
    await db.collection("users").doc(testUserId).delete();

    if (testGoalId) {
      await db.collection("goals").doc(testGoalId).delete();
    }
  });

  describe("GET /api/users/:userId/goals", () => {
    it("should return user goals", async () => {
      const res = await request(app).get(`/api/users/${testUserId}/goals`);
      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
    });
  });

  describe("PUT /api/users/:userId/goals", () => {
    it("should update user goals", async () => {
      const goalsData = {
        dailyCalories: 2000,
        dailySteps: 10000,
        dailyWater: 2000,
      };

      const res = await request(app)
        .put(`/api/users/${testUserId}/goals`)
        .send(goalsData);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data.dailyCalories).toBe(2000);
    });

    it("should return 500 for missing user ID", async () => {
      const res = await request(app)
        .put("/api/users//goals")
        .send({ dailyCalories: 2000 });

      expect(res.status).toBe(500);
    });

    it("should return 400 for missing goals data", async () => {
      const res = await request(app)
        .put(`/api/users/${testUserId}/goals`)
        .send({});

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain("Goals data is required");
    });

    it("should return 400 for invalid daily calories", async () => {
      const res = await request(app)
        .put(`/api/users/${testUserId}/goals`)
        .send({ dailyCalories: 500 }); // Below minimum

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain("between 1200 and 10000");
    });

    it("should return 400 for invalid daily water", async () => {
      const res = await request(app)
        .put(`/api/users/${testUserId}/goals`)
        .send({ dailyWater: 100 }); // Below minimum

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain("between 500 and 10000");
    });

    it("should return 400 for invalid daily steps", async () => {
      const res = await request(app)
        .put(`/api/users/${testUserId}/goals`)
        .send({ dailySteps: -100 }); // Negative value

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain("between 0 and 50000");
    });

    it("should return 400 for invalid weekly exercises", async () => {
      const res = await request(app)
        .put(`/api/users/${testUserId}/goals`)
        .send({ weeklyExercises: 25 }); // Above maximum

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain("between 0 and 21");
    });

    it("should update partial goals successfully", async () => {
      const res = await request(app)
        .put(`/api/users/${testUserId}/goals`)
        .send({ dailyCalories: 2500 }); // Only update one goal

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.dailyCalories).toBe(2500);
    });

    it("should return 404 for non-existent user", async () => {
      const res = await request(app)
        .put("/api/users/non-existent-user/goals")
        .send({ dailyCalories: 2000 });

      expect(res.status).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe("User not found");
    });
  });
});
