const request = require("supertest");
const app = require("../server");

describe("Calorie Calculator Controller Tests", () => {
  describe("POST /api/calorie-calculator/calculate", () => {
    it("should calculate calories correctly", async () => {
      const calculationData = {
        carbs: 100,
        protein: 100,
        fat: 100,
      };

      const res = await request(app)
        .post("/api/calorie-calculator/calculate")
        .send(calculationData);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data).toHaveProperty("totalCalories");
    });

    it("should return 400 for missing required fields", async () => {
      const res = await request(app)
        .post("/api/calorie-calculator/calculate")
        .send({ age: 25 }); // Missing other fields

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });

    it("should calculate calories with partial macronutrients", async () => {
      const calculationData = {
        carbs: 50,
        protein: 25,
        // fat: 0 (not provided)
      };

      const res = await request(app)
        .post("/api/calorie-calculator/calculate")
        .send(calculationData);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data.totalCalories).toBe(300); // 50*4 + 25*4 + 0*9
    });
  });

  describe("POST /api/calorie-calculator/food-calories", () => {
    it("should calculate food calories from direct calories", async () => {
      const foodData = {
        calories: 250,
      };

      const res = await request(app)
        .post("/api/calorie-calculator/food-calories")
        .send(foodData);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data.totalCalories).toBe(250);
    });

    it("should calculate food calories from macronutrients", async () => {
      const foodData = {
        carbs: 30,
        protein: 10,
        fat: 5,
      };

      const res = await request(app)
        .post("/api/calorie-calculator/food-calories")
        .send(foodData);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data.totalCalories).toBe(205); // 30*4 + 10*4 + 5*9 = 120 + 40 + 45 = 205
    });

    it("should return 400 for missing all fields", async () => {
      const res = await request(app)
        .post("/api/calorie-calculator/food-calories")
        .send({});

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain(
        "Either calories or macronutrients must be provided"
      );
    });
  });

  describe("GET /api/calorie-calculator/macronutrient-values", () => {
    it("should return macronutrient calorie values", async () => {
      const res = await request(app).get(
        "/api/calorie-calculator/macronutrient-values"
      );

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data).toHaveProperty("macronutrientCalories");
      expect(res.body.data).toHaveProperty("description");
      expect(res.body.data.macronutrientCalories.CARBS).toBe(4);
      expect(res.body.data.macronutrientCalories.PROTEIN).toBe(4);
      expect(res.body.data.macronutrientCalories.FAT).toBe(9);
    });
  });

  describe("POST /api/calorie-calculator/individual", () => {
    it("should calculate carbs calories", async () => {
      const data = {
        macronutrient: "carbs",
        grams: 50,
      };

      const res = await request(app)
        .post("/api/calorie-calculator/individual")
        .send(data);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toBeDefined();
      expect(res.body.data.calories).toBe(200); // 50 * 4
      expect(res.body.data.macronutrient).toBe("carbs");
    });

    it("should calculate protein calories", async () => {
      const data = {
        macronutrient: "protein",
        grams: 25,
      };

      const res = await request(app)
        .post("/api/calorie-calculator/individual")
        .send(data);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.calories).toBe(100); // 25 * 4
    });

    it("should calculate fat calories", async () => {
      const data = {
        macronutrient: "fat",
        grams: 10,
      };

      const res = await request(app)
        .post("/api/calorie-calculator/individual")
        .send(data);

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.calories).toBe(90); // 10 * 9
    });

    it("should return 400 for missing macronutrient", async () => {
      const res = await request(app)
        .post("/api/calorie-calculator/individual")
        .send({ grams: 50 });

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain(
        "Macronutrient type and grams are required"
      );
    });

    it("should return 400 for missing grams", async () => {
      const res = await request(app)
        .post("/api/calorie-calculator/individual")
        .send({ macronutrient: "carbs" });

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });

    it("should return 400 for invalid macronutrient type", async () => {
      const res = await request(app)
        .post("/api/calorie-calculator/individual")
        .send({ macronutrient: "invalid", grams: 50 });

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain("carbs', 'protein', or 'fat'");
    });
  });
});
