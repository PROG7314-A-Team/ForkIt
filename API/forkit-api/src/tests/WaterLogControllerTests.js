const request = require("supertest");
const app = require("../server");
const { db } = require("../config/firebase");

describe("Water Log Controller Tests", () => {
  let testUserId = "test-user-water-123";
  let testWaterLogId;

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

    if (testWaterLogId) {
      await db.collection("waterLogs").doc(testWaterLogId).delete();
    }
  });

  describe("POST /api/water-logs", () => {
    it("should create water log successfully", async () => {
      const waterData = {
        userId: testUserId,
        amount: 250,
        date: new Date().toISOString().split("T")[0],
      };

      const res = await request(app).post("/api/water-logs").send(waterData);

      expect(res.status).toBe(201);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data.amount).toBe(250);
      testWaterLogId = res.body.data.id;
    });

    it("should return 400 for missing required fields", async () => {
      const res = await request(app)
        .post("/api/water-logs")
        .send({ userId: testUserId }); // Missing amount and date

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain("required");
    });

    it("should return 400 for invalid amount", async () => {
      const res = await request(app)
        .post("/api/water-logs")
        .send({
          userId: testUserId,
          amount: -100, // Negative amount
          date: new Date().toISOString().split("T")[0],
        });

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain("positive number");
    });

    it("should return 400 for zero amount", async () => {
      const res = await request(app)
        .post("/api/water-logs")
        .send({
          userId: testUserId,
          amount: 0,
          date: new Date().toISOString().split("T")[0],
        });

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });
  });

  describe("GET /api/water-logs/:userId", () => {
    it("should return water logs for user", async () => {
      const res = await request(app).get(`/api/water-logs/user/${testUserId}`);
      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(Array.isArray(res.body.data)).toBe(true);
    });

    it("should return water logs with date filter", async () => {
      const today = new Date().toISOString().split("T")[0];
      const res = await request(app)
        .get(`/api/water-logs/user/${testUserId}`)
        .query({ date: today });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(Array.isArray(res.body.data)).toBe(true);
    });
  });

  describe("GET /api/water-logs/:id", () => {
    it("should return specific water log by ID", async () => {
      if (!testWaterLogId) return;

      const res = await request(app).get(`/api/water-logs/${testWaterLogId}`);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data.id).toBe(testWaterLogId);
    });

    it("should return 404 for non-existent water log", async () => {
      const res = await request(app).get("/api/water-logs/non-existent-id");

      expect(res.status).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe("Water log not found");
    });
  });

  describe("PUT /api/water-logs/:id", () => {
    it("should update water log successfully", async () => {
      if (!testWaterLogId) return;

      const updateData = {
        amount: 300,
      };

      const res = await request(app)
        .put(`/api/water-logs/${testWaterLogId}`)
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
    });

    it("should return 400 for invalid amount in update", async () => {
      if (!testWaterLogId) return;

      const res = await request(app)
        .put(`/api/water-logs/${testWaterLogId}`)
        .send({ amount: -50 });

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });

    it("should return 400 for missing water log ID", async () => {
      const res = await request(app).put("/api/water-logs/").send({});

      expect(res.status).toBe(404);
    });
  });

  describe("DELETE /api/water-logs/:id", () => {
    it("should delete water log successfully", async () => {
      if (!testWaterLogId) return;

      const res = await request(app).delete(
        `/api/water-logs/${testWaterLogId}`
      );

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe("Water log deleted successfully");
    });

    it("should return 400 for missing water log ID", async () => {
      const res = await request(app).delete("/api/water-logs/");

      expect(res.status).toBe(404);
    });
  });

  describe("GET /api/water-logs/date-range", () => {
    it("should return water logs by date range", async () => {
      const startDate = new Date();
      startDate.setDate(startDate.getDate() - 7);
      const endDate = new Date();

      const res = await request(app)
        .get("/api/water-logs/date-range")
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
        .get("/api/water-logs/date-range")
        .query({ userId: testUserId }); // Missing startDate and endDate

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain("required");
    });
  });

  describe("GET /api/water-logs/daily-total", () => {
    it("should return daily water total", async () => {
      const today = new Date().toISOString().split("T")[0];
      const res = await request(app)
        .get("/api/water-logs/daily-total")
        .query({ userId: testUserId, date: today });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data).toHaveProperty("totalAmount");
      expect(res.body.data).toHaveProperty("entries");
    });

    it("should return 400 for missing parameters", async () => {
      const res = await request(app).get("/api/water-logs/daily-total");

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });
  });

  describe("GET /api/water-logs/daily-summary", () => {
    it("should return daily water summary", async () => {
      const today = new Date().toISOString().split("T")[0];
      const res = await request(app)
        .get("/api/water-logs/daily-summary")
        .query({ userId: testUserId, date: today });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data).toHaveProperty("totalAmount");
      expect(res.body.data).toHaveProperty("goal");
      expect(res.body.data).toHaveProperty("remaining");
    });
  });

  describe("GET /api/water-logs/monthly-summary", () => {
    it("should return monthly water summary", async () => {
      const currentYear = new Date().getFullYear();
      const currentMonth = new Date().getMonth() + 1;

      const res = await request(app)
        .get("/api/water-logs/monthly-summary")
        .query({ userId: testUserId, year: currentYear, month: currentMonth });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data).toHaveProperty("totalAmount");
      expect(res.body.data).toHaveProperty("averageDailyAmount");
    });
  });

  describe("GET /api/water-logs/recent-activity", () => {
    it("should return recent water activity", async () => {
      const res = await request(app)
        .get("/api/water-logs/recent-activity")
        .query({ userId: testUserId });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data).toHaveProperty("recentActivity");
      expect(Array.isArray(res.body.data.recentActivity)).toBe(true);
    });
  });

  describe("GET /api/water-logs/trends", () => {
    it("should return water trends", async () => {
      const startDate = new Date();
      startDate.setDate(startDate.getDate() - 30);
      const endDate = new Date();

      const res = await request(app)
        .get("/api/water-logs/trends")
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

  describe("GET /api/water-logs/dashboard", () => {
    it("should return water dashboard data", async () => {
      const res = await request(app)
        .get("/api/water-logs/dashboard")
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
