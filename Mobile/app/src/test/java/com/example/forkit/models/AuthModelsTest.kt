package com.example.forkit.models

import com.example.forkit.data.models.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Authentication data models
 * Tests login, registration, and authentication responses
 */
class AuthModelsTest {
    
    @Test
    fun `register request creates with email and password`() {
        // Act
        val registerRequest = RegisterRequest(
            email = "test@example.com",
            password = "securePassword123"
        )
        
        // Assert
        assertEquals("test@example.com", registerRequest.email)
        assertEquals("securePassword123", registerRequest.password)
    }
    
    @Test
    fun `register response includes user details`() {
        // Act
        val registerResponse = RegisterResponse(
            message = "User registered successfully",
            uid = "user123",
            email = "test@example.com"
        )
        
        // Assert
        assertEquals("User registered successfully", registerResponse.message)
        assertEquals("user123", registerResponse.uid)
        assertEquals("test@example.com", registerResponse.email)
    }
    
    @Test
    fun `login request validates credentials`() {
        // Act
        val loginRequest = LoginRequest(
            email = "user@example.com",
            password = "myPassword456"
        )
        
        // Assert
        assertEquals("user@example.com", loginRequest.email)
        assertEquals("myPassword456", loginRequest.password)
    }
    
    @Test
    fun `login response includes authentication tokens`() {
        // Act
        val loginResponse = LoginResponse(
            message = "Login successful",
            userId = "user123",
            idToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
            refreshToken = "refresh_token_123",
            expiresIn = "3600"
        )
        
        // Assert
        assertEquals("Login successful", loginResponse.message)
        assertEquals("user123", loginResponse.userId)
        assertTrue(loginResponse.idToken.startsWith("eyJ"))
        assertEquals("refresh_token_123", loginResponse.refreshToken)
        assertEquals("3600", loginResponse.expiresIn)
    }
    
    @Test
    fun `register request with different email formats`() {
        // Test various email formats
        val emails = listOf(
            "user@example.com",
            "test.user+tag@domain.co.uk",
            "user123@subdomain.example.org"
        )
        
        emails.forEach { email ->
            val request = RegisterRequest(email, "password123")
            assertEquals(email, request.email)
        }
    }
    
    @Test
    fun `login response with different token formats`() {
        // Act
        val shortTokenResponse = LoginResponse(
            message = "Login successful",
            userId = "user123",
            idToken = "short_token",
            refreshToken = "refresh_123",
            expiresIn = "1800"
        )
        
        val longTokenResponse = LoginResponse(
            message = "Login successful",
            userId = "user456",
            idToken = "very_long_jwt_token_with_many_characters_and_payload",
            refreshToken = "long_refresh_token_456",
            expiresIn = "7200"
        )
        
        // Assert
        assertEquals("short_token", shortTokenResponse.idToken)
        assertEquals("1800", shortTokenResponse.expiresIn)
        
        assertTrue(longTokenResponse.idToken.length > 50)
        assertEquals("7200", longTokenResponse.expiresIn)
    }
    
    @Test
    fun `register response with success message`() {
        // Act
        val successResponse = RegisterResponse(
            message = "Account created successfully! Welcome to ForkIt.",
            uid = "new_user_789",
            email = "newuser@example.com"
        )
        
        // Assert
        assertTrue(successResponse.message.contains("successfully"))
        assertEquals("new_user_789", successResponse.uid)
        assertEquals("newuser@example.com", successResponse.email)
    }
    
    @Test
    fun `login response with extended expiration`() {
        // Act
        val extendedResponse = LoginResponse(
            message = "Login successful - session extended",
            userId = "user999",
            idToken = "extended_token_999",
            refreshToken = "extended_refresh_999",
            expiresIn = "86400" // 24 hours
        )
        
        // Assert
        assertEquals("86400", extendedResponse.expiresIn)
        assertTrue(extendedResponse.message.contains("extended"))
    }
    
    @Test
    fun `authentication models handle edge cases`() {
        // Test with empty strings
        val emptyEmailRequest = RegisterRequest("", "password")
        assertEquals("", emptyEmailRequest.email)
        
        // Test with special characters
        val specialPasswordRequest = LoginRequest("user@test.com", "P@ssw0rd!@#$%")
        assertEquals("P@ssw0rd!@#$%", specialPasswordRequest.password)
        
        // Test with very long tokens
        val longTokenResponse = LoginResponse(
            message = "Success",
            userId = "user",
            idToken = "a".repeat(1000),
            refreshToken = "b".repeat(500),
            expiresIn = "3600"
        )
        
        assertEquals(1000, longTokenResponse.idToken.length)
        assertEquals(500, longTokenResponse.refreshToken.length)
    }
}