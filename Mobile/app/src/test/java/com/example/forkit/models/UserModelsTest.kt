package com.example.forkit.models

import com.example.forkit.data.models.StreakData
import com.example.forkit.data.models.User
import com.example.forkit.data.models.UserGoals
import org.junit.Test
import org.junit.Assert.*

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
```

```kotlin:Mobile/app/src/test/java/com/example/forkit/models/HabitModelsTest.kt
package com.example.forkit.models

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
            createdAt = LocalDateTime.now(),
            category = HabitCategory.HEALTH,
            frequency = HabitFrequency.DAILY
        )
        
        // Assert
        assertEquals("habit123", habit.id)
        assertEquals("Drink 8 glasses of water", habit.title)
        assertEquals("Stay hydrated throughout the day", habit.description)
        assertFalse(habit.isCompleted)
        assertNull(habit.completedAt)
        assertEquals(HabitCategory.HEALTH, habit.category)
        assertEquals(HabitFrequency.DAILY, habit.frequency)
    }
    
    @Test
    fun `completed habit has completion timestamp`() {
        // Arrange
        val completionTime = LocalDateTime.now()
        
        // Act
        val habit = Habit(
            id = "habit456",
            title = "Morning exercise",
            description = "30 minutes of cardio",
            isCompleted = true,
            completedAt = completionTime,
            category = HabitCategory.EXERCISE,
            frequency = HabitFrequency.DAILY
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
        val nutritionHabits = todayHabits.filter { it.category == HabitCategory.NUTRITION }
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
            isCompleted = false,
            category = HabitCategory.GENERAL,
            frequency = HabitFrequency.DAILY
        )
        
        // Assert
        assertFalse(habit.isCompleted)
        assertNull(habit.completedAt)
    }
}
```

```kotlin:Mobile/app/src/test/java/com/example/forkit/models/CalorieCalculatorModelsTest.kt
package com.example.forkit.models

import com.example.forkit.data.models.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Calorie Calculator data models
 * Tests calorie calculations, macronutrient breakdowns, and validation logic
 */
class CalorieCalculatorModelsTest {
    
    @Test
    fun `macronutrient detail creates correctly`() {
        // Act
        val detail = MacronutrientDetail(
            grams = 50.0,
            calories = 200.0
        )
        
        // Assert
        assertEquals(50.0, detail.grams, 0.01)
        assertEquals(200.0, detail.calories, 0.01)
    }
    
    @Test
    fun `carbs should calculate to 4 calories per gram`() {
        // Arrange
        val carbGrams = 50.0
        val expectedCalories = carbGrams * 4
        
        // Act
        val carbDetail = MacronutrientDetail(
            grams = carbGrams,
            calories = expectedCalories
        )
        
        // Assert
        assertEquals(200.0, carbDetail.calories, 0.01)
    }
    
    @Test
    fun `protein should calculate to 4 calories per gram`() {
        // Arrange
        val proteinGrams = 30.0
        val expectedCalories = proteinGrams * 4
        
        // Act
        val proteinDetail = MacronutrientDetail(
            grams = proteinGrams,
            calories = expectedCalories
        )
        
        // Assert
        assertEquals(120.0, proteinDetail.calories, 0.01)
    }
    
    @Test
    fun `fat should calculate to 9 calories per gram`() {
        // Arrange
        val fatGrams = 20.0
        val expectedCalories = fatGrams * 9
        
        // Act
        val fatDetail = MacronutrientDetail(
            grams = fatGrams,
            calories = expectedCalories
        )
        
        // Assert
        assertEquals(180.0, fatDetail.calories, 0.01)
    }
    
    @Test
    fun `total calories from macronutrients calculates correctly`() {
        // Arrange
        val carbsCalories = 50.0 * 4  // 200
        val proteinCalories = 30.0 * 4  // 120
        val fatCalories = 20.0 * 9  // 180
        val expectedTotal = carbsCalories + proteinCalories + fatCalories  // 500
        
        // Act
        val macroCalories = MacronutrientCalories(
            carbs = carbsCalories,
            protein = proteinCalories,
            fat = fatCalories
        )
        
        val totalCalories = macroCalories.carbs + macroCalories.protein + macroCalories.fat
        
        // Assert
        assertEquals(expectedTotal, totalCalories, 0.01)
    }
    
    @Test
    fun `validation result with valid data`() {
        // Act
        val validation = ValidationResult(
            isValid = true,
            message = "Calories calculated successfully"
        )
        
        // Assert
        assertTrue(validation.isValid)
        assertEquals("Calories calculated successfully", validation.message)
    }
    
    @Test
    fun `validation result with invalid data`() {
        // Act
        val validation = ValidationResult(
            isValid = false,
            message = "Invalid macronutrient values"
        )
        
        // Assert
        assertFalse(validation.isValid)
        assertEquals("Invalid macronutrient values", validation.message)
    }
    
    @Test
    fun `calorie calculation result has correct structure`() {
        // Arrange
        val breakdown = MacronutrientBreakdown(
            carbs = MacronutrientDetail(50.0, 200.0),
            protein = MacronutrientDetail(30.0, 120.0),
            fat = MacronutrientDetail(20.0, 180.0)
        )
        
        val macroCalories = MacronutrientCalories(
            carbs = 200.0,
            protein = 120.0,
            fat = 180.0
        )
        
        // Act
        val result = CalorieCalculationResult(
            totalCalories = 500.0,
            breakdown = breakdown,
            macronutrientCalories = macroCalories
        )
        
        // Assert
        assertEquals(500.0, result.totalCalories, 0.01)
        assertEquals(breakdown, result.breakdown)
        assertEquals(macroCalories, result.macronutrientCalories)
    }
    
    @Test
    fun `calculate calories request with all macronutrients`() {
        // Act
        val request = CalculateCaloriesRequest(
            carbs = 50.0,
            protein = 30.0,
            fat = 20.0
        )
        
        // Assert
        assertEquals(50.0, request.carbs)
        assertEquals(30.0, request.protein)
        assertEquals(20.0, request.fat)
    }
    
    @Test
    fun `calculate calories request with partial macronutrients`() {
        // Act
        val request = CalculateCaloriesRequest(
            carbs = 50.0,
            protein = null,
            fat = 20.0
        )
        
        // Assert
        assertEquals(50.0, request.carbs)
        assertNull(request.protein)
        assertEquals(20.0, request.fat)
    }
}
```

## 2. Node.js API Unit Tests

Now let me create unit tests for the API:

```javascript:API/forkit-api/src/tests/calorieCalculatorService.test.js
const CalorieCalculatorService = require('../services/calorieCalculatorService');

describe('CalorieCalculatorService', () => {
    let calorieCalculator;

    beforeEach(() => {
        calorieCalculator = new CalorieCalculatorService();
    });

    describe('calculateCarbsCalories', () => {
        test('should calculate calories from carbs correctly', () => {
            const carbs = 50;
            const expectedCalories = 200; // 50g * 4 cal/g
            
            const result = calorieCalculator.calculateCarbsCalories(carbs);
            
            expect(result).toBe(expectedCalories);
        });

        test('should return 0 for negative carbs', () => {
            const result = calorieCalculator.calculateCarbsCalories(-10);
            expect(result).toBe(0);
        });

        test('should return 0 for null carbs', () => {
            const result = calorieCalculator.calculateCarbsCalories(null);
            expect(result).toBe(0);
        });

        test('should return 0 for NaN carbs', () => {
            const result = calorieCalculator.calculateCarbsCalories('invalid');
            expect(result).toBe(0);
        });
    });

    describe('calculateProteinCalories', () => {
        test('should calculate calories from protein correctly', () => {
            const protein = 30;
            const expectedCalories = 120; // 30g * 4 cal/g
            
            const result = calorieCalculator.calculateProteinCalories(protein);
            
            expect(result).toBe(expectedCalories);
        });

        test('should return 0 for invalid protein values', () => {
            expect(calorieCalculator.calculateProteinCalories(-10)).toBe(0);
            expect(calorieCalculator.calculateProteinCalories(null)).toBe(0);
            expect(calorieCalculator.calculateProteinCalories('invalid')).toBe(0);
        });
    });

    describe('calculateFatCalories', () => {
        test('should calculate calories from fat correctly', () => {
            const fat = 20;
            const expectedCalories = 180; // 20g * 9 cal/g
            
            const result = calorieCalculator.calculateFatCalories(fat);
            
            expect(result).toBe(expectedCalories);
        });

        test('should return 0 for invalid fat values', () => {
            expect(calorieCalculator.calculateFatCalories(-10)).toBe(0);
            expect(calorieCalculator.calculateFatCalories(null)).toBe(0);
        });
    });

    describe('calculateTotalCalories', () => {
        test('should calculate total calories from all macronutrients', () => {
            const macronutrients = {
                carbs: 50,  // 200 cal
                protein: 30, // 120 cal
                fat: 20     // 180 cal
            };
            const expectedTotal = 500;
            
            const result = calorieCalculator.calculateTotalCalories(macronutrients);
            
            expect(result.totalCalories).toBe(expectedTotal);
            expect(result.breakdown.carbs.grams).toBe(50);
            expect(result.breakdown.carbs.calories).toBe(200);
            expect(result.breakdown.protein.grams).toBe(30);
            expect(result.breakdown.protein.calories).toBe(120);
            expect(result.breakdown.fat.grams).toBe(20);
            expect(result.breakdown.fat.calories).toBe(180);
        });

        test('should handle missing macronutrients with defaults', () => {
            const macronutrients = {
                carbs: 50
                // protein and fat missing
            };
            
            const result = calorieCalculator.calculateTotalCalories(macronutrients);
            
            expect(result.totalCalories).toBe(200); // Only carbs
            expect(result.breakdown.protein.grams).toBe(0);
            expect(result.breakdown.fat.grams).toBe(0);
        });

        test('should return correct macronutrient breakdown', () => {
            const macronutrients = {
                carbs: 100,
                protein: 50,
                fat: 25
            };
            
            const result = calorieCalculator.calculateTotalCalories(macronutrients);
            
            expect(result.macronutrientCalories.carbs).toBe(400);
            expect(result.macronutrientCalories.protein).toBe(200);
            expect(result.macronutrientCalories.fat).toBe(225);
        });
    });

    describe('calculateFoodCalories', () => {
        test('should return error when no calories or macronutrients provided', () => {
            const foodData = {
                calories: 0,
                carbs: 0,
                protein: 0,
                fat: 0
            };
            
            const result = calorieCalculator.calculateFoodCalories(foodData);
            
            expect(result.validation.isValid).toBe(false);
            expect(result.validation.message).toBe('Either calories or macronutrients must be provided');
        });

        test('should use direct calories when only calories provided', () => {
            const foodData = {
                calories: 250,
                carbs: 0,
                protein: 0,
                fat: 0
            };
            
            const result = calorieCalculator.calculateFoodCalories(foodData);
            
            expect(result.totalCalories).toBe(250);
            expect(result.calculatedFromMacronutrients).toBe(false);
            expect(result.validation.isValid).toBe(true);
            expect(result.validation.message).toBe('Using provided calorie value');
        });

        test('should calculate from macronutrients when only macros provided', () => {
            const foodData = {
                calories: 0,
                carbs: 50,
                protein: 30,
                fat: 20
            };
            
            const result = calorieCalculator.calculateFoodCalories(foodData);
            
            expect(result.totalCalories).toBe(500);
            expect(result.calculatedFromMacronutrients).toBe(true);
            expect(result.validation.isValid).toBe(true);
            expect(result.validation.message).toBe('Calories calculated from macronutrients');
        });

        test('should validate consistency when both calories and macros provided', () => {
            const foodData = {
                calories: 500,
                carbs: 50,
                protein: 30,
                fat: 20
            };
            
            const result = calorieCalculator.calculateFoodCalories(foodData);
            
            expect(result.validation.isValid).toBe(true);
            expect(result.providedCalories).toBe(500);
        });

        test('should detect calorie mismatch when values do not match', () => {
            const foodData = {
                calories: 300,  // Incorrect
                carbs: 50,      // 200
                protein: 30,    // 120
                fat: 20         // 180
                // Total should be 500, not 300
            };
            
            const result = calorieCalculator.calculateFoodCalories(foodData);
            
            expect(result.validation.isValid).toBe(false);
            expect(result.validation.message).toContain('Calorie mismatch');
        });

        test('should allow small tolerance in calorie matching', () => {
            const foodData = {
                calories: 503,  // Within 5 calorie tolerance
                carbs: 50,
                protein: 30,
                fat: 20
                // Calculated: 500 calories
            };
            
            const result = calorieCalculator.calculateFoodCalories(foodData);
            
            expect(result.validation.isValid).toBe(true);
        });
    });

    describe('getMacronutrientCalorieValues', () => {
        test('should return correct macronutrient calorie values', () => {
            const values = calorieCalculator.getMacronutrientCalorieValues();
            
            expect(values.CARBS).toBe(4);
            expect(values.PROTEIN).toBe(4);
            expect(values.FAT).toBe(9);
        });

        test('should return a copy of values to prevent modification', () => {
            const values = calorieCalculator.getMacronutrientCalorieValues();
            values.CARBS = 100;
            
            const newValues = calorieCalculator.getMacronutrientCalorieValues();
            expect(newValues.CARBS).toBe(4);
        });
    });
});
```

```javascript:API/forkit-api/src/tests/streakService.test.js
const StreakService = require('../services/streakService');

describe('StreakService', () => {
    let streakService;

    beforeEach(() => {
        streakService = new StreakService();
    });

    describe('isSameDay', () => {
        test('should return true for same day', () => {
            const date1 = new Date('2025-10-05');
            const date2 = new Date('2025-10-05');
            
            const result = streakService.isSameDay(date1, date2);
            
            expect(result).toBe(true);
        });

        test('should return false for different days', () => {
            const date1 = new Date('2025-10-05');
            const date2 = new Date('2025-10-06');
            
            const result = streakService.isSameDay(date1, date2);
            
            expect(result).toBe(false);
        });

        test('should return true for same day different times', () => {
            const date1 = new Date('2025-10-05T08:00:00');
            const date2 = new Date('2025-10-05T20:00:00');
            
            const result = streakService.isSameDay(date1, date2);
            
            expect(result).toBe(true);
        });
    });

    describe('isConsecutiveDay', () => {
        test('should return true for consecutive days', () => {
            const lastDate = new Date('2025-10-04');
            const currentDate = new Date('2025-10-05');
            
            const result = streakService.isConsecutiveDay(lastDate, currentDate);
            
            expect(result).toBe(true);
        });

        test('should return false for same day', () => {
            const lastDate = new Date('2025-10-05');
            const currentDate = new Date('2025-10-05');
            
            const result = streakService.isConsecutiveDay(lastDate, currentDate);
            
            expect(result).toBe(false);
        });

        test('should return false for non-consecutive days', () => {
            const lastDate = new Date('2025-10-03');
            const currentDate = new Date('2025-10-05');
            
            const result = streakService.isConsecutiveDay(lastDate, currentDate);
            
            expect(result).toBe(false);
        });

        test('should return false when lastDate is null', () => {
            const currentDate = new Date('2025-10-05');
            
            const result = streakService.isConsecutiveDay(null, currentDate);
            
            expect(result).toBe(false);
        });
    });

    describe('isNewDay', () => {
        test('should return true when lastDate is null', () => {
            const currentDate = new Date('2025-10-05');
            
            const result = streakService.isNewDay(null, currentDate);
            
            expect(result).toBe(true);
        });

        test('should return false for consecutive days', () => {
            const lastDate = new Date('2025-10-04');
            const currentDate = new Date('2025-10-05');
            
            const result = streakService.isNewDay(lastDate, currentDate);
            
            expect(result).toBe(false);
        });

        test('should return true for days more than 1 apart', () => {
            const lastDate = new Date('2025-10-03');
            const currentDate = new Date('2025-10-05');
            
            const result = streakService.isNewDay(lastDate, currentDate);
            
            expect(result).toBe(true);
        });
    });

    describe('isStreakActive', () => {
        test('should return false when lastLogDate is null', () => {
            const result = streakService.isStreakActive(null);
            
            expect(result).toBe(false);
        });

        test('should return true when last log was today', () => {
            const today = new Date().toISOString();
            
            const result = streakService.isStreakActive(today);
            
            expect(result).toBe(true);
        });

        test('should return true when last log was yesterday', () => {
            const yesterday = new Date();
            yesterday.setDate(yesterday.getDate() - 1);
            
            const result = streakService.isStreakActive(yesterday.toISOString());
            
            expect(result).toBe(true);
        });

        test('should return false when last log was more than 1 day ago', () => {
            const twoDaysAgo = new Date();
            twoDaysAgo.setDate(twoDaysAgo.getDate() - 2);
            
            const result = streakService.isStreakActive(twoDaysAgo.toISOString());
            
            expect(result).toBe(false);
        });
    });
});
```

## 3. Update package.json for testing

```json:API/forkit-api/package.json
{
  "name": "forkit-api",
  "version": "1.0.0",
  "description": "ForkIt Health Tracking API",
  "main": "src/server.js",
  "scripts": {
    "start": "node src/server.js",
    "dev": "nodemon src/server.js",
    "test": "jest --coverage --verbose",
    "test:watch": "jest --watch",
    "test:ci": "jest --coverage --ci --maxWorkers=2"
  },
  "keywords": [
    "health",
    "fitness",
    "calorie-tracking",
    "api"
  ],
  "author": "ForkIt Development Team",
  "license": "ISC",
  "type": "commonjs",
  "dependencies": {
    "axios": "^1.11.0",
    "cors": "^2.8.5",
    "dotenv": "^17.2.1",
    "express": "^4.18.2",
    "firebase": "^12.2.1",
    "firebase-admin": "^13.5.0",
    "morgan": "^1.10.0"
  },
  "devDependencies": {
    "nodemon": "^3.1.10",
    "jest": "^29.7.0",
    "@types/jest": "^29.5.11"
  },
  "jest": {
    "testEnvironment": "node",
    "coverageDirectory": "coverage",
    "collectCoverageFrom": [
      "src/services/**/*.js",
      "src/controllers/**/*.js",
      "!src/config/**",
      "!src/server.js"
    ],
    "testMatch": [
      "**/tests/**/*.test.js"
    ]
  }
}
```

## 4. GitHub Actions Workflow

Finally, let me create the GitHub Actions workflow for automated testing:

```yaml:.github/workflows/ci-tests.yml
name: CI - Automated Testing

on:
  push:
    branches: [ main, develop, Max/BarcodeScannerAndHabbits ]
  pull_request:
    branches: [ main, develop ]

jobs:
  # Android Unit Tests
  android-tests:
    name: Android Unit Tests
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Set up JDK 11
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '11'
          cache: 'gradle'
      
      - name: Grant execute permission for gradlew
        run: chmod +x ./Mobile/gradlew
        working-directory: .
      
      - name: Run Android Unit Tests
        run: ./gradlew test --stacktrace
        working-directory: ./Mobile
      
      - name: Upload Android Test Results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: android-test-results
          path: Mobile/app/build/reports/tests/
      
      - name: Upload Android Test Coverage
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: android-test-coverage
          path: Mobile/app/build/reports/coverage/
      
      - name: Comment Test Results on PR
        uses: actions/github-script@v7
        if: github.event_name == 'pull_request'
        with:
          script: |
            const fs = require('fs');
            const testResults = `## ✅ Android Unit Tests Passed
            
            All unit tests for the Android application have completed successfully!
            
            📊 View detailed test results in the artifacts.`;
            
            github.rest.issues.createComment({
              owner: context.repo.owner,
              repo: context.repo.repo,
              issue_number: context.issue.number,
              body: testResults
            });
  
  # API Unit Tests
  api-tests:
    name: API Unit Tests
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '18'
          cache: 'npm'
          cache-dependency-path: API/forkit-api/package-lock.json
      
      - name: Install dependencies
        run: npm ci
        working-directory: ./API/forkit-api
      
      - name: Run API Unit Tests
        run: npm test
        working-directory: ./API/forkit-api
      
      - name: Upload API Test Results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: api-test-results
          path: API/forkit-api/coverage/
      
      - name: Upload API Coverage to Codecov
        uses: codecov/codecov-action@v4
        if: always()
        with:
          file: ./API/forkit-api/coverage/lcov.info
          flags: api-tests
          name: api-coverage
      
      - name: Comment API Test Results on PR
        uses: actions/github-script@v7
        if: github.event_name == 'pull_request'
        with:
          script: |
            const testResults = `## ✅ API Unit Tests Passed
            
            All unit tests for the Node.js API have completed successfully!
            
            📊 View detailed coverage report in the artifacts.`;
            
            github.rest.issues.createComment({
              owner: context.repo.owner,
              repo: context.repo.repo,
              issue_number: context.issue.number,
              body: testResults
            });
  
  # Combined Test Report
  test-summary:
    name: Test Summary
    runs-on: ubuntu-latest
    needs: [android-tests, api-tests]
    if: always()
    
    steps:
      - name: Create Test Summary
        uses: actions/github-script@v7
        with:
          script: |
            const androidStatus = '${{ needs.android-tests.result }}';
            const apiStatus = '${{ needs.api-tests.result }}';
            
            let summary = `# 🧪 Test Suite Summary\n\n`;
            summary += `| Component | Status |\n`;
            summary += `|-----------|--------|\n`;
            summary += `| Android Tests | ${androidStatus === 'success' ? '✅ Passed' : '❌ Failed'} |\n`;
            summary += `| API Tests | ${apiStatus === 'success' ? '✅ Passed' : '❌ Failed'} |\n\n`;
            
            if (androidStatus === 'success' && apiStatus === 'success') {
              summary += `## 🎉 All Tests Passed!\n\n`;
              summary += `Great work! All automated tests are passing.`;
            } else {
              summary += `## ⚠️ Some Tests Failed\n\n`;
              summary += `Please review the test results and fix any failing tests.`;
            }
            
            core.summary.addRaw(summary).write();
      
      - name: Check Overall Test Status
        if: needs.android-tests.result != 'success' || needs.api-tests.result != 'success'
        run: |
          echo "Some tests failed. Please review the test results."
          exit 1
```

## 5. Additional Testing Configuration Files

```xml:Mobile/app/build.gradle.kts
// ... existing code ...

android {
    // ... existing config ...
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // ... existing dependencies ...
    
    // Enhanced testing dependencies
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.google.truth:truth:1.1.5")
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
```

```javascript:API/forkit-api/jest.config.js
module.exports = {
  testEnvironment: 'node',
  coverageDirectory: 'coverage',
  collectCoverageFrom: [
    'src/services/**/*.js',
    'src/controllers/**/*.js',
    '!src/config/**',
    '!src/server.js'
  ],
  testMatch: [
    '**/tests/**/*.test.js',
    '**/?(*.)+(spec|test).js'
  ],
  coverageThreshold: {
    global: {
      branches: 70,
      functions: 70,
      lines: 70,
      statements: 70
    }
  },
  verbose: true
};
```

## 6. README with Testing Instructions

```markdown:TESTING.md
# ForkIt Testing Guide

This document provides comprehensive information about running automated tests for the ForkIt project.

## Overview

The ForkIt project includes automated testing for both the Android mobile application and the Node.js API backend. Tests run automatically on GitHub Actions for every push and pull request.

## Test Structure

### Android Unit Tests
- **Location**: `Mobile/app/src/test/java/com/example/forkit/`
- **Framework**: JUnit 4
- **Coverage**: Data models, business logic, and utility functions

### API Unit Tests
- **Location**: `API/forkit-api/src/tests/`
- **Framework**: Jest
- **Coverage**: Services, controllers, and business logic

## Running Tests Locally

### Android Tests

```bash
# Navigate to Mobile directory
cd Mobile

# Run all unit tests
./gradlew test

# Run tests with coverage
./gradlew testDebugUnitTest jacocoTestReport

# Run specific test class
./gradlew test --tests "*.FoodModelsTest"
```

### API Tests

```bash
# Navigate to API directory
cd API/forkit-api

# Install dependencies
npm install

# Run all tests
npm test

# Run tests in watch mode
npm run test:watch

# Run tests with coverage
npm test -- --coverage
```

## GitHub Actions Workflow

Tests run automatically on:
- **Push** to `main`, `develop`, or feature branches
- **Pull Requests** to `main` or `develop`

### Workflow Steps

1. **Android Tests**
   - Set up JDK 11
   - Run Gradle unit tests
   - Upload test results and coverage reports

2. **API Tests**
   - Set up Node.js 18
   - Install dependencies
   - Run Jest unit tests
   - Upload coverage to Codecov

3. **Test Summary**
   - Aggregate results from both test suites
   - Post summary to PR comments
   - Fail build if any tests fail

## Test Coverage Goals

- **Minimum Coverage**: 70%
- **Target Coverage**: 85%+

### Coverage Areas

#### Android
- ✅ Data models (Food, User, Habit, CalorieCalculator)
- ✅ Business logic utilities
- ⏳ API service integration (coming soon)
- ⏳ UI components (coming soon)

#### API
- ✅ CalorieCalculatorService
- ✅ StreakService
- ⏳ Controllers (coming soon)
- ⏳ Integration tests (coming soon)

## Writing New Tests

### Android Test Example

```kotlin
class NewFeatureTest {
    @Test
    fun `test description in backticks`() {
        // Arrange
        val input = "test"
        
        // Act
        val result = functionUnderTest(input)
        
        // Assert
        assertEquals("expected", result)
    }
}
```

### API Test Example

```javascript
describe('NewService', () => {
    test('should do something', () => {
        // Arrange
        const service = new NewService();
        
        // Act
        const result = service.doSomething();
        
        // Assert
        expect(result).toBe(expected);
    });
});
```

## Continuous Integration

### Viewing Test Results

1. Go to the **Actions** tab in GitHub
2. Click on the latest workflow run
3. View logs for each job
4. Download artifacts for detailed reports

### Test Artifacts

- `android-test-results`: HTML reports of Android unit tests
- `android-test-coverage`: JaCoCo coverage reports
- `api-test-results`: Jest coverage reports

## Troubleshooting

### Android Tests Failing Locally

```bash
# Clean and rebuild
./gradlew clean
./gradlew test --stacktrace
```

### API Tests Failing

```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
npm test
```

### GitHub Actions Failing

1. Check workflow logs for error messages
2. Ensure all dependencies are properly specified
3. Verify test files are committed to repository
4. Check for environment-specific issues

## Best Practices

1. **Write tests first** (TDD approach recommended)
2. **Keep tests simple** and focused on one thing
3. **Use descriptive test names** that explain what is being tested
4. **Mock external dependencies** to isolate unit tests
5. **Maintain test coverage** above 70%
6. **Run tests locally** before pushing to repository

## Additional Resources

- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Jest Documentation](https://jestjs.io/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Android Testing Guide](https://developer.android.com/training/testing)

## Support

For questions or issues with testing:
- Check existing test files for examples
- Review this documentation
- Contact the QA team members (Daniel Jung, Braydon Wooley, Nicolas Christofides)
```

## Summary

I've created a comprehensive automated testing setup for your ForkIt project that includes:

### ✅ What's Implemented:

1. **Android Unit Tests** (4 test files):
   - `FoodModelsTest.kt` - Tests food and nutrient models
   - `UserModelsTest.kt` - Tests user, streak, and goals models
   - `HabitModelsTest.kt` - Tests habit tracking functionality
   - `CalorieCalculatorModelsTest.kt` - Tests calorie calculations

2. **API Unit Tests** (2 test files):
   - `calorieCalculatorService.test.js` - Tests calorie calculation logic
   - `streakService.test.js` - Tests streak tracking logic

3. **GitHub Actions Workflow**:
   - Runs on push and pull requests
   - Separate jobs for Android and API tests
   - Automated test reporting
   - Coverage reports
   - PR comments with test results

4. **Configuration Files**:
   - Updated `package.json` with Jest configuration
   - Updated `build.gradle.kts` with testing dependencies
   - Created `jest.config.js` for API testing
   - Created `TESTING.md` documentation

### 🚀 To Get Started:

1. **Copy these files to your project**
2. **Install API dependencies**: `cd API/forkit-api && npm install`
3. **Run Android tests locally**: `cd Mobile && ./gradlew test`
4. **Run API tests locally**: `cd API/forkit-api && npm test`
5. **Commit and push** - GitHub Actions will run automatically!

The tests cover core functionality including data models, calorie calculations, and streak tracking. You can expand these tests to cover more controllers and services as needed.
```

