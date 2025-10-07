const request = require("supertest");
const app = require("../server");
const { auth, db } = require("../config/firebase");

describe("Auth Controller Tests", () => {
  let testUserUid;
  const testEmail = "test@example.com";
  const testPassword = "password123";

  beforeAll(async () => {
    // Create a test user for login tests
    try {
      const userRecord = await auth.createUser({
        email: testEmail,
        password: testPassword,
      });
      testUserUid = userRecord.uid;

      // Create user document in Firestore
      await db
        .collection("users")
        .doc(testUserUid)
        .set({
          userId: testUserUid,
          email: testEmail,
          name: "Test User",
          streakData: {
            currentStreak: 0,
            longestStreak: 0,
            lastLogDate: null,
            streakStartDate: null,
          },
          goals: {
            dailyCalories: 2000,
            dailyWater: 2000,
            dailySteps: 8000,
            weeklyExercises: 3,
          },
          createdAt: new Date().toISOString(),
          goalsUpdatedAt: new Date().toISOString(),
        });
    } catch (error) {
      console.error("Error setting up test user:", error);
    }
  });

  afterAll(async () => {
    // Clean up test user
    if (testUserUid) {
      try {
        await auth.deleteUser(testUserUid);
        await db.collection("users").doc(testUserUid).delete();
      } catch (error) {
        console.error("Error cleaning up test user:", error);
      }
    }
  });

  describe("POST /api/users/register", () => {
    it("should register user successfully", async () => {
      // Generate unique email for this test
      const uniqueEmail = `newuser${Date.now()}@example.com`;
      const userData = {
        email: uniqueEmail,
        password: "password123",
        name: "New User",
      };

      const res = await request(app).post("/api/users/register").send(userData);

      expect(res.status).toBe(201);
      expect(res.body.message).toBe("User created successfully");
      expect(res.body.uid).toBeDefined();
      expect(res.body.email).toBe(userData.email);

      // Clean up the created user
      try {
        await auth.deleteUser(res.body.uid);
        await db.collection("users").doc(res.body.uid).delete();
      } catch (error) {
        console.error("Error cleaning up test user:", error);
      }
    });

    it("should return 400 for missing fields", async () => {
      const res = await request(app)
        .post("/api/users/register")
        .send({ email: "test@example.com" }); // Missing password

      expect(res.status).toBe(400);
      expect(res.body.message).toBe("Email and password are required");
    });
  });

  describe("POST /api/users/login", () => {
    it("should login user successfully", async () => {
      const loginData = {
        email: testEmail,
        password: testPassword,
      };

      const res = await request(app).post("/api/users/login").send(loginData);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe("Login successful");
      expect(res.body.userId).toBeDefined();
      expect(res.body.idToken).toBeDefined();
    });

    it("should return 401 for invalid credentials", async () => {
      const res = await request(app).post("/api/users/login").send({
        email: testEmail,
        password: "wrongpassword",
      });

      expect(res.status).toBe(401);
      expect(res.body.message).toBe("INVALID_LOGIN_CREDENTIALS");
    });
  });
});
