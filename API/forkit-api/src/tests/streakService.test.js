const StreakService = require("../services/streakService");

describe("StreakService", () => {
  let streakService;

  beforeEach(() => {
    streakService = new StreakService();
  });

  describe("isSameDay", () => {
    test("should return true for same day", () => {
      const date1 = new Date("2025-10-05");
      const date2 = new Date("2025-10-05");

      const result = streakService.isSameDay(date1, date2);

      expect(result).toBe(true);
    });

    test("should return false for different days", () => {
      const date1 = new Date("2025-10-05");
      const date2 = new Date("2025-10-06");

      const result = streakService.isSameDay(date1, date2);

      expect(result).toBe(false);
    });

    test("should return true for same day different times", () => {
      const date1 = new Date("2025-10-05T08:00:00");
      const date2 = new Date("2025-10-05T20:00:00");

      const result = streakService.isSameDay(date1, date2);

      expect(result).toBe(true);
    });
  });

  describe("isConsecutiveDay", () => {
    test("should return true for consecutive days", () => {
      const lastDate = new Date("2025-10-04");
      const currentDate = new Date("2025-10-05");

      const result = streakService.isConsecutiveDay(lastDate, currentDate);

      expect(result).toBe(true);
    });

    test("should return false for same day", () => {
      const lastDate = new Date("2025-10-05");
      const currentDate = new Date("2025-10-05");

      const result = streakService.isConsecutiveDay(lastDate, currentDate);

      expect(result).toBe(false);
    });

    test("should return false for non-consecutive days", () => {
      const lastDate = new Date("2025-10-03");
      const currentDate = new Date("2025-10-05");

      const result = streakService.isConsecutiveDay(lastDate, currentDate);

      expect(result).toBe(false);
    });

    test("should return false when lastDate is null", () => {
      const currentDate = new Date("2025-10-05");

      const result = streakService.isConsecutiveDay(null, currentDate);

      expect(result).toBe(false);
    });
  });

  describe("isNewDay", () => {
    test("should return true when lastDate is null", () => {
      const currentDate = new Date("2025-10-05");

      const result = streakService.isNewDay(null, currentDate);

      expect(result).toBe(true);
    });

    test("should return false for consecutive days", () => {
      const lastDate = new Date("2025-10-04");
      const currentDate = new Date("2025-10-05");

      const result = streakService.isNewDay(lastDate, currentDate);

      expect(result).toBe(false);
    });

    test("should return true for days more than 1 apart", () => {
      const lastDate = new Date("2025-10-03");
      const currentDate = new Date("2025-10-05");

      const result = streakService.isNewDay(lastDate, currentDate);

      expect(result).toBe(true);
    });
  });

  describe("isStreakActive", () => {
    test("should return false when lastLogDate is null", () => {
      const result = streakService.isStreakActive(null);

      expect(result).toBe(false);
    });

    test("should return true when last log was today", () => {
      const today = new Date().toISOString();

      const result = streakService.isStreakActive(today);

      expect(result).toBe(true);
    });

    test("should return true when last log was yesterday", () => {
      const yesterday = new Date();
      yesterday.setDate(yesterday.getDate() - 1);

      const result = streakService.isStreakActive(yesterday.toISOString());

      expect(result).toBe(true);
    });

    test("should return false when last log was more than 1 day ago", () => {
      const twoDaysAgo = new Date();
      twoDaysAgo.setDate(twoDaysAgo.getDate() - 2);

      const result = streakService.isStreakActive(twoDaysAgo.toISOString());

      expect(result).toBe(false);
    });
  });
});
