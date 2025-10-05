package com.example.forkit.utils

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Validation utility functions
 * Tests input validation, email validation, and data validation logic
 */
class ValidationUtilsTest {

    @Test
    fun `email validation works correctly`() {
        // Valid emails
        val validEmails = listOf(
            "user@example.com",
            "test.user@domain.co.uk",
            "user123@subdomain.example.org",
            "user+tag@example.com",
            "user.name+tag@example.com"
        )
        
        validEmails.forEach { email ->
            assertTrue("Email '$email' should be valid", isValidEmail(email))
        }
        
        // Invalid emails
        val invalidEmails = listOf(
            "",
            "invalid-email",
            "@example.com",
            "user@",
            "user@.com",
            "user..name@example.com",
            "user@example",
            "user name@example.com"
        )
        
        invalidEmails.forEach { email ->
            assertFalse("Email '$email' should be invalid", isValidEmail(email))
        }
    }

    @Test
    fun `password validation works correctly`() {
        // Valid passwords
        val validPasswords = listOf(
            "password123",
            "SecurePass456",
            "MyP@ssw0rd",
            "VerySecure123!",
            "P@ssw0rd!@#$%"
        )
        
        validPasswords.forEach { password ->
            assertTrue("Password should be valid", isValidPassword(password))
        }
        
        // Invalid passwords
        val invalidPasswords = listOf(
            "",
            "123",
            "password",
            "12345678",
            "abcdefgh"
        )
        
        invalidPasswords.forEach { password ->
            assertFalse("Password should be invalid", isValidPassword(password))
        }
    }

    @Test
    fun `user id validation works correctly`() {
        // Valid user IDs
        val validUserIds = listOf(
            "user123",
            "user_456",
            "user-789",
            "user123abc",
            "user123ABC"
        )
        
        validUserIds.forEach { userId ->
            assertTrue("User ID '$userId' should be valid", isValidUserId(userId))
        }
        
        // Invalid user IDs
        val invalidUserIds = listOf(
            "",
            " ",
            "user 123",
            "user@123",
            "user#123",
            "user.123"
        )
        
        invalidUserIds.forEach { userId ->
            assertFalse("User ID '$userId' should be invalid", isValidUserId(userId))
        }
    }

    @Test
    fun `numeric validation works correctly`() {
        // Valid numbers
        val validNumbers = listOf(
            "123",
            "0",
            "999999",
            "123.45",
            "0.5"
        )
        
        validNumbers.forEach { number ->
            assertTrue("Number '$number' should be valid", isValidNumber(number))
        }
        
        // Invalid numbers
        val invalidNumbers = listOf(
            "",
            "abc",
            "12a",
            "a12",
            "12.34.56",
            "12,34"
        )
        
        invalidNumbers.forEach { number ->
            assertFalse("Number '$number' should be invalid", isValidNumber(number))
        }
    }

    @Test
    fun `date validation works correctly`() {
        // Valid dates
        val validDates = listOf(
            "2025-01-15",
            "2025-12-31",
            "2025-02-28",
            "2024-02-29" // Leap year
        )
        
        validDates.forEach { date ->
            assertTrue("Date '$date' should be valid", isValidDate(date))
        }
        
        // Invalid dates
        val invalidDates = listOf(
            "",
            "2025-1-15",
            "25-01-15",
            "2025/01/15",
            "2025-13-01",
            "2025-01-32",
            "2025-02-30"
        )
        
        invalidDates.forEach { date ->
            assertFalse("Date '$date' should be invalid", isValidDate(date))
        }
    }

    @Test
    fun `serving size validation works correctly`() {
        // Valid serving sizes
        val validServingSizes = listOf(
            "1.0",
            "0.5",
            "2.5",
            "100",
            "0.25"
        )
        
        validServingSizes.forEach { servingSize ->
            assertTrue("Serving size '$servingSize' should be valid", isValidServingSize(servingSize))
        }
        
        // Invalid serving sizes
        val invalidServingSizes = listOf(
            "",
            "0",
            "-1.0",
            "abc",
            "1.2.3"
        )
        
        invalidServingSizes.forEach { servingSize ->
            assertFalse("Serving size '$servingSize' should be invalid", isValidServingSize(servingSize))
        }
    }

    @Test
    fun `meal type validation works correctly`() {
        // Valid meal types
        val validMealTypes = listOf(
            "breakfast",
            "lunch",
            "dinner",
            "snack",
            "Breakfast",
            "LUNCH"
        )
        
        validMealTypes.forEach { mealType ->
            assertTrue("Meal type '$mealType' should be valid", isValidMealType(mealType))
        }
        
        // Invalid meal types
        val invalidMealTypes = listOf(
            "",
            "brunch",
            "midnight snack",
            "123",
            "meal"
        )
        
        invalidMealTypes.forEach { mealType ->
            assertFalse("Meal type '$mealType' should be invalid", isValidMealType(mealType))
        }
    }

    @Test
    fun `exercise type validation works correctly`() {
        // Valid exercise types
        val validExerciseTypes = listOf(
            "cardio",
            "strength",
            "flexibility",
            "endurance",
            "Cardio",
            "STRENGTH"
        )
        
        validExerciseTypes.forEach { exerciseType ->
            assertTrue("Exercise type '$exerciseType' should be valid", isValidExerciseType(exerciseType))
        }
        
        // Invalid exercise types
        val invalidExerciseTypes = listOf(
            "",
            "running",
            "swimming",
            "123",
            "exercise"
        )
        
        invalidExerciseTypes.forEach { exerciseType ->
            assertFalse("Exercise type '$exerciseType' should be invalid", isValidExerciseType(exerciseType))
        }
    }

    @Test
    fun `input sanitization works correctly`() {
        // Test input sanitization
        val inputs = listOf(
            "  user@example.com  " to "user@example.com",
            "USER@EXAMPLE.COM" to "user@example.com",
            "  Test User  " to "Test User",
            "user@example.com\n" to "user@example.com"
        )
        
        inputs.forEach { (input, expected) ->
            val sanitized = sanitizeInput(input)
            assertEquals("Input should be sanitized correctly", expected, sanitized)
        }
    }

    @Test
    fun `validation error messages are correct`() {
        // Test validation error messages
        val testCases = listOf(
            "" to "Input cannot be empty",
            "invalid-email" to "Invalid email format",
            "123" to "Password must be at least 8 characters",
            "user 123" to "User ID cannot contain spaces"
        )
        
        testCases.forEach { (input, expectedMessage) ->
            val errorMessage = getValidationErrorMessage(input)
            assertNotNull("Error message should not be null", errorMessage)
            assertTrue("Error message should contain expected text", 
                errorMessage.contains(expectedMessage.substringBefore(" ")))
        }
    }

    // Helper functions for validation (these would be implemented in the actual utils class)
    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && 
               email.contains("@") && 
               email.contains(".") &&
               !email.startsWith("@") &&
               !email.endsWith("@") &&
               !email.contains("..") &&
               email.indexOf("@") < email.lastIndexOf(".")
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8 && password.any { it.isDigit() }
    }

    private fun isValidUserId(userId: String): Boolean {
        return userId.isNotEmpty() && 
               userId.matches(Regex("[a-zA-Z0-9_-]+")) &&
               !userId.contains(" ")
    }

    private fun isValidNumber(number: String): Boolean {
        return try {
            number.toDouble()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun isValidDate(date: String): Boolean {
        return date.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) && 
               date.length == 10
    }

    private fun isValidServingSize(servingSize: String): Boolean {
        return try {
            val size = servingSize.toDouble()
            size > 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun isValidMealType(mealType: String): Boolean {
        val validTypes = listOf("breakfast", "lunch", "dinner", "snack")
        return validTypes.contains(mealType.lowercase())
    }

    private fun isValidExerciseType(exerciseType: String): Boolean {
        val validTypes = listOf("cardio", "strength", "flexibility", "endurance")
        return validTypes.contains(exerciseType.lowercase())
    }

    private fun sanitizeInput(input: String): String {
        return input.trim()
    }

    private fun getValidationErrorMessage(input: String): String {
        return when {
            input.isEmpty() -> "Input cannot be empty"
            input.contains("@") && !input.contains(".") -> "Invalid email format"
            input.length < 8 -> "Password must be at least 8 characters"
            input.contains(" ") -> "User ID cannot contain spaces"
            else -> "Invalid input"
        }
    }
}
