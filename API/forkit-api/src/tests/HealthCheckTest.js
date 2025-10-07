// Set test environment
process.env.NODE_ENV = "test";
const request = require("supertest");
const app = require("../server");
const { db } = require("../config/firebase");

describe("Health Check", () => {
  it("should return health status", async () => {
    const res = await request(app).get("/api/health");
    expect(res.status).toBe(200);
    expect(res.body.status).toBe("OK");
    expect(res.body.message).toBe("ForkIt API is running");
  });

  it("should return root endpoint", async () => {
    const res = await request(app).get("/");
    expect(res.status).toBe(200);
    expect(res.body.message).toBe("Welcome to ForkIt API");
  });
});
