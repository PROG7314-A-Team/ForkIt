const FirebaseService = require("./firebaseService");

class StreakService {
  constructor() {}

  async updateUserStreak(userId, logDate) {
    const userService = new FirebaseService("users");
    const user = await userService.query([
      { field: "userId", operator: "==", value: userId },
    ]);

    if (!user || user.length === 0) {
      return {
        success: false,
        message: "User not found",
      };
    }

    const userData = user[0];
    const currentDate = new Date(logDate);
    const lastLogDate = userData.streakData?.lastLogDate
      ? new Date(userData.streakData.lastLogDate)
      : null;

    let streakData = userData.streakData || {
      currentStreak: 0,
      longestStreak: 0,
      lastLogDate: null,
      streakStartDate: null,
    };

    // Check if this is the same day as the last log
    const isSameDay = lastLogDate && this.isSameDay(lastLogDate, currentDate);

    // Only update streak if this is a new day
    if (!isSameDay) {
      // Does this log extend the streak?
      if (this.isConsecutiveDay(lastLogDate, currentDate)) {
        streakData.currentStreak += 1;
        if (!streakData.streakStartDate) {
          streakData.streakStartDate = logDate;
        }
      } else if (this.isNewDay(lastLogDate, currentDate)) {
        // Reset streak if more than 1 day gap
        streakData.currentStreak = 1;
        streakData.streakStartDate = logDate;
      } else {
        // First log ever
        streakData.currentStreak = 1;
        streakData.streakStartDate = logDate;
      }

      // Update longest streak
      if (streakData.currentStreak > streakData.longestStreak) {
        streakData.longestStreak = streakData.currentStreak;
      }

      // Update last log date only if it's a new day
      streakData.lastLogDate = logDate;
    }

    streakData.lastCalculated = new Date().toISOString();

    // Update the user document
    await userService.update(userData.id, { streakData });

    return {
      success: true,
      streakData: streakData,
    };
  }

  isSameDay(date1, date2) {
    if (!date1 || !date2) return false;
    return date1.toDateString() === date2.toDateString();
  }

  isConsecutiveDay(lastDate, currentDate) {
    if (!lastDate) return false; // Changed from true to false
    const diffTime = currentDate - lastDate;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays === 1;
  }

  isNewDay(lastDate, currentDate) {
    if (!lastDate) return true;
    const diffTime = currentDate - lastDate;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays > 1;
  }

  isStreakActive(lastLogDate) {
    if (!lastLogDate) return false;
    const currentDate = new Date();
    const lastLogDateObj = new Date(lastLogDate);
    const diffTime = currentDate - lastLogDateObj;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays <= 1;
  }
}

module.exports = StreakService;
