# Meal Ingredients Troubleshooting Guide

## 🔍 Issue
Ingredients are not showing up when displaying meals, even though they were added during meal creation.

## ✅ What Was Fixed

### Type Mismatch Issue
There were **two different `MealIngredient` classes** with incompatible field names:

1. **`data.models.MealIngredient`** (UI layer)
   - Fields: `id`, `foodName`, `servingSize`, `measuringUnit`, `calories`, `carbs`, `fat`, `protein`

2. **`data.local.entities.MealIngredient`** (Database layer)
   - Fields: `foodName`, `quantity`, `unit`, `calories`, `carbs`, `fat`, `protein`

### Fix Applied
Updated `AddFullMealActivity.kt` to properly use type aliases and convert between the two types:

```kotlin
// Import with aliases
import com.example.forkit.data.models.MealIngredient as UIMealIngredient
import com.example.forkit.data.local.entities.MealIngredient as DBMealIngredient

// Proper conversion when saving
val mealIngredients = ingredients.map { ingredient ->
    DBMealIngredient(
        foodName = ingredient.foodName,
        quantity = ingredient.servingSize, // servingSize -> quantity
        unit = ingredient.measuringUnit,   // measuringUnit -> unit
        calories = ingredient.calories,
        carbs = ingredient.carbs,
        fat = ingredient.fat,
        protein = ingredient.protein
    )
}
```

## 🧪 How to Test

### Step 1: Create a New Meal
1. Open the app
2. Navigate to **Meals** tab
3. Click **"Create Meal"** button
4. Enter meal name: `"Test Meal"`
5. Enter description: `"Testing ingredients"`
6. Click **"Add Ingredient"** button
7. Search for and select an ingredient (e.g., "chicken")
8. Adjust serving size if needed
9. Click **"Add Ingredient"** to add it
10. **IMPORTANT:** Verify the ingredient appears in the list
11. Add 2-3 more ingredients
12. Click **"Add Meal"** button

### Step 2: Verify in Meals List
1. Go back to Meals tab
2. Look for your "Test Meal"
3. The card should show: `"3 ingredients: chicken, rice, broccoli"`
4. If it shows `"0 ingredients:"` - there's still an issue

### Step 3: View Meal Details
1. Tap on the "Test Meal" card
2. Scroll down to the "Ingredients" section
3. You should see all added ingredients with their amounts
4. If it says "No ingredients listed for this meal" - the ingredients weren't saved

## 🐛 Debug Checklist

### Check 1: Are ingredients being added to the list?
Look for this in Logcat when clicking "Add Ingredient":
```
MealsDebug: 🍴 Parsed MealIngredient: [ingredient name] | [calories] kcal
```

### Check 2: Are ingredients being saved?
Look for this when clicking "Add Meal":
```
MealsDebug: ✅ Meal created successfully: [meal name] - [id]
```

### Check 3: Are ingredients being retrieved?
Look for this when opening Meals tab:
```
MealsScreen: Loaded [X] meals from API
MealsScreen: Loaded [Y] meals from local database
```

### Check 4: What's in the ingredients list?
Add this temporary log before creating the meal (around line 445 in AddFullMealActivity):
```kotlin
Log.d(DEBUG_TAG, "MealsDebug: Creating meal with ${ingredients.size} ingredients")
ingredients.forEach { 
    Log.d(DEBUG_TAG, "  - ${it.foodName}: ${it.servingSize}${it.measuringUnit}")
}
```

## 🔧 Additional Debugging

### Test Direct Database Access
Check if ingredients are in the database:

1. Enable database inspector in Android Studio
2. Navigate to `meal_logs` table
3. Find your test meal
4. Check the `ingredients` column - it should contain JSON like:
```json
[
  {
    "foodName": "chicken",
    "quantity": 100.0,
    "unit": "g",
    "calories": 165.0,
    "carbs": 0.0,
    "fat": 3.6,
    "protein": 31.0
  }
]
```

### Test API Response
If the meal was synced to the server, check the API response:
```bash
curl "http://localhost:3000/api/meal-logs?userId=YOUR_USER_ID"
```

Look for your meal and check if `ingredients` array is populated.

## 🎯 Most Likely Issues

### Issue 1: Ingredients List is Empty
**Symptom:** Meal saves but shows 0 ingredients

**Cause:** The `ingredients` list in AddFullMealActivity might be cleared before saving

**Fix:** Check that `ingredients.clear()` on line 498 happens AFTER the meal is created, not before

### Issue 2: TypeConverter Not Working
**Symptom:** Ingredients save but don't load from database

**Cause:** Room TypeConverter might not be properly converting the list

**Fix:** Verify `Converters.kt` is registered in AppDatabase

### Issue 3: Null Ingredients from API
**Symptom:** Ingredients show locally but not after sync

**Cause:** API might be returning null or empty ingredients array

**Fix:** Check API response format matches expected structure

## ✅ Verification Steps After Fix

Run these checks to confirm everything works:

1. ✅ Create meal with 3 ingredients
2. ✅ See "3 ingredients: ..." in meal card
3. ✅ Open meal detail and see all 3 ingredients listed
4. ✅ Close app and reopen - ingredients still there
5. ✅ Go offline, create meal, go online - ingredients still there
6. ✅ Log meal to today - ingredients show in food log

## 📝 Expected Behavior

### In Meals List:
```
Test Meal
3 ingredients: chicken, rice, broccoli
650 kcal
```

### In Meal Detail:
```
Ingredients
• chicken (100 g)
• rice (150 g)
• broccoli (80 g)
```

## 🚨 If Still Not Working

If ingredients still don't show after the fix:

1. **Clean and rebuild** the app:
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

2. **Clear app data** to reset the database

3. **Check Logcat** for any errors during:
   - Adding ingredients
   - Creating meal
   - Loading meals
   - Opening meal details

4. **Verify the fix was applied:**
   - Check `AddFullMealActivity.kt` line 39-40 has the type aliases
   - Check `AddFullMealActivity.kt` line 451-462 has the conversion logic

## 📊 Files Modified

- ✅ `Mobile/app/src/main/java/com/example/forkit/ui/meals/AddFullMealActivity.kt`
  - Added type aliases for MealIngredient classes
  - Updated ingredient list type to use UIMealIngredient
  - Clarified conversion logic with better comments

## 🎉 Next Steps

1. **Rebuild the app** with the changes
2. **Test creating a new meal** with ingredients
3. **Verify ingredients appear** in both list and detail views
4. **Check Logcat** for any warnings or errors
5. If still having issues, share the Logcat output filtered by "MealsDebug"

---

**Status:** Type mismatch fixed. Test with a new meal to verify ingredients now save and display correctly.

