const { db } = require("../config/firebase");

class HabitSchedulingService {
  
  /**
   * Get daily habits for a user, ensuring they reset daily
   */
  async getDailyHabits(userId) {
    try {
      const today = new Date().toISOString().split('T')[0]; // YYYY-MM-DD format
      
      // Get all daily habits for the user
      const habitsSnapshot = await db.collection('habits')
        .where('userId', '==', userId)
        .where('frequency', '==', 'DAILY')
        .get();
      
      const habits = [];
      
      for (const doc of habitsSnapshot.docs) {
        const habitData = doc.data();
        
        // Check if habit was completed today
        const completionSnapshot = await db.collection('habitCompletions')
          .where('habitId', '==', doc.id)
          .where('date', '==', today)
          .limit(1)
          .get();
        
        const isCompletedToday = !completionSnapshot.empty;
        
        habits.push({
          id: doc.id,
          title: habitData.title,
          description: habitData.description,
          category: habitData.category,
          frequency: habitData.frequency,
          isCompleted: isCompletedToday,
          completedAt: isCompletedToday ? new Date().toISOString() : null,
          createdAt: habitData.createdAt,
          userId: habitData.userId
        });
      }
      
      return habits;
    } catch (error) {
      throw new Error(`Error getting daily habits: ${error.message}`);
    }
  }
  
  /**
   * Get weekly habits for a user, showing only habits scheduled for today
   */
  async getWeeklyHabits(userId) {
    try {
      const today = new Date();
      const dayOfWeek = today.getDay(); // 0 = Sunday, 1 = Monday, etc.
      const todayStr = today.toISOString().split('T')[0];
      
      // Get all weekly habits for the user
      const habitsSnapshot = await db.collection('habits')
        .where('userId', '==', userId)
        .where('frequency', '==', 'WEEKLY')
        .get();
      
      const habits = [];
      
      for (const doc of habitsSnapshot.docs) {
        const habitData = doc.data();
        
        // Check if this habit should appear today (if today is in the selected days)
        if (habitData.selectedDays && habitData.selectedDays.includes(dayOfWeek)) {
          
          // Check if habit was completed today
          const completionSnapshot = await db.collection('habitCompletions')
            .where('habitId', '==', doc.id)
            .where('date', '==', todayStr)
            .limit(1)
            .get();
          
          const isCompletedToday = !completionSnapshot.empty;
          
          habits.push({
            id: doc.id,
            title: habitData.title,
            description: habitData.description,
            category: habitData.category,
            frequency: habitData.frequency,
            selectedDays: habitData.selectedDays,
            isCompleted: isCompletedToday,
            completedAt: isCompletedToday ? new Date().toISOString() : null,
            createdAt: habitData.createdAt,
            userId: habitData.userId
          });
        }
      }
      
      return habits;
    } catch (error) {
      throw new Error(`Error getting weekly habits: ${error.message}`);
    }
  }
  
  /**
   * Get monthly habits for a user, showing only habits scheduled for today
   */
  async getMonthlyHabits(userId) {
    try {
      const today = new Date();
      const dayOfMonth = today.getDate();
      const todayStr = today.toISOString().split('T')[0];
      
      // Get all monthly habits for the user
      const habitsSnapshot = await db.collection('habits')
        .where('userId', '==', userId)
        .where('frequency', '==', 'MONTHLY')
        .get();
      
      const habits = [];
      
      for (const doc of habitsSnapshot.docs) {
        const habitData = doc.data();
        
        // Check if this habit should appear today (if today matches the selected day of month)
        if (habitData.dayOfMonth === dayOfMonth) {
          
          // Check if habit was completed today
          const completionSnapshot = await db.collection('habitCompletions')
            .where('habitId', '==', doc.id)
            .where('date', '==', todayStr)
            .limit(1)
            .get();
          
          const isCompletedToday = !completionSnapshot.empty;
          
          habits.push({
            id: doc.id,
            title: habitData.title,
            description: habitData.description,
            category: habitData.category,
            frequency: habitData.frequency,
            dayOfMonth: habitData.dayOfMonth,
            isCompleted: isCompletedToday,
            completedAt: isCompletedToday ? new Date().toISOString() : null,
            createdAt: habitData.createdAt,
            userId: habitData.userId
          });
        }
      }
      
      return habits;
    } catch (error) {
      throw new Error(`Error getting monthly habits: ${error.message}`);
    }
  }
  
  /**
   * Mark a habit as completed for today
   */
  async completeHabit(habitId, userId) {
    try {
      const today = new Date().toISOString().split('T')[0];
      
      // Check if already completed today
      const existingCompletion = await db.collection('habitCompletions')
        .where('habitId', '==', habitId)
        .where('date', '==', today)
        .limit(1)
        .get();
      
      if (!existingCompletion.empty) {
        throw new Error('Habit already completed today');
      }
      
      // Create completion record
      await db.collection('habitCompletions').add({
        habitId: habitId,
        userId: userId,
        date: today,
        completedAt: new Date().toISOString()
      });
      
      return {
        id: habitId,
        isCompleted: true,
        completedAt: new Date().toISOString()
      };
    } catch (error) {
      throw new Error(`Error completing habit: ${error.message}`);
    }
  }
  
  /**
   * Unmark a habit as completed for today
   */
  async uncompleteHabit(habitId, userId) {
    try {
      const today = new Date().toISOString().split('T')[0];
      
      // Find and delete completion record for today
      const completionSnapshot = await db.collection('habitCompletions')
        .where('habitId', '==', habitId)
        .where('date', '==', today)
        .get();
      
      for (const doc of completionSnapshot.docs) {
        await doc.ref.delete();
      }
      
      return {
        id: habitId,
        isCompleted: false,
        completedAt: null
      };
    } catch (error) {
      throw new Error(`Error uncompleting habit: ${error.message}`);
    }
  }
  
  /**
   * Create a new habit with proper scheduling data
   */
  async createHabit(userId, habitData) {
    try {
      const habitDoc = {
        userId: userId,
        title: habitData.title,
        description: habitData.description,
        category: habitData.category,
        frequency: habitData.frequency,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      
      // Add scheduling-specific data based on frequency
      if (habitData.frequency === 'WEEKLY' && habitData.selectedDays) {
        habitDoc.selectedDays = habitData.selectedDays;
      }
      
      if (habitData.frequency === 'MONTHLY' && habitData.dayOfMonth) {
        habitDoc.dayOfMonth = habitData.dayOfMonth;
      }
      
      const docRef = await db.collection('habits').add(habitDoc);
      
      return {
        id: docRef.id,
        ...habitDoc
      };
    } catch (error) {
      throw new Error(`Error creating habit: ${error.message}`);
    }
  }
}

module.exports = HabitSchedulingService;
