# Testing Food Search Performance Improvements

## Quick Test Guide

### Prerequisites
1. Make sure your API server is running
2. Have your mobile app ready or use a REST client (Postman/Insomnia)

## Test Scenarios

### Test 1: First Search (Cache Miss)
**What to test:** Initial search performance

```bash
# Using curl
curl "http://localhost:3000/api/food/chicken"

# Or in your mobile app
# Search for "chicken" for the first time
```

**Expected Result:**
- Response time: 1-3 seconds (improved from 3-10 seconds)
- Console log: "Found X results from OpenFoodFacts, Y from local DB"
- Should return results with proper nutrition data

### Test 2: Repeated Search (Cache Hit)
**What to test:** Cache performance

```bash
# Immediately search for "chicken" again
curl "http://localhost:3000/api/food/chicken"
```

**Expected Result:**
- Response time: <100ms (almost instant)
- Console log: "Cache hit for search term: "chicken""
- Same results as first search

### Test 3: Timeout Protection
**What to test:** Network resilience

```bash
# If OpenFoodFacts is slow, it should timeout gracefully
# This happens automatically if the API takes >5 seconds
```

**Expected Result:**
- Maximum wait time: 5 seconds
- Should still return local results if API times out
- Console log: "OpenFoodFacts error (continuing): ..."

### Test 4: Various Search Terms
**What to test:** Different query types

```bash
curl "http://localhost:3000/api/food/rice"
curl "http://localhost:3000/api/food/apple"
curl "http://localhost:3000/api/food/milk"
curl "http://localhost:3000/api/food/bread"
```

**Expected Result:**
- First search of each: 1-3 seconds
- Repeated searches: <100ms
- Relevant results with scoring

## Mobile App Testing

### Manual Testing Steps
1. **Open your app** and navigate to the food search
2. **Type "chicken"** and wait for results
   - ⏱️ Note the time it takes
3. **Delete and type "chicken" again**
   - ⏱️ Should be instant this time
4. **Try different searches:**
   - "rice"
   - "apple"
   - "yogurt"
5. **Test network issues:**
   - Turn off WiFi briefly and try searching
   - Should get graceful error message

## What to Look For

### ✅ Success Indicators
- Searches feel much faster
- Repeated searches are instant
- No hanging/frozen UI
- Relevant results appear at the top
- Loading indicators work smoothly

### ❌ Issues to Watch For
- Cache not working (all searches same speed)
- Timeout errors appearing too often
- Results missing nutrition data
- Scoring seems off (irrelevant results first)

## Server Logs to Monitor

When the API is running, watch for these log messages:

```
✅ Good logs:
"Cache hit for search term: "chicken""
"Found 25 results from OpenFoodFacts, 0 from local DB"
"Found 10 scored food results for "chicken""

⚠️ Warning logs:
"OpenFoodFacts error (continuing): ECONNABORTED"
"OpenFoodFacts request timed out"

❌ Error logs:
"Error in enhanced food search: ..."
```

## Performance Benchmarks

### Before Optimization
- First search: 3-10 seconds
- Repeated search: 3-10 seconds
- Timeout: Never (could hang indefinitely)
- Cache: None

### After Optimization
- First search: 1-3 seconds ✅
- Repeated search: <100ms ✅
- Timeout: 5 seconds max ✅
- Cache: 5 minute TTL ✅

## Troubleshooting

### "Search is still slow"
1. Check if cache is working (look for cache hit logs)
2. Verify OpenFoodFacts API is accessible
3. Check network connection speed
4. Consider increasing timeout in `foodSearchService.js`

### "Cache not working"
1. Verify server hasn't been restarted (cache clears on restart)
2. Check logs for "Cache hit" messages
3. Search for exact same term (case-insensitive)

### "Getting timeout errors"
1. Your network might be slow
2. OpenFoodFacts might be down
3. Consider increasing timeout from 5s to 10s:
   ```javascript
   timeout: 10000 // 10 seconds
   ```

### "Results seem less relevant"
1. The scoring algorithm was simplified for speed
2. Most relevant results should still appear first
3. If specific queries are problematic, let me know

## Advanced Testing

### Load Testing (Optional)
Test with multiple concurrent requests:

```bash
# Install artillery if needed: npm install -g artillery

# Create artillery.yml:
cat > artillery.yml << EOF
config:
  target: 'http://localhost:3000'
  phases:
    - duration: 60
      arrivalRate: 10
scenarios:
  - flow:
    - get:
        url: "/api/food/chicken"
EOF

# Run test
artillery run artillery.yml
```

### Cache Performance Test
```javascript
// Test script (Node.js)
const axios = require('axios');

async function testCache() {
  const searches = ['chicken', 'rice', 'apple', 'milk', 'bread'];
  
  for (const term of searches) {
    // First search (cache miss)
    const start1 = Date.now();
    await axios.get(`http://localhost:3000/api/food/${term}`);
    const time1 = Date.now() - start1;
    
    // Second search (cache hit)
    const start2 = Date.now();
    await axios.get(`http://localhost:3000/api/food/${term}`);
    const time2 = Date.now() - start2;
    
    console.log(`${term}: ${time1}ms -> ${time2}ms (${Math.round((1 - time2/time1) * 100)}% faster)`);
  }
}

testCache();
```

## Next Steps

After testing:
1. ✅ Verify searches are faster
2. ✅ Confirm cache is working
3. ✅ Check results are relevant
4. 📝 Report any issues
5. 🚀 Deploy to production

## Configuration Tuning

If needed, adjust these values in `foodSearchService.js`:

```javascript
// Line 8-10
this.CACHE_TTL = 5 * 60 * 1000;  // Cache duration
this.CACHE_MAX_SIZE = 100;        // Max cached queries

// Line 125
timeout: 5000                     // API timeout

// Line 121
page_size: 25                     // Results per request
```

## Questions or Issues?

If you encounter any problems:
1. Check the server logs
2. Try restarting the API server
3. Clear the cache (restart server)
4. Review the optimization document

---

**Remember:** The biggest improvements will be noticed on:
- Repeated searches (should be instant)
- Common food items (chicken, rice, etc.)
- Poor network conditions (timeout protection)

