module.exports = {
  testEnvironment: "node",
  coverageDirectory: "coverage",
  collectCoverageFrom: [
    "src/services/**/*.js",
    "src/controllers/**/*.js",
    "!src/config/**",
    "!src/server.js",
  ],
  testMatch: ["**/tests/**/*.test.js", "**/?(*.)+(spec|test).js"],
  coverageThreshold: {
    global: {
      branches: 70,
      functions: 70,
      lines: 70,
      statements: 70,
    },
  },
  verbose: true,
};
