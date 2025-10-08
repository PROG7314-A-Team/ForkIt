const request = require("supertest");
const app = require("../server");
const { db } = require("../config/firebase");

describe("Food Controller Tests", () => {
  let testFoodId;

  afterAll(async () => {
    if (testFoodId) {
      await db.collection("food").doc(testFoodId).delete();
    }
  });

  describe("GET /api/food/barcode/:code", () => {
    it("should return food by valid barcode", async () => {
      // Use a known valid barcode for testing
      const res = await request(app).get("/api/food/barcode/3017620422003");
      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data.barcode).toBe("3017620422003");
      expect(res.body.data).toHaveProperty("name");
      expect(res.body.data).toHaveProperty("calories");
    });

    it("should return 404 for invalid barcode", async () => {
      const res = await request(app).get("/api/food/barcode/invalid-barcode");
      expect(res.status).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe("Food item not found for this barcode");
    });
  });

  describe("GET /api/food/:name", () => {
    it("should return food by name", async () => {
      const res = await request(app).get("/api/food/apple");
      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
    });

    it("should return 404 for non-existent food", async () => {
      const res = await request(app).get("/api/food/nonexistentfood123");
      expect(res.status).toBe(404);
      expect(res.body.success).toBe(false);
    });
  });

  describe("POST /api/food", () => {
    it("should create custom food successfully", async () => {
      const foodData = {
        name: "Test Food",
        brand: "Test Brand",
        calories: 100,
        nutrients: {
          carbs: 20,
          protein: 5,
          fat: 2,
        },
      };

      const res = await request(app).post("/api/food").send(foodData);

      expect(res.status).toBe(201);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      testFoodId = res.body.data.id;
    });

    it("should return 400 for missing required fields", async () => {
      const res = await request(app).post("/api/food").send({ name: "Test" }); // Missing calories

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });
  });

  describe("PUT /api/food/:id", () => {
    it("should update food successfully", async () => {
      const res = await request(app)
        .put(`/api/food/${testFoodId}`)
        .send({ name: "Updated Test Food" });
      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
    });

    it("should return 404 for missing food id", async () => {
      const res = await request(app)
        .put(`/api/food/nonexistentfood123`)
        .send({ name: "Updated Test Food" });
      expect(res.status).toBe(404);
      expect(res.body.success).toBe(false);
    });
  });
});
