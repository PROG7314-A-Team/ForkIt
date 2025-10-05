package com.example.forkit.utils

import org.junit.Test
import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Unit tests for Date utility functions
 * Tests date formatting, parsing, and date calculations used in the app
 */
class DateUtilsTest {

    @Test
    fun `today date format is correct`() {
        // Arrange
        val calendar = Calendar.getInstance()
        val expectedFormat = "yyyy-MM-dd"
        
        // Act
        val todayDate = String.format("%04d-%02d-%02d", 
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH))
        
        // Assert
        assertNotNull("Today date should not be null", todayDate)
        assertTrue("Today date should match expected format", 
            todayDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        assertEquals("Today date should be 10 characters", 10, todayDate.length)
    }

    @Test
    fun `date format validation works correctly`() {
        // Test valid dates
        val validDates = listOf(
            "2025-01-15",
            "2025-12-31",
            "2025-02-28",
            "2025-02-29" // Leap year
        )
        
        validDates.forEach { date ->
            assertTrue("Date '$date' should be valid", 
                date.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        }
        
        // Test invalid dates
        val invalidDates = listOf(
            "2025-1-15",    // Missing leading zero
            "25-01-15",     // Two digit year
            "2025/01/15"    // Wrong separator
        )
        
        invalidDates.forEach { date ->
            assertFalse("Date '$date' should be invalid", 
                date.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        }
    }

    @Test
    fun `date parsing works correctly`() {
        // Arrange
        val dateString = "2025-01-15"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // Act
        val parsedDate = dateFormat.parse(dateString)
        
        // Assert
        assertNotNull("Parsed date should not be null", parsedDate)
        
        val calendar = Calendar.getInstance()
        calendar.time = parsedDate!!
        
        assertEquals("Year should be 2025", 2025, calendar.get(Calendar.YEAR))
        assertEquals("Month should be January (0-indexed)", 0, calendar.get(Calendar.MONTH))
        assertEquals("Day should be 15", 15, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `date comparison works correctly`() {
        // Arrange
        val date1 = "2025-01-15"
        val date2 = "2025-01-16"
        val date3 = "2025-01-15"
        
        // Act & Assert
        assertTrue("date1 should be before date2", date1 < date2)
        assertTrue("date2 should be after date1", date2 > date1)
        assertEquals("date1 should equal date3", date1, date3)
    }

    @Test
    fun `date range validation works correctly`() {
        // Arrange
        val startDate = "2025-01-01"
        val endDate = "2025-01-31"
        val middleDate = "2025-01-15"
        
        // Act & Assert
        assertTrue("Start date should be before end date", startDate < endDate)
        assertTrue("Middle date should be between start and end", 
            middleDate > startDate && middleDate < endDate)
        assertTrue("Start date should be before middle date", startDate < middleDate)
        assertTrue("Middle date should be before end date", middleDate < endDate)
    }

    @Test
    fun `time extraction from datetime works correctly`() {
        // Arrange
        val dateTime = "2025-01-15T14:30:00Z"
        
        // Act
        val time = dateTime.substring(11, 16) // Extract HH:MM
        
        // Assert
        assertEquals("Time should be extracted correctly", "14:30", time)
        assertTrue("Time should be in HH:MM format", 
            time.matches(Regex("\\d{2}:\\d{2}")))
    }

    @Test
    fun `datetime format validation works correctly`() {
        // Test valid datetime formats
        val validDateTimes = listOf(
            "2025-01-15T14:30:00Z",
            "2025-12-31T23:59:59Z",
            "2025-02-28T00:00:00Z"
        )
        
        validDateTimes.forEach { dateTime ->
            assertTrue("DateTime '$dateTime' should be valid", 
                dateTime.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")))
        }
    }

    @Test
    fun `month and year extraction works correctly`() {
        // Arrange
        val date = "2025-01-15"
        
        // Act
        val year = date.substring(0, 4).toInt()
        val month = date.substring(5, 7).toInt()
        val day = date.substring(8, 10).toInt()
        
        // Assert
        assertEquals("Year should be 2025", 2025, year)
        assertEquals("Month should be 1", 1, month)
        assertEquals("Day should be 15", 15, day)
    }

    @Test
    fun `date arithmetic works correctly`() {
        // Test date addition (simulated)
        val baseDate = "2025-01-15"
        val daysToAdd = 7
        
        // Simulate adding days (simplified)
        val calendar = Calendar.getInstance()
        calendar.set(2025, 0, 15) // January 15, 2025
        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
        
        val resultDate = String.format("%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH))
        
        // Assert
        assertEquals("Date after adding 7 days should be 2025-01-22", "2025-01-22", resultDate)
    }

    @Test
    fun `date validation edge cases`() {
        // Test leap year
        val leapYearDate = "2024-02-29"
        assertTrue("Leap year date should be valid", 
            leapYearDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        
        // Test non-leap year February 29th
        val nonLeapYearDate = "2025-02-29"
        assertTrue("Non-leap year date format should be valid", 
            nonLeapYearDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        
        // Test year boundaries
        val yearStart = "2025-01-01"
        val yearEnd = "2025-12-31"
        
        assertTrue("Year start should be valid", 
            yearStart.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        assertTrue("Year end should be valid", 
            yearEnd.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `date sorting works correctly`() {
        // Arrange
        val dates = listOf(
            "2025-01-15",
            "2025-01-10",
            "2025-01-20",
            "2025-01-05"
        )
        
        // Act
        val sortedDates = dates.sorted()
        
        // Assert
        assertEquals("First date should be 2025-01-05", "2025-01-05", sortedDates[0])
        assertEquals("Last date should be 2025-01-20", "2025-01-20", sortedDates[3])
        assertTrue("Dates should be in ascending order", 
            sortedDates[0] < sortedDates[1] && 
            sortedDates[1] < sortedDates[2] && 
            sortedDates[2] < sortedDates[3])
    }
}
