# ForkIt API - Calorie Counting Application Endpoints

This document outlines all the API endpoints for the ForkIt calorie counting application.

## Base URL
```
http://localhost:3000/api
```

## Authentication
All endpoints require a `userId` to identify the user making the request.

---

## 🎯 User Goals Endpoints (`/api/users/:id/goals`)

### Get User Goals
```
GET /api/users/:id/goals
```
**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "user123",
    "dailyCalories": 2500,
    "dailyWater": 2500,
    "dailySteps": 10000,
    "weeklyExercises": 5,
    "updatedAt": "2025-10-01T12:00:00.000Z"
  },
  "message": "User goals retrieved successfully"
}
```

### Update User Goals
```
PUT /api/users/:id/goals
```
**Request Body:**
```json
{
  "dailyCalories": 2500,
  "dailyWater": 2500,
  "dailySteps": 10000,
  "weeklyExercises": 5
}
```

**Validation Rules:**
- `dailyCalories`: 1200 - 10000 kcal
- `dailyWater`: 500 - 10000 ml
- `dailySteps`: 0 - 50000 steps
- `weeklyExercises`: 0 - 21 sessions

**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "user123",
    "dailyCalories": 2500,
    "dailyWater": 2500,
    "dailySteps": 10000,
    "weeklyExercises": 5,
    "updatedAt": "2025-10-01T12:00:00.000Z"
  },
  "message": "User goals updated successfully"
}
```

**Note:** You can update individual goals or all at once. Unspecified goals will retain their current values.

---

## 📝 Food Logging Endpoints (`/api/food-logs`)

### Get All Food Logs
```
GET /api/food-logs
```
**Query Parameters:**
- `userId` (optional): Filter by user ID
- `date` (optional): Filter by specific date

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "doc_id",
      "userId": "user123",
      "foodName": "Apple",
      "servingSize": 1.5,
      "measuringUnit": "Cup",
      "date": "2024-01-15",
      "mealType": "Breakfast",
      "calories": 75,
      "carbs": 19,
      "fat": 0.2,
      "protein": 0.4,
      "foodId": "food123",
      "createdAt": "2024-01-15T08:30:00.000Z",
      "updatedAt": "2024-01-15T08:30:00.000Z"
    }
  ],
  "message": "Food logs retrieved successfully"
}
```

### Create Food Log Entry
```
POST /api/food-logs
```
**Request Body:**
```json
{
  "userId": "user123",
  "foodName": "Apple",
  "servingSize": 1.5,
  "measuringUnit": "Cup",
  "date": "2024-01-15",
  "mealType": "Breakfast",
  "calories": 75,
  "carbs": 19,
  "fat": 0.2,
  "protein": 0.4,
  "foodId": "food123"
}
```

**Required Fields:**
- `userId`, `foodName`, `servingSize`, `measuringUnit`, `date`, `mealType`

**Measuring Units:** Cup, ML, Grams, Ounces, Tablespoons, Teaspoons, etc.

**Meal Types:** Breakfast, Lunch, Dinner, Snacks

### Get Food Logs by Date Range
```
GET /api/food-logs/date-range?userId=user123&startDate=2024-01-01&endDate=2024-01-31
```

### Get Food Log by ID
```
GET /api/food-logs/:id
```

### Update Food Log
```
PUT /api/food-logs/:id
```

### Delete Food Log
```
DELETE /api/food-logs/:id
```

### Get Daily Calorie Summary
```
GET /api/food-logs/daily-summary?userId=user123&date=2024-01-15
```
**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "user123",
    "date": "2024-01-15",
    "totalCalories": 1000,
    "totalCarbs": 120,
    "totalFat": 45,
    "totalProtein": 60,
    "mealDistribution": [
      {
        "mealType": "Breakfast",
        "calories": 300,
        "percentage": 30
      },
      {
        "mealType": "Lunch",
        "calories": 400,
        "percentage": 40
      },
      {
        "mealType": "Dinner",
        "calories": 300,
        "percentage": 30
      }
    ],
    "mealTotals": {
      "Breakfast": 300,
      "Lunch": 400,
      "Dinner": 300,
      "Snacks": 0
    },
    "entryCount": 3
  },
  "message": "Daily calorie summary retrieved successfully"
}
```

### Get Monthly Calorie Summary
```
GET /api/food-logs/monthly-summary?userId=user123&year=2024&month=1
```
**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "user123",
    "year": 2024,
    "month": 1,
    "totalCalories": 2500,
    "totalCarbs": 300,
    "totalFat": 112,
    "totalProtein": 150,
    "averageDailyCalories": 1250,
    "daysWithData": 2,
    "dailyTotals": {
      "2024-01-15": {
        "calories": 1000,
        "carbs": 120,
        "fat": 45,
        "protein": 60,
        "entryCount": 3
      }
    },
    "entryCount": 6
  },
  "message": "Monthly calorie summary retrieved successfully"
}
```

### Get Recent Food Activity
```
GET /api/food-logs/recent-activity?userId=user123&limit=10
```
**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "user123",
    "recentActivity": [
      {
        "id": "doc_id",
        "foodName": "Apple",
        "servingSize": 1.5,
        "measuringUnit": "Cup",
        "calories": 75,
        "mealType": "Breakfast",
        "date": "2024-01-15",
        "createdAt": "2024-01-15T08:30:00.000Z",
        "time": "08:30 AM"
      }
    ],
    "count": 1
  },
  "message": "Recent food activity retrieved successfully"
}
```

### Get Calorie Trends
```
GET /api/food-logs/trends?userId=user123&startDate=2024-01-01&endDate=2024-01-31&groupBy=day
```
**Query Parameters:**
- `groupBy`: day, week, or month

**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "user123",
    "startDate": "2024-01-01",
    "endDate": "2024-01-31",
    "groupBy": "day",
    "trends": [
      {
        "date": "2024-01-15",
        "calories": 1000,
        "carbs": 120,
        "fat": 45,
        "protein": 60,
        "entryCount": 3
      }
    ],
    "totalDays": 1
  },
  "message": "Calorie trends retrieved successfully"
}
```

### Get Dashboard Data
```
GET /api/food-logs/dashboard?userId=user123&date=2024-01-15&year=2024&month=1
```
**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "user123",
    "date": "2024-01-15",
    "year": 2024,
    "month": 1,
    "daily": {
      "totalCalories": 1000,
      "totalCarbs": 120,
      "totalFat": 45,
      "totalProtein": 60,
      "mealDistribution": [
        {
          "mealType": "Breakfast",
          "calories": 300,
          "percentage": 30
        }
      ],
      "remainingCalories": 1000,
      "dailyGoal": 2000,
      "entryCount": 3
    },
    "monthly": {
      "consumed": 2500,
      "averageDaily": 1250,
      "daysWithData": 2,
      "dailyBreakdown": [
        {
          "date": "2024-01-15",
          "calories": 1000
        }
      ]
    },
    "recentActivity": {
      "entries": [...],
      "count": 10
    },
    "summary": {
      "totalCaloricIntake": 1000,
      "consumed": 2500,
      "remaining": 1000,
      "mealBreakdown": [...]
    }
  },
  "message": "Dashboard data retrieved successfully"
}
```

---

## 🍳 Meal Logging Endpoints (`/api/meal-logs`)

### Get All Meal Logs
```
GET /api/meal-logs
```

### Create Meal Log Entry
```
POST /api/meal-logs
```
**Request Body:**
```json
{
  "userId": "user123",
  "name": "Chicken Stir Fry",
  "description": "Healthy chicken stir fry with vegetables",
  "ingredients": [
    {
      "name": "Chicken Breast",
      "amount": 200,
      "unit": "grams"
    },
    {
      "name": "Broccoli",
      "amount": 150,
      "unit": "grams"
    }
  ],
  "instructions": [
    "Cut chicken into strips",
    "Heat oil in pan",
    "Cook chicken until golden",
    "Add vegetables and cook until tender"
  ],
  "totalCalories": 450,
  "totalCarbs": 25,
  "totalFat": 12,
  "totalProtein": 45,
  "servings": 2,
  "date": "2024-01-15",
  "mealType": "Dinner"
}
```

**Required Fields:**
- `userId`, `name`, `ingredients`, `instructions`, `date`

### Other Meal Log Endpoints
- `GET /api/meal-logs/date-range` - Get meals by date range
- `GET /api/meal-logs/:id` - Get meal by ID
- `PUT /api/meal-logs/:id` - Update meal
- `DELETE /api/meal-logs/:id` - Delete meal

---

## 💧 Water Logging Endpoints (`/api/water-logs`)

### Get All Water Logs
```
GET /api/water-logs
```

### Create Water Log Entry
```
POST /api/water-logs
```
**Request Body:**
```json
{
  "userId": "user123",
  "amount": 250,
  "date": "2024-01-15"
}
```

**Required Fields:**
- `userId`, `amount` (in milliliters), `date`

### Get Daily Water Total
```
GET /api/water-logs/daily-total?userId=user123&date=2024-01-15
```
**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "user123",
    "date": "2024-01-15",
    "totalAmount": 1500,
    "entries": 6
  },
  "message": "Daily water total retrieved successfully"
}
```

### Other Water Log Endpoints
- `GET /api/water-logs/date-range` - Get water logs by date range
- `GET /api/water-logs/:id` - Get water log by ID
- `PUT /api/water-logs/:id` - Update water log
- `DELETE /api/water-logs/:id` - Delete water log

---

## 🏃 Exercise Logging Endpoints (`/api/exercise-logs`)

### Get All Exercise Logs
```
GET /api/exercise-logs
```

### Create Exercise Log Entry
```
POST /api/exercise-logs
```
**Request Body:**
```json
{
  "userId": "user123",
  "name": "Morning Jog",
  "date": "2024-01-15",
  "caloriesBurnt": 300,
  "type": "Cardio",
  "duration": 30,
  "notes": "Ran in the park"
}
```

**Required Fields:**
- `userId`, `name`, `date`, `caloriesBurnt`, `type`

**Exercise Types:** Cardio, Strength

### Get Daily Exercise Total
```
GET /api/exercise-logs/daily-total?userId=user123&date=2024-01-15
```
**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "user123",
    "date": "2024-01-15",
    "totalCaloriesBurnt": 450,
    "totalDuration": 60,
    "totalExercises": 2,
    "cardioExercises": 1,
    "strengthExercises": 1,
    "entries": [...]
  },
  "message": "Daily exercise total retrieved successfully"
}
```

### Other Exercise Log Endpoints
- `GET /api/exercise-logs/date-range` - Get exercises by date range
- `GET /api/exercise-logs/:id` - Get exercise by ID
- `PUT /api/exercise-logs/:id` - Update exercise
- `DELETE /api/exercise-logs/:id` - Delete exercise

---

## 🧮 Calorie Calculator Endpoints (`/api/calorie-calculator`)

### Get Macronutrient Calorie Values
```
GET /api/calorie-calculator/macronutrient-values
```
**Response:**
```json
{
  "success": true,
  "data": {
    "macronutrientCalories": {
      "CARBS": 4,
      "PROTEIN": 4,
      "FAT": 9
    },
    "description": {
      "carbs": "4 calories per gram of carbohydrates",
      "protein": "4 calories per gram of protein",
      "fat": "9 calories per gram of fat"
    }
  },
  "message": "Macronutrient calorie values retrieved successfully"
}
```

### Calculate Total Calories from Macronutrients
```
POST /api/calorie-calculator/calculate
```
**Request Body:**
```json
{
  "carbs": 50,
  "protein": 30,
  "fat": 20
}
```
**Response:**
```json
{
  "success": true,
  "data": {
    "totalCalories": 560,
    "breakdown": {
      "carbs": {
        "grams": 50,
        "calories": 200
      },
      "protein": {
        "grams": 30,
        "calories": 120
      },
      "fat": {
        "grams": 20,
        "calories": 180
      }
    },
    "macronutrientCalories": {
      "carbs": 200,
      "protein": 120,
      "fat": 180
    }
  },
  "message": "Calories calculated successfully"
}
```

### Calculate Individual Macronutrient Calories
```
POST /api/calorie-calculator/individual
```
**Request Body:**
```json
{
  "macronutrient": "carbs",
  "grams": 25
}
```
**Response:**
```json
{
  "success": true,
  "data": {
    "macronutrient": "carbs",
    "grams": 25,
    "calories": 100,
    "caloriesPerGram": 4
  },
  "message": "carbs calories calculated successfully"
}
```

### Calculate Food Calories (Smart Calculation)
```
POST /api/calorie-calculator/food-calories
```
**Request Body:**
```json
{
  "calories": 0,
  "carbs": 50,
  "protein": 30,
  "fat": 20
}
```
**Response:**
```json
{
  "success": true,
  "data": {
    "totalCalories": 560,
    "calculatedFromMacronutrients": true,
    "breakdown": {
      "carbs": {
        "grams": 50,
        "calories": 200
      },
      "protein": {
        "grams": 30,
        "calories": 120
      },
      "fat": {
        "grams": 20,
        "calories": 180
      }
    },
    "validation": {
      "isValid": true,
      "message": "Calories calculated from macronutrients"
    }
  },
  "message": "Food calories calculated successfully"
}
```

**Features:**
- **Smart Calculation:** Automatically calculates calories from macronutrients when no direct calorie value is provided
- **Validation:** Compares provided calories with calculated macronutrient calories for consistency
- **Individual Calculations:** Calculate calories for each macronutrient separately
- **Flexible Input:** Accepts any combination of calories and macronutrients
- **Real-time Ready:** Perfect for mobile app real-time calorie calculation as user types

---

## 🗄️ Database Collections

The API creates the following Firestore collections:

1. **foodLogs** - Individual food item logs
2. **mealLogs** - Complete meal recipes and logs
3. **waterLogs** - Water consumption tracking
4. **exerciseLogs** - Exercise activity logs

Each document automatically includes:
- `createdAt` - ISO timestamp when created
- `updatedAt` - ISO timestamp when last updated

---

## 🚀 Getting Started

1. **Start the API server:**
   ```bash
   npm run dev
   ```

2. **Test the endpoints:**
   ```bash
   # Health check
   curl http://localhost:3000/api/health
   
   # Get all endpoints
   curl http://localhost:3000/
   ```

3. **Environment Variables Required:**
   - Firebase project configuration
   - Database URL
   - Storage bucket

---

## 📱 Android Integration Notes

For your Android Studio project, you can use these endpoints to:

1. **Food Logging:** Track individual food items with serving sizes and nutrition data
2. **Meal Logging:** Save complete meal recipes with ingredients and instructions
3. **Water Logging:** Track daily water intake in milliliters
4. **Exercise Logging:** Log workouts with calories burnt and exercise type

All endpoints support CRUD operations and include proper error handling and validation.
