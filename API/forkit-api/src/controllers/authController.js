const { auth, db } = require("../config/firebase"); // your firebase.js file
const axios = require("axios");
const { Timestamp } = require("firebase-admin/firestore");
const userController = require("../controllers/userController");
const admin = require("firebase-admin");

// create a new user - used by api/users/register endpoint
const createUser = async (req, res) => {
  const { email, password, ...otherData } = req.body;

  if (!email || !password) {
    return res.status(400).json({ message: "Email and password are required" });
  }

  try {
    // 1. Create the user in Firebase Authentication
    const userRecord = await auth.createUser({
      email,
      password,
    });

    const uid = userRecord.uid;

    // 2. Save a document in Firestore with UID as doc id
    await db
      .collection("users")
      .doc(uid)
      .set({
        userId: uid,
        email: email,
        streakData: {
          currentStreak: 0,
          longestStreak: 0,
          lastLogDate: null,
          streakStartDate: null,
        },
        goals: {
          dailyCalories: 2000, // Default calorie goal in kcal
          dailyWater: 2000, // Default water goal in ml
          dailySteps: 8000, // Default steps goal
          weeklyExercises: 3, // Default weekly exercise sessions
        },
        ...otherData,
        createdAt: new Date(
          new Date().getTime() + 2 * 60 * 60 * 1000
        ).toISOString(), // e.g. "2025-09-19T08:48:34.806Z"
        goalsUpdatedAt: new Date(
          new Date().getTime() + 2 * 60 * 60 * 1000
        ).toISOString(),
      });

    //3. Return response
    return res.status(201).json({
      message: "User created successfully",
      uid,
      email,
    });
  } catch (error) {
    console.error("Error creating user:", error);
    return res.status(500).json({
      message: "Error creating user",
      error: error.message,
    });
  }
};

// login a user - used by api/users/login endpoint
const loginUser = async (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).json({ message: "Email and password are required" });
  }

  try {
    const apiKey = process.env.FIREBASE_API_KEY; // Add your Firebase Web API Key to .env
    const response = await axios.post(
      `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${apiKey}`,
      {
        email,
        password,
        returnSecureToken: true,
      }
    );

    const { idToken, refreshToken, expiresIn, localId } = response.data;

    res.status(200).json({
      message: "Login successful",
      userId: localId,
      idToken,
      refreshToken,
      expiresIn,
    });
  } catch (error) {
    console.error("Login error:", error.response?.data || error.message);
    res.status(401).json({
      message: error.response?.data?.error?.message || error.message,
      error: error.response?.data?.error?.message || error.message,
    });
  }
};


const buildDefaultUserProfile = (uid, email, otherData = {}) => {
  const now = new Date(new Date().getTime() + 2 * 60 * 60 * 1000).toISOString();
  return {
    userId: uid,
    email,
    authProvider: "google",
    streakData: {
      currentStreak: 0,
      longestStreak: 0,
      lastLogDate: null,
      streakStartDate: null,
    },
    goals: {
      dailyCalories: 2000,
      dailyWater: 2000,
      dailySteps: 8000,
      weeklyExercises: 3,
    },
    ...otherData,
    createdAt: now,
    goalsUpdatedAt: now,
  };
};

// Create document from Google Sign Up (SSO):
const registerGoogleUser = async (req, res) => {
  const { email, idToken, ...otherData } = req.body;

  if (!email || !idToken) {
    return res
      .status(400)
      .json({ success: false, message: "Email and idToken are required" });
  }

  try {
    // 1. Verify the provided Firebase ID token
    const decodedToken = await admin.auth().verifyIdToken(idToken);

    if (decodedToken.email !== email) {
      return res
        .status(401)
        .json({
          success: false,
          message: "Token email mismatch. Please sign in again.",
        });
    }

    // 2. Check if the user already exists in Firebase Auth
    let userRecord;
    try {
      userRecord = await auth.getUserByEmail(email);
    } catch (error) {
      if (error.code === "auth/user-not-found") {
        userRecord = await auth.createUser({
          email,
          emailVerified: true,
        });
      } else {
        throw error;
      }
    }

    const uid = userRecord.uid || decodedToken.uid;

    // 3. Create Firestore record if not already created
    const userDocRef = db.collection("users").doc(uid);
    const userDoc = await userDocRef.get();

    if (!userDoc.exists) {
      await userDocRef.set(buildDefaultUserProfile(uid, email, otherData));
    }

    return res.status(201).json({
      success: true,
      message: "Google user registered successfully",
      uid,
      email,
    });
  } catch (error) {
    console.error("Error registering Google user:", error);
    const statusCode =
      error.code === "auth/argument-error" || error.code === "auth/id-token-expired"
        ? 401
        : 500;
    return res.status(statusCode).json({
      success: false,
      message:
        statusCode === 401 ? "Invalid or expired Google token" : "Error registering Google user",
      error: error.message,
    });
  }
};

// Create document from Google Sign Up (SSO):
const loginGoogleUser = async (req, res) => {
  const { email, idToken } = req.body;

  if (!email || !idToken) {
    return res
      .status(400)
      .json({ success: false, message: "Email and idToken are required" });
  }

  try {
    // 1. Verify the token using Firebase Admin SDK
    const decodedToken = await admin.auth().verifyIdToken(idToken);

    if (decodedToken.email !== email) {
      return res
        .status(401)
        .json({
          success: false,
          message: "Token email mismatch. Please sign in again.",
        });
    }

    const uid = decodedToken.uid;

    // 2. Ensure user exists in Firestore. Create a default profile if missing.
    const userDocRef = db.collection("users").doc(uid);
    const userDoc = await userDocRef.get();

    if (!userDoc.exists) {
      await userDocRef.set(buildDefaultUserProfile(uid, email));
    }

    // 3. Generate a new token for session management (optional)
    const customToken = await admin.auth().createCustomToken(uid);

    // 4. Respond with user details and token
    return res.status(200).json({
      success: true,
      message: "Google login successful",
      userId: uid,
      idToken,
      customToken,
    });
  } catch (error) {
    console.error("Error verifying Google ID token:", error);
    return res.status(401).json({
      success: false,
      message: "Invalid or expired token",
      error: error.message,
    });
  }
};

module.exports = {
  createUser,
  loginUser,
  registerGoogleUser,
  loginGoogleUser,
};
