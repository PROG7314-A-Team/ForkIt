const axios = require("axios");
const FirebaseService = require("./firebaseService");

class FoodSearchService {
  constructor() {
    this.foodService = new FirebaseService("food");
  }

  /**
   * Enhanced food search with scoring and filtering
   * @param {string} searchTerm - The food name to search for
   * @param {number} maxResults - Maximum number of results to return (default: 25)
   * @returns {Array} - Sorted array of scored food items
   */
  async searchFoodByName(searchTerm, maxResults = 25) {
    try {
      // Get results from OpenFoodFacts
      const openFoodFactsResults = await this.getOpenFoodFactsResults(
        searchTerm
      );

      // Score and filter results
      const scoredResults = this.scoreAndFilterResults(
        openFoodFactsResults,
        searchTerm
      );

      // Sort by score and limit results
      const sortedResults = scoredResults
        .sort((a, b) => b.totalScore - a.totalScore)
        .slice(0, maxResults);

      // If we don't have enough results from OpenFoodFacts, try local database
      if (sortedResults.length < maxResults) {
        const localResults = await this.getLocalDatabaseResults(searchTerm);
        const scoredLocalResults = this.scoreAndFilterResults(
          localResults,
          searchTerm,
          true
        );

        // Merge and deduplicate results
        const mergedResults = this.mergeResults(
          sortedResults,
          scoredLocalResults,
          maxResults
        );
        return mergedResults;
      }

      return sortedResults;
    } catch (error) {
      console.error("Error in enhanced food search:", error);
      throw error;
    }
  }

  /**
   * Get results from OpenFoodFacts API
   */
  async getOpenFoodFactsResults(searchTerm) {
    try {
      const response = await axios.get(
        `https://world.openfoodfacts.org/cgi/search.pl?search_terms=${searchTerm}&json=1&countries_tags=South_Africa`
      );

      if (response.data.count > 0) {
        return response.data.products || [];
      }
      return [];
    } catch (error) {
      console.error("Error fetching from OpenFoodFacts:", error);
      return [];
    }
  }

  /**
   * Get results from local database
   */
  async getLocalDatabaseResults(searchTerm) {
    try {
      const localResults = await this.foodService.query([
        { field: "name", operator: "==", value: searchTerm },
      ]);
      return localResults || [];
    } catch (error) {
      console.error("Error fetching from local database:", error);
      return [];
    }
  }

  /**
   * Score and filter food results based on the scoring algorithm
   */
  scoreAndFilterResults(products, searchTerm, isLocal = false) {
    const scoredResults = [];

    for (const product of products) {
      if (!product || !product.product_name) continue;

      const score = this.calculateFoodScore(product, searchTerm, isLocal);

      // Only include products with a positive score or from local database
      if (score.totalScore > 0 || isLocal) {
        scoredResults.push({
          ...product,
          scoreBreakdown: score,
          totalScore: score.totalScore,
          isLocal: isLocal,
        });
      }
    }

    return scoredResults;
  }

  /**
   * Calculate comprehensive food score based on the scoring algorithm
   */
  calculateFoodScore(product, searchTerm, isLocal = false) {
    const score = {
      dataCompleteness: 0,
      foodQuality: 0,
      nameRelevance: 0,
      simplicity: 0,
      totalScore: 0,
    };

    // Data completeness (high weight)
    score.dataCompleteness += this.scoreDataCompleteness(product);

    // Food quality indicators (medium weight)
    score.foodQuality += this.scoreFoodQuality(product);

    // Name relevance (high weight)
    score.nameRelevance += this.scoreNameRelevance(product, searchTerm);

    // Simplicity (low weight)
    score.simplicity += this.scoreSimplicity(product);

    // Calculate total score
    score.totalScore =
      score.dataCompleteness +
      score.foodQuality +
      score.nameRelevance +
      score.simplicity;

    return score;
  }

  /**
   * Score data completeness
   */
  scoreDataCompleteness(product) {
    let score = 0;

    // Has nutriscore? +10 points
    if (product.nutriscore_grade && product.nutriscore_grade !== "unknown") {
      score += 10;
    }

    // Has ecoscore? +5 points
    if (product.ecoscore_grade && product.ecoscore_grade !== "unknown") {
      score += 5;
    }

    // Has complete nutrition facts? +10 points
    const nutriments = product.nutriments || {};
    const hasCompleteNutrition =
      nutriments.carbohydrates_100g !== undefined &&
      nutriments.proteins_100g !== undefined &&
      nutriments.fat_100g !== undefined &&
      nutriments["energy-kcal_100g"] !== undefined;
    if (hasCompleteNutrition) {
      score += 10;
    }

    // Has ingredients list? +5 points
    if (
      product.ingredients_text &&
      product.ingredients_text.trim().length > 0
    ) {
      score += 5;
    }

    return score;
  }

  /**
   * Score food quality indicators
   */
  scoreFoodQuality(product) {
    let score = 0;

    // Nova group scoring
    const novaGroup = product.nova_group;
    if (novaGroup !== undefined) {
      switch (novaGroup) {
        case 1:
        case 2:
          score += 8;
          break;
        case 3:
          score += 3;
          break;
        case 4:
          score -= 5;
          break;
        default:
          break;
      }
    }

    return score;
  }

  /**
   * Score name relevance with fuzzy matching
   */
  scoreNameRelevance(product, searchTerm) {
    let score = 0;
    const productName = (product.product_name || "").toLowerCase();
    const searchTermLower = searchTerm.toLowerCase();

    // Exact search term match in name? +25 points
    if (productName === searchTermLower) {
      score += 25;
    }
    // Search term at start of name? +20 points
    else if (productName.startsWith(searchTermLower)) {
      score += 20;
    }
    // Contains search term? +10 points
    else if (productName.includes(searchTermLower)) {
      score += 10;
    }

    // Enhanced fuzzy matching for better relevance
    const fuzzyScore = this.calculateFuzzyMatchScore(
      productName,
      searchTermLower
    );
    score += fuzzyScore;

    // Word-based matching (higher weight for individual words)
    const wordScore = this.calculateWordMatchScore(
      productName,
      searchTermLower
    );
    score += wordScore;

    // Contains negative keywords? -15 points
    const negativeKeywords = [
      "flavored",
      "snack",
      "chips",
      "candy",
      "sweet",
      "artificial",
      "processed",
      "topped",
      "marinated",
      "seasoned",
      "coated",
      "breaded",
      "crumbed",
      "crispy",
      "crunchy",
    ];
    const hasNegativeKeywords = negativeKeywords.some((keyword) =>
      productName.includes(keyword)
    );
    if (hasNegativeKeywords) {
      score -= 15;
    }

    // Bonus for simple, clean names (no extra descriptors)
    if (this.isSimpleCleanName(productName, searchTermLower)) {
      score += 8;
    }

    return score;
  }

  /**
   * Calculate fuzzy match score using Levenshtein distance
   */
  calculateFuzzyMatchScore(productName, searchTerm) {
    const distance = this.levenshteinDistance(productName, searchTerm);
    const maxLength = Math.max(productName.length, searchTerm.length);

    if (maxLength === 0) return 0;

    const similarity = 1 - distance / maxLength;

    // Only give fuzzy score if similarity is above 0.6 (60% similar)
    if (similarity > 0.6) {
      return Math.round(similarity * 12); // Max 12 points for fuzzy match
    }

    return 0;
  }

  /**
   * Calculate Levenshtein distance between two strings
   */
  levenshteinDistance(str1, str2) {
    const matrix = [];

    for (let i = 0; i <= str2.length; i++) {
      matrix[i] = [i];
    }

    for (let j = 0; j <= str1.length; j++) {
      matrix[0][j] = j;
    }

    for (let i = 1; i <= str2.length; i++) {
      for (let j = 1; j <= str1.length; j++) {
        if (str2.charAt(i - 1) === str1.charAt(j - 1)) {
          matrix[i][j] = matrix[i - 1][j - 1];
        } else {
          matrix[i][j] = Math.min(
            matrix[i - 1][j - 1] + 1,
            matrix[i][j - 1] + 1,
            matrix[i - 1][j] + 1
          );
        }
      }
    }

    return matrix[str2.length][str1.length];
  }

  /**
   * Calculate word-based matching score
   */
  calculateWordMatchScore(productName, searchTerm) {
    const productWords = productName.split(/\s+/);
    const searchWords = searchTerm.split(/\s+/);
    let score = 0;

    // Check for exact word matches
    for (const searchWord of searchWords) {
      for (const productWord of productWords) {
        // Exact word match
        if (productWord === searchWord) {
          score += 15;
        }
        // Word starts with search term
        else if (productWord.startsWith(searchWord)) {
          score += 12;
        }
        // Word contains search term
        else if (productWord.includes(searchWord)) {
          score += 8;
        }
        // Fuzzy word match
        else {
          const wordDistance = this.levenshteinDistance(
            productWord,
            searchWord
          );
          const wordSimilarity =
            1 - wordDistance / Math.max(productWord.length, searchWord.length);

          if (wordSimilarity > 0.7) {
            score += Math.round(wordSimilarity * 6); // Max 6 points for fuzzy word match
          }
        }
      }
    }

    // Bonus for word order preservation
    if (this.hasWordOrderPreservation(productWords, searchWords)) {
      score += 5;
    }

    return score;
  }

  /**
   * Check if product name preserves the word order of search terms
   */
  hasWordOrderPreservation(productWords, searchWords) {
    let searchIndex = 0;

    for (const productWord of productWords) {
      if (
        searchIndex < searchWords.length &&
        (productWord === searchWords[searchIndex] ||
          productWord.startsWith(searchWords[searchIndex]))
      ) {
        searchIndex++;
      }
    }

    return searchIndex === searchWords.length;
  }

  /**
   * Check if the product name is simple and clean (no extra descriptors)
   */
  isSimpleCleanName(productName, searchTerm) {
    const productWords = productName.split(/\s+/);
    const searchWords = searchTerm.split(/\s+/);

    // If product name has significantly more words than search term, it's likely complex
    if (productWords.length > searchWords.length + 2) {
      return false;
    }

    // Check if most words in the product name are from the search term
    const matchingWords = productWords.filter((word) =>
      searchWords.some(
        (searchWord) => word === searchWord || word.startsWith(searchWord)
      )
    );

    return matchingWords.length >= searchWords.length;
  }

  /**
   * Score simplicity
   */
  scoreSimplicity(product) {
    let score = 0;

    // Fewer than 5 ingredients? +5 points
    if (product.ingredients_text) {
      const ingredientCount = product.ingredients_text.split(",").length;
      if (ingredientCount < 5) {
        score += 5;
      } else if (ingredientCount < 10) {
        score += 2;
      }
    }

    // Generic/no brand? +3 points
    if (
      !product.brands ||
      product.brands.trim() === "" ||
      product.brands === "Unknown Brand"
    ) {
      score += 3;
    }

    // Bonus for simple product names (fewer words, no complex descriptors)
    const productName = (product.product_name || "").toLowerCase();
    const wordCount = productName.split(/\s+/).length;

    if (wordCount <= 2) {
      score += 5; // Very simple names like "chicken breast"
    } else if (wordCount <= 4) {
      score += 2; // Moderately simple names
    }

    // Penalty for overly complex names with many descriptors
    const complexDescriptors = [
      "topped with",
      "marinated in",
      "seasoned with",
      "coated in",
      "breaded with",
      "stuffed with",
      "filled with",
      "served with",
    ];

    const hasComplexDescriptors = complexDescriptors.some((descriptor) =>
      productName.includes(descriptor)
    );

    if (hasComplexDescriptors) {
      score -= 3;
    }

    return score;
  }

  /**
   * Merge results from different sources and remove duplicates
   */
  mergeResults(openFoodFactsResults, localResults, maxResults) {
    const mergedResults = [...openFoodFactsResults];

    // Add local results that aren't already in the merged results
    for (const localResult of localResults) {
      const isDuplicate = mergedResults.some(
        (result) =>
          result.product_name === localResult.name ||
          result.barcode === localResult.barcode
      );

      if (!isDuplicate) {
        mergedResults.push(localResult);
      }
    }

    // Sort by score and limit results
    return mergedResults
      .sort((a, b) => b.totalScore - a.totalScore)
      .slice(0, maxResults);
  }

  /**
   * Format results for API response with comprehensive serving data
   */
  formatResultsForAPI(scoredResults) {
    return scoredResults.map((result) => {
      const servingData = this.extractServingData(result);
      const nutritionalData = this.extractNutritionalData(result);

      return {
        id: result.code || result.id,
        name: result.product_name || result.name,
        brand: result.brands || result.brand || "Unknown Brand",
        barcode: result.code || result.barcode,
        image: result.image_small_url || result.image_url || null,
        ingredients: result.ingredients_text || result.ingredients || null,
        nutriscore: result.nutriscore_grade || null,
        ecoscore: result.ecoscore_grade || null,
        novaGroup: result.nova_group || null,
        score: {
          total: result.totalScore,
          breakdown: result.scoreBreakdown,
        },
        isLocal: result.isLocal || false,

        // Nutritional data per 100g
        nutrients: nutritionalData.per100g,
        calories: nutritionalData.caloriesPer100g,

        // Comprehensive serving data for frontend
        servingSize: servingData,

        // Nutritional data per serving (when available)
        nutrientsPerServing: nutritionalData.perServing,
        caloriesPerServing: nutritionalData.caloriesPerServing,

        // Additional useful data for frontend
        defaultServing: servingData.defaultServing,
        availableUnits: servingData.availableUnits,
      };
    });
  }

  /**
   * Extract comprehensive serving size data
   */
  extractServingData(result) {
    const nutriments = result.nutriments || {};

    // Parse serving size using the existing helper function from foodController
    const parsedServingSize = this.parseServingSize(result.serving_size);

    // Get serving quantity and unit from API
    const apiServingQuantity = result.serving_quantity || null;
    const apiServingUnit = result.serving_quantity_unit || null;

    // Calculate default serving (use parsed data if available, otherwise API data)
    const defaultQuantity =
      parsedServingSize.quantity || apiServingQuantity || 100;
    const defaultUnit = parsedServingSize.unit || apiServingUnit || "g";

    // Available units for frontend dropdown
    const availableUnits = this.getAvailableUnits(
      defaultUnit,
      parsedServingSize.unit,
      apiServingUnit
    );

    return {
      // Original serving size string
      size: result.serving_size || null,

      // Parsed serving size data
      quantity: parsedServingSize.quantity,
      unit: parsedServingSize.unit,
      original: parsedServingSize.original,

      // API serving data
      apiQuantity: apiServingQuantity,
      apiUnit: apiServingUnit,

      // Default serving for frontend
      defaultServing: {
        quantity: defaultQuantity,
        unit: defaultUnit,
        display: `${defaultQuantity}${defaultUnit}`,
      },

      // Available units for frontend
      availableUnits: availableUnits,

      // Additional serving information
      servingSize: {
        size: result.serving_size || null,
        quantity: parsedServingSize.quantity,
        unit: parsedServingSize.unit,
        original: parsedServingSize.original,
        apiQuantity: apiServingQuantity,
        apiUnit: apiServingUnit,
      },
    };
  }

  /**
   * Parse serving size using regex (similar to foodController helper)
   */
  parseServingSize(servingSizeString) {
    if (!servingSizeString) {
      return { quantity: null, unit: null, original: null };
    }

    // Remove extra whitespace and convert to string
    const cleanSize = String(servingSizeString).trim();

    // Regex to match number (including decimals) followed by optional unit
    const regex =
      /^(\d+(?:\.\d+)?)\s*([a-zA-Z\u00C0-\u017F\u0100-\u017F\u0180-\u024F\u1E00-\u1EFF]*)?$/;
    const match = cleanSize.match(regex);

    if (match) {
      const quantity = parseFloat(match[1]);
      const unit = match[2] ? match[2].trim() : null;

      return {
        quantity: quantity,
        unit: unit,
        original: cleanSize,
      };
    }

    // If regex doesn't match, try to extract just the number
    const numberMatch = cleanSize.match(/(\d+(?:\.\d+)?)/);
    if (numberMatch) {
      return {
        quantity: parseFloat(numberMatch[1]),
        unit: null,
        original: cleanSize,
      };
    }

    return { quantity: null, unit: null, original: cleanSize };
  }

  /**
   * Get available units for frontend dropdown
   */
  getAvailableUnits(defaultUnit, parsedUnit, apiUnit) {
    const units = new Set();

    // Add default unit
    if (defaultUnit) units.add(defaultUnit);

    // Add parsed unit if different
    if (parsedUnit && parsedUnit !== defaultUnit) units.add(parsedUnit);

    // Add API unit if different
    if (apiUnit && apiUnit !== defaultUnit && apiUnit !== parsedUnit)
      units.add(apiUnit);

    // Add common units
    const commonUnits = [
      "g",
      "kg",
      "ml",
      "l",
      "cup",
      "tbsp",
      "tsp",
      "piece",
      "slice",
      "serving",
    ];
    commonUnits.forEach((unit) => units.add(unit));

    return Array.from(units).sort();
  }

  /**
   * Extract comprehensive nutritional data
   */
  extractNutritionalData(result) {
    const nutriments = result.nutriments || {};

    // Nutritional data per 100g
    const per100g = {
      carbs: nutriments.carbohydrates_100g || nutriments.carbohydrates || 0,
      protein: nutriments.proteins_100g || nutriments.proteins || 0,
      fat: nutriments.fat_100g || nutriments.fat || 0,
      fiber: nutriments.fiber_100g || nutriments.fiber || 0,
      sugar: nutriments.sugars_100g || nutriments.sugars || 0,
      sodium: nutriments.sodium_100g || nutriments.sodium || 0,
    };

    const caloriesPer100g =
      nutriments["energy-kcal_100g"] || nutriments["energy-kcal"] || 0;

    // Nutritional data per serving (when available)
    const perServing = {
      carbs: nutriments.carbohydrates_serving || null,
      protein: nutriments.proteins_serving || null,
      fat: nutriments.fat_serving || null,
      fiber: nutriments.fiber_serving || null,
      sugar: nutriments.sugars_serving || null,
      sodium: nutriments.sodium_serving || null,
    };

    const caloriesPerServing = nutriments["energy-kcal_serving"] || null;

    return {
      per100g,
      caloriesPer100g,
      perServing,
      caloriesPerServing,
    };
  }
}

module.exports = FoodSearchService;
