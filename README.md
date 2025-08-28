# ForkIt

A comprehensive Android application for calorie counting, exercise tracking, and dietary habit management designed to empower users in achieving their health and fitness goals.

## Overview

ForkIt is a user-friendly health tracking application that provides a complete solution for monitoring daily calorie intake, exercise activities, and water consumption. The application features an intuitive dashboard, detailed analytics, and customizable goal tracking to support users in maintaining a healthy lifestyle.

## Key Features

### Food Logging

- **Barcode Scanner Integration**: Instantly retrieve nutritional information by scanning food package barcodes using OpenFoodFacts API
- **Comprehensive Food Database**: Search from an extensive database of food items with detailed nutritional information
- **Custom Food Creation**: Add new food items when not found in the database, complete with nutritional details
- **Meal Management**: Create and save custom meals with adjustable serving sizes and ingredient modifications
- **Smart Suggestions**: Access recently logged foods with meal-specific recommendations

### Exercise and Water Tracking

- **Exercise Logging**: Record workout details including name, duration, calories burned, and exercise type
- **Water Intake Monitoring**: Track daily water consumption with progress indicators against personal goals
- **Goal-Based Tracking**: Set and monitor progress toward daily and weekly targets

### Analytics and Insights

- **Visual Data Analysis**: Comprehensive graphical representation of eating habits and nutritional intake
- **Meal Distribution Charts**: Pie chart breakdowns showing calorie distribution across breakfast, lunch, dinner, and snacks
- **Macro Tracking**: Monitor protein, carbohydrates, and fat intake with detailed metrics
- **Streak Tracking**: Gamified consistency tracking to encourage regular logging habits

### Habit and Goal Management

- **Custom Habit Creation**: Set personalized habits for various health goals
- **Flexible Notifications**: Create custom reminders with specific days and times
- **Progress Monitoring**: Track daily, weekly, and monthly habit completion

### User Experience

- **Single Sign-On (SSO)**: Streamlined authentication using Google accounts
- **Biometric Security**: Fingerprint and facial recognition for secure access to sensitive information
- **Offline Functionality**: Continue logging activities without internet connection with automatic synchronization
- **Multi-language Support**: Available in English, isiZulu, and Afrikaans

## Technical Architecture

### Frontend

- **Platform**: Android (API Level 29+)
- **Design**: Material Design guidelines for consistent user experience
- **Authentication**: Firebase Authentication with biometric support

### Backend

- **API**: Node.js with Express.js framework
- **Database**: Firebase Firestore for real-time data synchronization
- **Hosting**: Azure App Service for reliable backend infrastructure
- **External Integration**: OpenFoodFacts API for barcode scanning and food database access

### Key Requirements

- Android 10 (API Level 29) or higher
- Internet connection for initial setup and data synchronization
- Camera permissions for barcode scanning functionality
- Biometric hardware for enhanced security features (optional)

## Development Team

- **Denzel Zimba** (ST10383606) - Backend & Frontend Development
- **Daniel Jung** (ST10324495) - Backend Development & QA Testing
- **Braydon Wooley** (ST10394807) - Frontend Development & QA Testing
- **Nicolas Christofides** (ST10339570) - Frontend Development & QA Testing
- **Max van der Walt** (ST10354483) - Backend Development & QA Testing

## Project Structure

The application follows a modular architecture with clear separation between frontend and backend components:

- User authentication and profile management
- Food logging and database integration
- Exercise and water tracking systems
- Analytics and data visualization
- Habit tracking and notification systems
- Offline data synchronization

## Getting Started

### Prerequisites

- Android Studio for development
- Node.js for backend development
- Firebase project setup
- Azure account for hosting

### Installation

1. Clone the repository
2. Set up Firebase project and configuration
3. Configure Azure App Service for backend hosting
4. Install dependencies for both frontend and backend components
5. Configure OpenFoodFacts API integration

## Performance and Reliability

- Dashboard loading time under 5 seconds on standard devices
- Database searches and barcode scanning results in under 5 seconds
- 99.5% uptime for backend services
- Scalable architecture supporting 100,000+ concurrent users
- Comprehensive error handling and data loss prevention

## Security and Privacy

- Secure password storage using bcrypt with unique salts
- OAuth 2.0 compliance through Firebase Authentication
- Biometric authentication for sensitive data access
- Encrypted data transmission and storage

## Contributing

This project is developed as part of the PROG7314 course. Please refer to the project documentation for detailed implementation guidelines and coding standards.

## License

This project is developed for educational purposes as part of university coursework.

## Documentation

For detailed technical specifications, API documentation, and implementation guidelines, please refer to the accompanying project documentation.
