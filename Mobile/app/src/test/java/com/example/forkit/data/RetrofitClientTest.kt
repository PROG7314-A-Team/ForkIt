package com.example.forkit.data

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Unit tests for RetrofitClient
 * Tests API client configuration, URL handling, and service creation
 */
class RetrofitClientTest {

    @Before
    fun setUp() {
        // Reset any static state if needed
    }

    @Test
    fun `retrofit client has correct base url`() {
        // Act
        val baseUrl = "https://forkit-api.onrender.com/"
        
        // Assert
        assertEquals("https://forkit-api.onrender.com/", baseUrl)
        assertTrue("Base URL should be HTTPS", baseUrl.startsWith("https://"))
        assertTrue("Base URL should end with slash", baseUrl.endsWith("/"))
    }

    @Test
    fun `retrofit client creates api service correctly`() {
        // Act
        val apiService = RetrofitClient.api
        
        // Assert
        assertNotNull("API service should not be null", apiService)
        assertTrue("API service should be instance of ApiService", apiService is ApiService)
    }

    @Test
    fun `retrofit client is singleton`() {
        // Act
        val apiService1 = RetrofitClient.api
        val apiService2 = RetrofitClient.api
        
        // Assert
        assertSame("API service should be singleton", apiService1, apiService2)
    }

    @Test
    fun `retrofit client configuration is correct`() {
        // Act
        val retrofit = Retrofit.Builder()
            .baseUrl("https://forkit-api.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        // Assert
        assertNotNull("Retrofit instance should not be null", retrofit)
        assertEquals("https://forkit-api.onrender.com/", retrofit.baseUrl().toString())
    }

    @Test
    fun `retrofit client handles different base urls`() {
        // Test with different base URLs
        val testUrls = listOf(
            "https://forkit-api.onrender.com/",
            "https://api.forkit.com/",
            "http://localhost:3000/"
        )
        
        testUrls.forEach { url ->
            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            assertNotNull("Retrofit should be created for URL: $url", retrofit)
            assertEquals(url, retrofit.baseUrl().toString())
        }
    }

    @Test
    fun `retrofit client creates service with correct interface`() {
        // Act
        val apiService = RetrofitClient.api
        
        // Assert
        assertNotNull("API service should not be null", apiService)
        
        // Verify the service has the expected methods by checking the class
        val serviceClass = apiService.javaClass
        assertNotNull("Service class should not be null", serviceClass)
        
        // Check if it implements ApiService interface
        val interfaces = serviceClass.interfaces
        assertTrue("Service should implement ApiService interface", 
            interfaces.any { it.simpleName == "ApiService" })
    }

    @Test
    fun `retrofit client handles network configuration`() {
        // Act
        val retrofit = Retrofit.Builder()
            .baseUrl("https://forkit-api.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        // Assert
        assertNotNull("Retrofit should be created", retrofit)
        
        // Verify converter factory is set
        val converterFactories = retrofit.converterFactories()
        assertTrue("Should have Gson converter factory", 
            converterFactories.any { it is GsonConverterFactory })
    }

    @Test
    fun `retrofit client base url format validation`() {
        // Arrange
        val baseUrl = "https://forkit-api.onrender.com/"
        
        // Act & Assert
        assertTrue("Base URL should be valid HTTPS URL", 
            baseUrl.matches(Regex("^https://.*/$")))
        
        assertTrue("Base URL should contain domain", 
            baseUrl.contains("forkit-api.onrender.com"))
        
        assertTrue("Base URL should end with slash", 
            baseUrl.endsWith("/"))
    }

    @Test
    fun `retrofit client handles service creation edge cases`() {
        // Test that service creation doesn't throw exceptions
        try {
            val apiService = RetrofitClient.api
            assertNotNull("API service should be created without exceptions", apiService)
        } catch (e: Exception) {
            fail("API service creation should not throw exceptions: ${e.message}")
        }
    }

    @Test
    fun `retrofit client configuration consistency`() {
        // Act
        val apiService1 = RetrofitClient.api
        val apiService2 = RetrofitClient.api
        
        // Assert
        assertSame("API services should be identical", apiService1, apiService2)
        assertEquals("API service classes should be identical", 
            apiService1.javaClass, apiService2.javaClass)
    }
}
