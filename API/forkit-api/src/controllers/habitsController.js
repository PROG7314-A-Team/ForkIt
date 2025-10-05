const firebaseService = require("../services/firebaseService");
const HabitSchedulingService = require("../services/habitSchedulingService");
const habitsService = new firebaseService("habits");
const habitSchedulingService = new HabitSchedulingService();

exports.getDailyHabits = async (req, res) => {
  try {
    const { userId } = req.params;
    if (!userId) return res.status(400).json({ 
      success: false, 
      message: "User ID is required",
      data: []
    });
    
    const dailyHabits = await habitSchedulingService.getDailyHabits(userId);
    
    res.json({
      success: true,
      message: "Daily habits retrieved successfully",
      data: dailyHabits
    });
  } catch (error) {
    console.error("Error getting daily habits:", error);
    res.status(500).json({ 
      success: false, 
      message: error.message,
      data: []
    });
  }
};

exports.getWeeklyHabits = async (req, res) => {
  try {
    const { userId } = req.params;
    if (!userId) return res.status(400).json({ 
      success: false, 
      message: "User ID is required",
      data: []
    });
    
    const weeklyHabits = await habitSchedulingService.getWeeklyHabits(userId);
    
    res.json({
      success: true,
      message: "Weekly habits retrieved successfully",
      data: weeklyHabits
    });
  } catch (error) {
    console.error("Error getting weekly habits:", error);
    res.status(500).json({ 
      success: false, 
      message: error.message,
      data: []
    });
  }
};


exports.getMonthlyHabits = async (req, res) => {
  try {
    const { userId } = req.params;
    if (!userId) return res.status(400).json({ 
      success: false, 
      message: "User ID is required",
      data: []
    });
    
    const monthlyHabits = await habitSchedulingService.getMonthlyHabits(userId);
    
    res.json({
      success: true,
      message: "Monthly habits retrieved successfully",
      data: monthlyHabits
    });
  } catch (error) {
    console.error("Error getting monthly habits:", error);
    res.status(500).json({ 
      success: false, 
      message: error.message,
      data: []
    });
  }
};


exports.createHabit = async (req, res) => {
  try {
    console.log("createHabit req.body", req.body);
    const { userId, habit } = req.body;
    if (!userId) return res.status(400).json({ 
      success: false, 
      message: "User ID is required" 
    });
    if (!habit) return res.status(400).json({ 
      success: false, 
      message: "Habit is required" 
    });
    
    const createdHabit = await habitSchedulingService.createHabit(userId, habit);
    
    res.status(201).json({
      success: true,
      message: "Habit created successfully",
      data: createdHabit
    });
  } catch (error) {
    console.error("Error creating habit:", error);
    res.status(500).json({ 
      success: false, 
      message: error.message 
    });
  }
};

exports.updateHabit = async (req, res) => {
  try {
    const { id } = req.params;
    const updateData = req.body;
    
    // Handle completion/uncompletion separately from other updates
    if (updateData.isCompleted !== undefined) {
      // Get the habit to find userId
      const habitDoc = await habitsService.getById(id);
      if (!habitDoc || !habitDoc.data()) {
        return res.status(404).json({
          success: false,
          message: "Habit not found"
        });
      }
      
      const userId = habitDoc.data().userId;
      let result;
      
      if (updateData.isCompleted) {
        result = await habitSchedulingService.completeHabit(id, userId);
      } else {
        result = await habitSchedulingService.uncompleteHabit(id, userId);
      }
      
      return res.json({
        success: true,
        message: updateData.isCompleted ? "Habit completed successfully" : "Habit uncompleted successfully",
        data: result
      });
    }
    
    // Handle other updates (title, description, etc.)
    const updatedHabit = await habitsService.update(id, updateData);
    
    res.json({
      success: true,
      message: "Habit updated successfully",
      data: updatedHabit
    });
  } catch (error) {
    console.error("Error updating habit:", error);
    res.status(500).json({ 
      success: false, 
      message: error.message 
    });
  }
};

exports.deleteHabit = async (req, res) => {
  try {
    const { id } = req.params;
    
    const deletedHabit = await habitsService.delete(id);
    
    res.json({
      success: true,
      message: "Habit deleted successfully",
      data: deletedHabit
    });
  } catch (error) {
    console.error("Error deleting habit:", error);
    res.status(500).json({ 
      success: false, 
      message: error.message 
    });
  }
};
