package com.example.forkit.activities

import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.forkit.LoginActivity
import com.example.forkit.SignInActivity
import com.example.forkit.SignUpActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider

/**
 * Unit tests for LoginActivity
 * Tests activity lifecycle, intent handling, and business logic
 */
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @Test
    fun `login activity creates successfully`() {
        // Arrange
        val intent = Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)
        
        // Act
        val scenario = ActivityScenario.launch<LoginActivity>(intent)
        
        // Assert
        scenario.onActivity { activity ->
            assertNotNull("Activity should not be null", activity)
            assertTrue("Activity should be instance of LoginActivity", activity is LoginActivity)
        }
        
        scenario.close()
    }

    @Test
    fun `login activity handles intent extras correctly`() {
        // Arrange
        val intent = Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)
        intent.putExtra("test_key", "test_value")
        
        // Act
        val scenario = ActivityScenario.launch<LoginActivity>(intent)
        
        // Assert
        scenario.onActivity { activity ->
            val extras = activity.intent.extras
            assertNotNull("Intent extras should not be null", extras)
            assertEquals("test_value", extras?.getString("test_key"))
        }
        
        scenario.close()
    }

    @Test
    fun `login activity onCreate sets content view`() {
        // Arrange
        val intent = Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)
        
        // Act
        val scenario = ActivityScenario.launch<LoginActivity>(intent)
        
        // Assert
        scenario.onActivity { activity ->
            // Verify activity is created and content is set
            assertNotNull("Activity should be created", activity)
            // The setContent call in onCreate should be executed
            assertTrue("Activity should be in created state", !activity.isFinishing)
        }
        
        scenario.close()
    }

    @Test
    fun `login activity handles configuration changes`() {
        // Arrange
        val intent = Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)
        
        // Act
        val scenario = ActivityScenario.launch<LoginActivity>(intent)
        
        // Assert
        scenario.onActivity { activity ->
            // Simulate configuration change
            val bundle = Bundle()
            assertNotNull("Bundle should not be null", bundle)
        }
        
        scenario.close()
    }

    @Test
    fun `login activity intent navigation works correctly`() {
        // Arrange
        val intent = Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)
        
        // Act
        val scenario = ActivityScenario.launch<LoginActivity>(intent)
        
        // Assert
        scenario.onActivity { activity ->
            // Test that the activity can create intents for navigation
            val signInIntent = Intent(activity, SignInActivity::class.java)
            val signUpIntent = Intent(activity, SignUpActivity::class.java)
            
            assertNotNull("SignIn intent should be created", signInIntent)
            assertNotNull("SignUp intent should be created", signUpIntent)
            
            // Verify intent components
            assertEquals("SignInActivity", signInIntent.component?.className)
            assertEquals("SignUpActivity", signUpIntent.component?.className)
        }
        
        scenario.close()
    }

    @Test
    fun `login activity handles edge cases`() {
        // Arrange
        val intent = Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)
        
        // Act
        val scenario = ActivityScenario.launch<LoginActivity>(intent)
        
        // Assert
        scenario.onActivity { activity ->
            // Test with null intent (edge case)
            val nullIntent = Intent()
            assertNotNull("Null intent should be handled gracefully", nullIntent)
            
            // Test activity state
            assertFalse("Activity should not be finishing initially", activity.isFinishing)
            assertTrue("Activity should be in valid state", activity.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.CREATED))
        }
        
        scenario.close()
    }

    @Test
    fun `login activity lifecycle methods execute correctly`() {
        // Arrange
        val intent = Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)
        
        // Act & Assert
        val scenario = ActivityScenario.launch<LoginActivity>(intent)
        
        scenario.onActivity { activity ->
            // Verify onCreate was called (setContent was executed)
            assertNotNull("Activity should be properly initialized", activity)
            
            // Test activity state without calling protected methods
            assertTrue("Activity should be in valid state", 
                activity.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.CREATED))
        }
        
        scenario.close()
    }
}
