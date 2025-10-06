package com.example.forkit.business

import com.example.forkit.data.models.StreakData
import com.example.forkit.data.models.User
import com.example.forkit.data.models.UserGoals
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for User data models
 * Tests streak tracking, user goals, and user profile functionality
 */
class UserModelsTest {
    
    @Test
    fun `streak data creates with correct initial values`() {
        // Arrange & Act
        val streakData = StreakData(
            currentStreak = 5,
            longestStreak = 10,
            lastLogDate = "2025-10-05",
            streakStartDate = "2025-10-01",
            isActive = true
        )
        
        // Assert
        assertEquals(5, streakData.currentStreak)
        assertEquals(10, streakData.longestStreak)
        assertEquals("2025-10-05", streakData.lastLogDate)
        assertEquals("2025-10-01", streakData.streakStartDate)
        assertTrue(streakData.isActive)
    }
    
    @Test
    fun `user model creates with all required fields`() {
        // Arrange
        val streakData = StreakData(
            currentStreak = 3,
            longestStreak = 5,
            lastLogDate = "2025-10-05",
            streakStartDate = "2025-10-03",
            isActive = true
        )
        
        // Act
        val user = User(
            userId = "user123",
            success = true,
            message = "User created successfully",
            email = "test@example.com",
            age = 25,
            height = 175.0,
            weight = 70.0,
            streakData = streakData
        )
        
        // Assert
        assertEquals("user123", user.userId)
        assertTrue(user.success)
        assertEquals("test@example.com", user.email)
        assertEquals(25, user.age)
        assertEquals(175.0, user.height)
        assertEquals(70.0, user.weight)
        assertEquals(streakData, user.streakData)
    }
    
    @Test
    fun `user goals creates with valid daily targets`() {
        // Arrange & Act
        val userGoals = UserGoals(
            userId = "user123",
            dailyCalories = 2000,
            dailyWater = 2000,
            dailySteps = 10000,
            weeklyExercises = 5,
            updatedAt = "2025-10-05T12:00:00Z"
        )
        
        // Assert
        assertEquals("user123", userGoals.userId)
        assertEquals(2000, userGoals.dailyCalories)
        assertEquals(2000, userGoals.dailyWater)
        assertEquals(10000, userGoals.dailySteps)
        assertEquals(5, userGoals.weeklyExercises)
    }
    
    @Test
    fun `active streak is true when current streak greater than zero`() {
        // Arrange
        val activeStreak = StreakData(
            currentStreak = 7,
            longestStreak = 10,
            lastLogDate = "2025-10-05",
            streakStartDate = "2025-09-29",
            isActive = true
        )
        
        // Assert
        assertTrue(activeStreak.isActive)
        assertTrue(activeStreak.currentStreak > 0)
    }
    
    @Test
    fun `longest streak should be greater than or equal to current streak`() {
        // Arrange
        val streakData = StreakData(
            currentStreak = 5,
            longestStreak = 12,
            lastLogDate = "2025-10-05",
            streakStartDate = "2025-10-01",
            isActive = true
        )
        
        // Assert
        assertTrue(streakData.longestStreak >= streakData.currentStreak)
    }
}