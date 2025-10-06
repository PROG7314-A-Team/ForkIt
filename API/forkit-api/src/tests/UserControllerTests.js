const request = require("supertest");
const app = require("../server");
const { db } = require("../config/firebase");

describe("User Controller Tests", () => {
  let testUserId;

  beforeAll(async () => {
    // Seed test data
    const testUser = {
      userId: "test-user-123",
      email: "test@example.com",
      name: "Test User",
      age: 25,
      height: 170,
      weight: 70,
      createdAt: new Date().toISOString(),
    };

    const docRef = await db.collection("users").add(testUser);
    testUserId = docRef.id;
  });

  afterAll(async () => {
    // Cleanup test data
    if (testUserId) {
      await db.collection("users").doc(testUserId).delete();
    }
  });

  describe("GET /api/users", () => {
    it("should return all users from users collection", async () => {
      const res = await request(app).get("/api/users/");
      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(Array.isArray(res.body.data)).toBe(true);
    });

    it("should handle database errors gracefully", async () => {
      // Mock database error
      const originalGetAll = require("../services/firebaseService").prototype
        .getAll;
      require("../services/firebaseService").prototype.getAll = jest
        .fn()
        .mockRejectedValue(new Error("Database error"));

      const res = await request(app).get("/api/users/");
      expect(res.status).toBe(500);
      expect(res.body.success).toBe(false);

      // Restore original method
      require("../services/firebaseService").prototype.getAll = originalGetAll;
    });
  });

  describe("GET /api/users/:id", () => {
    it("should return user by ID", async () => {
      const res = await request(app).get("/api/users/test-user-123");
      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data[0].userId).toBe("test-user-123");
    });

    it("should return 404 for non-existent user", async () => {
      const res = await request(app).get("/api/users/non-existent-user");
      expect(res.status).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe("User not found");
    });
  });

  describe("GET /api/users/:id/streak", () => {
    it("should return user streak data", async () => {
      const res = await request(app).get("/api/users/test-user-123/streak");
      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data.userId).toBe("test-user-123");
      expect(res.body.data).toHaveProperty("currentStreak");
      expect(res.body.data).toHaveProperty("longestStreak");
      expect(res.body.data).toHaveProperty("isActive");
    });

    it("should return 404 for non-existent user streak", async () => {
      const res = await request(app).get("/api/users/non-existent-user/streak");
      expect(res.status).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe("User not found");
    });
  });

  describe("PUT /api/users/:id", () => {
    it("should update user successfully", async () => {
      const updateData = {
        name: "Updated Test User",
        age: 26,
        email: "updated@example.com",
      };

      const res = await request(app)
        .put(`/api/users/${testUserId}`)
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.message).toBe("User updated successfully");
    });

    it("should return 400 for missing user data", async () => {
      const res = await request(app).put(`/api/users/${testUserId}`).send({});

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe("User data is required");
    });
  });

  describe("PUT /api/users/:id/profile", () => {
    it("should update user profile successfully", async () => {
      const profileData = {
        age: 30,
        height: 175,
        weight: 75,
      };

      const res = await request(app)
        .put(`/api/users/${testUserId}/profile`)
        .send(profileData);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.message).toBe("User profile updated successfully");
    });

    it("should return 400 for missing required fields", async () => {
      const res = await request(app)
        .put(`/api/users/${testUserId}/profile`)
        .send({ age: 25 }); // Missing height and weight

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe("Age, height, and weight are required");
    });

    it("should return 400 for invalid age", async () => {
      const res = await request(app)
        .put(`/api/users/${testUserId}/profile`)
        .send({ age: 150, height: 170, weight: 70 });

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe("Age must be between 1 and 120");
    });

    it("should return 400 for invalid height", async () => {
      const res = await request(app)
        .put(`/api/users/${testUserId}/profile`)
        .send({ age: 25, height: 10, weight: 70 });

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe("Height must be between 50 and 250 cm");
    });

    it("should return 400 for invalid weight", async () => {
      const res = await request(app)
        .put(`/api/users/${testUserId}/profile`)
        .send({ age: 25, height: 170, weight: 5 });

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe("Weight must be between 20 and 300 kg");
    });
  });

  describe("DELETE /api/users/:id", () => {
    it("should delete user successfully", async () => {
      // Create a separate test user for deletion
      const deleteTestUser = {
        userId: "delete-test-user",
        email: "delete@example.com",
        name: "Delete Test User",
      };

      const docRef = await db.collection("users").add(deleteTestUser);
      const deleteUserId = docRef.id;

      const res = await request(app).delete(`/api/users/${deleteUserId}`);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe("User deleted successfully");
    });

    it("should handle deletion of non-existent user", async () => {
      const res = await request(app).delete("/api/users/non-existent-id");

      expect(res.status).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe("User not found");
    });
  });
});
