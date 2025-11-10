const axios = require("axios");
const FirebaseService = require("./firebaseService");

class FoodSearchService {
  constructor() {
    this.foodService = new FirebaseService("food");
    // Simple in-memory cache with 5 minute TTL
    this.searchCache = new Map();
    this.CACHE_TTL = 5 * 60 * 1000; // 5 minutes
    this.CACHE_MAX_SIZE = 100; // Maximum cache entries
  }

  /**
   * Get cached search results
   */
  getCachedResults(searchTerm) {
    const cached = this.searchCache.get(searchTerm.toLowerCase());
    if (cached && Date.now() - cached.timestamp < this.CACHE_TTL) {
      console.log(`Cache hit for search term: "${searchTerm}"`);
      return cached.data;
    }
    return null;
  }

  /**
   * Store search results in cache
   */
  setCachedResults(searchTerm, data) {
    // Limit cache size
    if (this.searchCache.size >= this.CACHE_MAX_SIZE) {
      // Remove oldest entry
      const firstKey = this.searchCache.keys().next().value;
      this.searchCache.delete(firstKey);
    }
    
    this.searchCache.set(searchTerm.toLowerCase(), {
      data: data,
      timestamp: Date.now()
    });
  }

  /**
   * Enhanced food search with caching, parallel execution, and optimized scoring
   * @param {string} searchTerm - The food name to search for
   * @param {number} maxResults - Maximum number of results to return (default: 25)
   * @returns {Array} - Sorted array of scored food items
   */
  async searchFoodByName(searchTerm, maxResults = 25) {
    try {
      // Check cache first
      const cachedResults = this.getCachedResults(searchTerm);
      if (cachedResults) {
        return cachedResults.slice(0, maxResults);
      }

      // Execute OpenFoodFacts and local database queries in parallel
      const [openFoodFactsResults, localResults] = await Promise.all([
        this.getOpenFoodFactsResults(searchTerm).catch(err => {
          console.error("OpenFoodFacts error (continuing):", err.message);
          return [];
        }),
        this.getLocalDatabaseResults(searchTerm).catch(err => {
          console.error("Local DB error (continuing):", err.message);
          return [];
        })
      ]);

      console.log(`Found ${openFoodFactsResults.length} results from OpenFoodFacts, ${localResults.length} from local DB`);

      // Score and filter results with optimized algorithm
      const scoredOpenFoodResults = this.scoreAndFilterResults(
        openFoodFactsResults,
        searchTerm,
        false,
        maxResults
      );

      const scoredLocalResults = this.scoreAndFilterResults(
        localResults,
        searchTerm,
        true,
        maxResults
      );

      // Merge and deduplicate results
      const mergedResults = this.mergeResults(
        scoredOpenFoodResults,
        scoredLocalResults,
        maxResults
      );

      // Cache the results
      this.setCachedResults(searchTerm, mergedResults);

      return mergedResults;
    } catch (error) {
      console.error("Error in enhanced food search:", error);
      throw error;
    }
  }

  /**
   * Get results from OpenFoodFacts API with timeout
   */
  async getOpenFoodFactsResults(searchTerm) {
    const endpoint = `https://world.openfoodfacts.org/cgi/search.pl`;
    const commonParams = {
      search_terms: searchTerm,
      json: 1,
      page_size: 25,
      fields:
        "code,product_name,brands,nutriments,serving_size,serving_quantity,serving_quantity_unit,image_small_url,image_url,ingredients_text,nutriscore_grade,ecoscore_grade,nova_group,countries_tags",
    };

    const southAfricaParams = {
      ...commonParams,
      tagtype_0: "countries",
      tag_contains_0: "contains",
      tag_0: "south-africa",
    };

    try {
      const [southAfricaResponse, globalResponse] = await Promise.all([
        axios.get(endpoint, {
          params: southAfricaParams,
          timeout: 5000,
        }),
        axios.get(endpoint, {
          params: commonParams,
          timeout: 5000,
        }),
      ]);

      const southAfricanProducts = this.extractProductsWithRegionFlag(
        southAfricaResponse,
        true
      );
      const globalProducts = this.extractProductsWithRegionFlag(
        globalResponse,
        false
      );

      const seenCodes = new Set(
        southAfricanProducts
          .map((product) => product.code)
          .filter((code) => !!code)
      );

      const combinedResults = [...southAfricanProducts];

      for (const product of globalProducts) {
        if (product.code && seenCodes.has(product.code)) {
          continue;
        }
        combinedResults.push(product);
      }

      return combinedResults;
    } catch (error) {
      if (error.code === "ECONNABORTED") {
        console.error("OpenFoodFacts request timed out");
      }
      throw error;
    }
  }

  extractProductsWithRegionFlag(response, isSouthAfrican) {
    if (!response || !response.data) {
      return [];
    }

    const products = response.data.products || [];
    return products.map((product) => ({
      ...product,
      isSouthAfrican:
        isSouthAfrican ||
        this.hasSouthAfricaCountryTag(product?.countries_tags || []),
    }));
  }

  hasSouthAfricaCountryTag(countriesTags = []) {
    return countriesTags.some((tag) =>
      String(tag).toLowerCase().includes("south-africa")
    );
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
   * Score and filter food results with optimized algorithm
   * Early termination after maxResults to avoid processing unnecessary items
   */
  scoreAndFilterResults(products, searchTerm, isLocal = false, maxResults = 25) {
    const scoredResults = [];
    const searchTermLower = searchTerm.toLowerCase();

    for (const product of products) {
      if (!product || !product.product_name) continue;

      // Quick score calculation (optimized version)
      const score = this.calculateFoodScoreOptimized(product, searchTermLower, isLocal);

      // Only include products with a positive score or from local database
      if (score.totalScore > 0 || isLocal) {
        scoredResults.push({
          ...product,
          scoreBreakdown: score,
          totalScore: score.totalScore,
          isLocal: isLocal,
        });
      }

      // Early termination optimization - if we have enough good results, stop processing
      if (scoredResults.length >= maxResults * 2 && !isLocal) {
        break;
      }
    }

    // Sort by score and return top results
    return scoredResults
      .sort((a, b) => b.totalScore - a.totalScore)
      .slice(0, maxResults);
  }

  /**
   * Optimized food scoring - removed expensive Levenshtein calculations
   * Focus on simpler, faster matching algorithms
   */
  calculateFoodScoreOptimized(product, searchTermLower, isLocal = false) {
    const score = {
      dataCompleteness: 0,
      foodQuality: 0,
      nameRelevance: 0,
      simplicity: 0,
      regionalPreference: 0,
      totalScore: 0,
    };

    // Data completeness (high weight) - simplified checks
    score.dataCompleteness = this.scoreDataCompletenessOptimized(product);

    // Food quality indicators (medium weight)
    score.foodQuality = this.scoreFoodQuality(product);

    // Name relevance (high weight) - optimized matching without Levenshtein
    score.nameRelevance = this.scoreNameRelevanceOptimized(product, searchTermLower);

    // Simplicity (low weight)
    score.simplicity = this.scoreSimplicityOptimized(product);

    // Regional preference (very high weight when available)
    score.regionalPreference = product.isSouthAfrican ? 100 : 0;

    // Calculate total score
    score.totalScore =
      score.dataCompleteness +
      score.foodQuality +
      score.nameRelevance +
      score.simplicity +
      score.regionalPreference;

    return score;
  }

  /**
   * Optimized data completeness scoring
   */
  scoreDataCompletenessOptimized(product) {
    let score = 0;

    // Has nutriscore? +10 points
    if (product.nutriscore_grade && product.nutriscore_grade !== "unknown") {
      score += 10;
    }

    // Has complete nutrition facts? +15 points (increased weight)
    const nutriments = product.nutriments || {};
    const hasCompleteNutrition =
      nutriments.carbohydrates_100g !== undefined &&
      nutriments.proteins_100g !== undefined &&
      nutriments.fat_100g !== undefined &&
      nutriments["energy-kcal_100g"] !== undefined;
    if (hasCompleteNutrition) {
      score += 15;
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
   * Optimized name relevance scoring - removed expensive Levenshtein algorithm
   * Focus on substring matching and word-based matching
   */
  scoreNameRelevanceOptimized(product, searchTermLower) {
    let score = 0;
    const productName = (product.product_name || "").toLowerCase();

    // Exact search term match in name? +30 points
    if (productName === searchTermLower) {
      return 30;
    }
    
    // Search term at start of name? +25 points
    if (productName.startsWith(searchTermLower)) {
      return 25;
    }
    
    // Contains search term? +15 points
    if (productName.includes(searchTermLower)) {
      score += 15;
    }

    // Word-based matching (optimized)
    const wordScore = this.calculateWordMatchScoreOptimized(
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
    ];
    const hasNegativeKeywords = negativeKeywords.some((keyword) =>
      productName.includes(keyword)
    );
    if (hasNegativeKeywords) {
      score -= 15;
    }

    // Bonus for simple, clean names
    if (this.isSimpleCleanName(productName, searchTermLower)) {
      score += 8;
    }

    return score;
  }

  /**
   * Optimized word matching without expensive Levenshtein distance
   */
  calculateWordMatchScoreOptimized(productName, searchTerm) {
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
        else if (productWord.startsWith(searchWord) && searchWord.length > 2) {
          score += 10;
        }
        // Word contains search term (for longer search terms only)
        else if (searchWord.length > 3 && productWord.includes(searchWord)) {
          score += 5;
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
   * Optimized simplicity scoring
   */
  scoreSimplicityOptimized(product) {
    let score = 0;

    // Fewer ingredients? +5 points
    if (product.ingredients_text) {
      const ingredientCount = product.ingredients_text.split(",").length;
      if (ingredientCount < 5) {
        score += 5;
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

    // Simple product names
    const productName = (product.product_name || "").toLowerCase();
    const wordCount = productName.split(/\s+/).length;

    if (wordCount <= 2) {
      score += 5;
    } else if (wordCount <= 4) {
      score += 2;
    }

    return score;
  }

  /**
   * Merge results from different sources and remove duplicates
   */
  mergeResults(openFoodFactsResults, localResults, maxResults) {
    const mergedResults = [...openFoodFactsResults];
    const seenNames = new Set(
      openFoodFactsResults.map(r => (r.product_name || '').toLowerCase())
    );

    // Add local results that aren't already in the merged results
    for (const localResult of localResults) {
      const localName = (localResult.name || localResult.product_name || '').toLowerCase();
      if (!seenNames.has(localName)) {
        mergedResults.push(localResult);
        seenNames.add(localName);
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
        isSouthAfrican: !!result.isSouthAfrican,

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
