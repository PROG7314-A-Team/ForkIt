const firebaseService = require("../services/firebaseService");
const habitsService = new firebaseService("habits");

exports.getDailyHabits = async (req, res) => {
  try {
    const { userId } = req.params;
    if (!userId) return res.status(400).json({ error: "User ID is required" });
    const habits = await habitsService.getByUserId(userId);
    // need to filter type = daily
    res.json(habits);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.getWeeklyHabits = async (req, res) => {
  try {
    const { userId } = req.params;
    if (!userId) return res.status(400).json({ error: "User ID is required" });
    const habits = await habitsService.getByUserId(userId);
    // need to filter type = weekly
    res.json(habits);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.getMonthlyHabits = async (req, res) => {
  try {
    const { userId } = req.params;
    if (!userId) return res.status(400).json({ error: "User ID is required" });
    const habits = await habitsService.getByUserId(userId);
    // need to filter type = monthly
    res.json(habits);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.createHabit = async (req, res) => {
  try {
    console.log("createHabit req.body", req.body);
    const { userId, habit } = req.body;
    if (!userId) return res.status(400).json({ error: "User ID is required" });
    if (!habit) return res.status(400).json({ error: "Habit is required" });
    const habitData = {
      userId,
      habit,
    };
    const createdHabit = await habitsService.create(habitData);
    res.json(createdHabit);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.updateHabit = async (req, res) => {
  try {
    const { id } = req.params;
    const updateData = req.body;
    const updatedHabit = await habitsService.update(id, updateData);
    res.json(updatedHabit);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.deleteHabit = async (req, res) => {
  try {
    const { id } = req.params;
    const deletedHabit = await habitsService.delete(id);
    res.json({
      success: true,
      data: deletedHabit,
      message: "Habit deleted successfully",
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};
