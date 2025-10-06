package com.example.forkit.business

import com.example.forkit.data.models.Habit
import com.example.forkit.data.models.HabitCategory
import com.example.forkit.data.models.HabitFrequency
import com.example.forkit.data.models.MockHabits
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime

/**
 * Unit tests for Habit data models
 * Tests habit creation, categories, frequencies, and completion tracking
 */
class HabitModelsTest {
    
    @Test
    fun `habit creates with all required fields`() {
        // Arrange & Act
        val habit = Habit(
            id = "habit123",
            title = "Drink 8 glasses of water",
            description = "Stay hydrated throughout the day",
            isCompleted = false,
            completedAt = null,
            createdAt = LocalDateTime.now().toString(),
            category = HabitCategory.HEALTH.toString(),
            frequency = HabitFrequency.DAILY.toString()
        )
        
        // Assert
        assertEquals("habit123", habit.id)
        assertEquals("Drink 8 glasses of water", habit.title)
        assertEquals("Stay hydrated throughout the day", habit.description)
        assertFalse(habit.isCompleted)
        assertNull(habit.completedAt)
        assertEquals(HabitCategory.HEALTH.toString(), habit.category)
        assertEquals(HabitFrequency.DAILY.toString(), habit.frequency)
    }
    
    @Test
    fun `completed habit has completion timestamp`() {
        // Arrange
        val completionTime = LocalDateTime.now().toString()
        
        // Act
        val habit = Habit(
            id = "habit456",
            title = "Morning exercise",
            description = "30 minutes of cardio",
            isCompleted = true,
            createdAt = "2025-10-05T12:00:00Z",
            completedAt = completionTime.toString(),
            category = HabitCategory.EXERCISE.toString(),
            frequency = HabitFrequency.DAILY.toString()
        )
        
        // Assert
        assertTrue(habit.isCompleted)
        assertNotNull(habit.completedAt)
        assertEquals(completionTime, habit.completedAt)
    }
    
    @Test
    fun `habit categories are correctly defined`() {
        // Assert
        val categories = HabitCategory.values()
        assertTrue(categories.contains(HabitCategory.NUTRITION))
        assertTrue(categories.contains(HabitCategory.EXERCISE))
        assertTrue(categories.contains(HabitCategory.HEALTH))
        assertTrue(categories.contains(HabitCategory.GENERAL))
    }
    
    @Test
    fun `habit frequencies are correctly defined`() {
        // Assert
        val frequencies = HabitFrequency.values()
        assertTrue(frequencies.contains(HabitFrequency.DAILY))
        assertTrue(frequencies.contains(HabitFrequency.WEEKLY))
        assertTrue(frequencies.contains(HabitFrequency.MONTHLY))
    }
    
    @Test
    fun `mock habits returns today habits list`() {
        // Act
        val todayHabits = MockHabits.getTodayHabits()
        
        // Assert
        assertNotNull(todayHabits)
        assertTrue(todayHabits.isNotEmpty())
        assertTrue(todayHabits.size >= 2)
        
        // Verify at least one nutrition habit exists
        val nutritionHabits = todayHabits.filter { it.category.toString() == HabitCategory.NUTRITION.toString() }
        assertTrue(nutritionHabits.isNotEmpty())
    }
    
    @Test
    fun `mock habits returns weekly habits list`() {
        // Act
        val weeklyHabits = MockHabits.getWeeklyHabits()
        
        // Assert
        assertNotNull(weeklyHabits)
        assertTrue(weeklyHabits.isNotEmpty())
    }
    
    @Test
    fun `uncompleted habit has no completion timestamp`() {
        // Act
        val habit = Habit(
            id = "habit789",
            title = "Read for 30 minutes",
            createdAt = "2025-10-05T12:00:00Z",
            isCompleted = false,
            category = HabitCategory.GENERAL.toString(),
            frequency = HabitFrequency.DAILY.toString()
        )
        
        // Assert
        assertFalse(habit.isCompleted)
        assertNull(habit.completedAt)
    }
}