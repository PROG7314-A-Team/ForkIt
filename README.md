# ForkIt - Health & Fitness Tracking Application

**Portfolio of Evidence for PROG7314 - Advanced Programming**

A comprehensive Android application for calorie counting, exercise tracking, and dietary habit management designed to empower users in achieving their health and fitness goals.
- **Video demonstration**: https://youtu.be/3Tp-dZ8jtqI?si=I1yy1ue7-Uae11AM

## Project Overview

ForkIt is a modern health tracking application built with Android Jetpack Compose and a robust Node.js backend. The application provides a complete solution for monitoring daily calorie intake, exercise activities, water consumption, and step tracking with an intuitive dashboard, detailed analytics, and customizable goal tracking.

## Key Features

### User Defined Feature 1: Food Logging & Nutrition Tracking

- **Barcode Scanner Integration**: ML Kit-powered barcode scanning with OpenFoodFacts API integration
- **Comprehensive Food Database**: Search from extensive database with detailed nutritional information
- **Custom Food Creation**: Add new food items with complete nutritional details
- **Meal Management**: Create and save custom meals with adjustable serving sizes
- **Smart Calorie Calculator**: Real-time macronutrient-to-calorie conversion

### User Defined Feature 2: Exercise & Activity Tracking

- **Exercise Logging**: Record workouts with duration, calories burned, and exercise type
- **Step Tracking**: Native Android step tracking using Health Connect and sensor fallback
- **Activity Recognition**: Automatic step counting with permission management
- **Goal-Based Tracking**: Set and monitor progress toward daily and weekly targets

### User Defined Feature 3: Water & Hydration Monitoring

- **Water Intake Tracking**: Monitor daily water consumption with progress indicators
- **Hydration Goals**: Set personalized daily water intake targets
- **Visual Progress**: Real-time progress bars and completion tracking

### User Defined Feature 4: Analytics & Insights

- **Visual Data Analysis**: Comprehensive charts using Vico library
- **Meal Distribution**: Pie chart breakdowns across breakfast, lunch, dinner, and snacks
- **Macro Tracking**: Monitor protein, carbohydrates, and fat intake
- **Trend Analysis**: Historical data visualization and progress tracking

### User Defined Feature 5: Habit & Goal Management

- **Custom Habit Creation**: Set personalized health habits
- **Flexible Notifications**: Custom reminders with specific days and times
- **Progress Monitoring**: Track daily, weekly, and monthly completion rates
- **Streak Tracking**: Gamified consistency tracking

### Additional Features: Security & User Experience

- **Biometric Authentication**: Fingerprint and facial recognition support
- **Firebase Authentication**: Secure Google SSO integration
- **Offline Functionality**: Continue logging without internet connection
- **Material Design 3**: Modern, accessible UI following Google's design guidelines

## Technical Architecture

### Frontend (Android)

- **Platform**: Android 13+ (API Level 33+)
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with StateFlow and ViewModel
- **Dependencies**:
  - Retrofit 2.9.0 for API communication
  - ML Kit 17.2.0 for barcode scanning
  - CameraX 1.3.1 for camera functionality
  - Health Connect 1.1.0 for step tracking
  - Vico 2.0.0 for data visualization
  - Biometric 1.2.0 for authentication

### Backend (Node.js)

- **Framework**: Express.js with comprehensive REST API
- **Database**: Firebase Firestore for real-time data synchronization
- **Authentication**: Firebase Admin SDK
- **Testing**: Jest with 90%+ test coverage
- **External APIs**: OpenFoodFacts integration for food data

### Testing & Quality Assurance

- **Backend Testing**: 11 comprehensive test suites covering all controllers
- **Frontend Testing**: Unit tests for business logic and data models
- **Test Coverage**: 90%+ backend coverage with detailed reporting
- **API Documentation**: Complete endpoint documentation with examples

## The App Has been Prepared for Publication in the Play Store

### Generated Signed APK:
**Initiated Process:**
![Image](https://github.com/user-attachments/assets/47123e97-1c92-4f98-96c3-0906bc111103)

**Continued:**
![Image](https://github.com/user-attachments/assets/bc106bef-e78e-438d-ad99-4d496bbe4096)

**Made it release version and created:**
![Image](https://github.com/user-attachments/assets/9e284ca6-1ab8-4fa3-a7c8-8c007070252f)

### Submitted to Google Play Store
**In Review Status**
![Image](https://github.com/user-attachments/assets/3d8cfec0-7cac-49ca-b2c6-5e9bbc408b94)

**Steps succeeded : Published a closeed testing release**
![Image](https://github.com/user-attachments/assets/2c3913a2-4282-4145-a801-173c7032ea38)

### Screenshots Submitted to Google Play Store

**Screenshots Overview:**
![Image](https://github.com/user-attachments/assets/37658768-e60a-4d93-965e-113122fcffec)

### HD Quality Google Play Store Submitted Screenshots:
<br/>
<img width="316" height="2778" alt="Image" src="https://github.com/user-attachments/assets/970d62fe-4d46-4a22-babc-67afb4f1827e" />
<img width="316" height="2778" alt="Image" src="https://github.com/user-attachments/assets/53a550ec-e100-4933-a8b9-a81f4b055798" />
<img width="316" height="2778" alt="Image" src="https://github.com/user-attachments/assets/363a4980-588d-4877-93ca-5c1795f3e276" />
<img width="316" height="2778" alt="Image" src="https://github.com/user-attachments/assets/c8669e3b-ab4c-41f6-a3ac-08dd375c3b1b" />
<img width="316" height="2778" alt="Image" src="https://github.com/user-attachments/assets/41cb3815-0f61-4d02-93da-bd4778141675" />
<img width="316" height="2778" alt="Image" src="https://github.com/user-attachments/assets/4dafff43-d2db-44d9-8e55-8dcd2c0f0280" />

## Development Tools & AI Assistance

This application was developed using **Cursor AI** as an integrated development environment and coding assistant. Cursor's AI-powered features were utilized throughout the development process for:

- **Boiler Plate Code Generation**: Assisting with boilerplate code
- **Debugging Support**: Identifying and resolving code issues and errors
- **Architecture Guidance**: Providing recommendations for best practices and design patterns
- **Documentation**: Helping generate comprehensive code comments and documentation
- **Testing**: Assisting with test case development and coverage optimization


## Project Structure

```
ForkIt/
├── API/forkit-api/                 # Node.js Backend
│   ├── src/
│   │   ├── controllers/           # API route handlers
│   │   ├── services/             # Business logic layer
│   │   ├── routes/               # Express route definitions
│   │   └── tests/                # Comprehensive test suites
│   ├── coverage/                 # Test coverage reports
│   └── API_ENDPOINTS_DOCUMENTATION.md
├── Mobile/app/                    # Android Frontend
│   ├── src/main/java/com/example/forkit/
│   │   ├── activities/           # UI Activities
│   │   ├── business/             # Business logic
│   │   ├── models/               # Data models
│   │   └── utils/                # Utility classes
│   ├── src/test/                 # Unit tests
│   └── build.gradle.kts          # Dependencies & configuration
└── README.md
```

## Testing & Quality Assurance

### Backend Testing (Node.js)

- **Test Framework**: Jest with Supertest for API testing
- **Coverage**: 90%+ code coverage across all modules
- **Test Suites**: 11 comprehensive test files covering:
  - Authentication Controller
  - Food & Meal Log Controllers
  - Exercise & Water Log Controllers
  - User & Goals Controllers
  - Calorie Calculator Service
  - Habit Management
  - Health Check endpoints

### Frontend Testing (Android)

- **Test Framework**: JUnit with Android Test Runner
- **Test Coverage**: Unit tests for business logic and data models
- **Test Categories**:
  - Authentication tests
  - Food logging functionality
  - Exercise tracking
  - Water intake monitoring
  - Data model validation

### API Documentation

- **Complete Endpoint Documentation**: 50+ documented API endpoints
- **Request/Response Examples**: Detailed JSON examples for all endpoints
- **Error Handling**: Comprehensive error response documentation
- **Authentication**: Firebase-based authentication flow

## Getting Started

### Prerequisites

- **Android Development**: Android Studio (latest version)
- **Backend Development**: Node.js 18+ and npm
- **Database**: Firebase project with Firestore enabled
- **External APIs**: OpenFoodFacts API key

### Installation & Setup

#### Backend Setup

```bash
cd API/forkit-api
npm install
npm run dev          # Development server
npm test             # Run test suite
npm run coverage     # Generate coverage report
```

#### Frontend Setup

```bash
cd Mobile
./gradlew build      # Build Android project
./gradlew test       # Run unit tests
```

### Environment Configuration

1. **Firebase Setup**: Configure Firebase project with Firestore
2. **API Keys**: Set up OpenFoodFacts API integration
3. **Permissions**: Configure Android permissions for camera, biometrics, and health data

## Performance Metrics

- **Dashboard Loading**: < 3 seconds on standard devices
- **Barcode Scanning**: < 2 seconds for food identification
- **API Response Time**: < 500ms for standard queries
- **Test Coverage**: 90%+ backend, comprehensive frontend testing
- **Database Performance**: Optimized Firestore queries with indexing

## Security Implementation

- **Authentication**: Firebase Authentication with Google SSO
- **Biometric Security**: Android BiometricPrompt for sensitive operations
- **Data Encryption**: End-to-end encryption for sensitive health data
- **Permission Management**: Granular Android permissions for health data access
- **API Security**: JWT token validation and user authentication

## Development Team

- **Denzel Zimba** (ST10383606) - Backend & Frontend Development
- **Daniel Jung** (ST10324495) - Backend Development & QA Testing
- **Braydon Wooley** (ST10394807) - Frontend Development & QA Testing
- **Nicolas Christofides** (ST10339570) - Frontend Development & QA Testing
- **Max van der Walt** (ST10354483) - Backend Development & QA Testing

## Academic Context

This project serves as a **Portfolio of Evidence for PROG7314 - Advanced Programming**, demonstrating:

- **Advanced Programming Concepts**: Modern Android development with Jetpack Compose
- **API Development**: RESTful API design with comprehensive testing
- **Database Integration**: Firebase Firestore with real-time synchronization
- **Mobile Development**: Native Android features including biometrics and health tracking
- **Testing & Quality Assurance**: Comprehensive test coverage and documentation
- **Software Architecture**: MVVM pattern with clean separation of concerns

## Documentation

- **API Documentation**: Complete endpoint documentation in `API/forkit-api/API_ENDPOINTS_DOCUMENTATION.md`
- **Test Coverage Reports**: Available in `API/forkit-api/coverage/`
- **Code Comments**: Comprehensive inline documentation throughout the codebase

## Key Achievements

- **Modern Android Development**: Jetpack Compose with Material Design 3
- **Comprehensive Backend**: 50+ API endpoints with 90%+ test coverage
- **Health Integration**: Native step tracking with Health Connect
- **Security Implementation**: Biometric authentication and secure data handling
- **Real-time Features**: Live data synchronization with Firebase
- **Professional Documentation**: Complete API and implementation documentation

---

**Note**: This project is developed for educational purposes as part of the PROG7314 Advanced Programming course at Varsity College.
