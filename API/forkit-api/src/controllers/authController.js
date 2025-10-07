const { auth, db } = require("../config/firebase"); // your firebase.js file
const axios = require("axios");
const { Timestamp } = require("firebase-admin/firestore");
const userController = require("../controllers/userController");

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

// Create document from Google Sign Up (SSO):
 const registerGoogleUser = async (req, res) => {
  const { email, ...otherData } = req.body;

  if (!email) {
    return res.status(400).json({ message: "Email is required" });
  }

  try {
    // 1. Check if the user already exists in Firebase Auth
    let userRecord;
    try {
      userRecord = await auth.getUserByEmail(email);
      console.log(`User ${email} already exists in Firebase`);
    } catch (error) {
      if (error.code === "auth/user-not-found") {
        // 2. Create Firebase user (without password)
        userRecord = await auth.createUser({
          email,
          emailVerified: true,
        });
        console.log(`Created new Firebase user for ${email}`);
      } else {
        throw error;
      }
    }

    const uid = userRecord.uid;

    // 3. Create Firestore record if not already created
    const userDocRef = db.collection("users").doc(uid);
    const userDoc = await userDocRef.get();

    if (!userDoc.exists) {
      await userDocRef.set({
        userId: uid,
        email,
        authProvider: "google",
        streakData: {
          currentStreak: 0,
          longestStreak: 0,
          lastLogDate: null,
          streakStartDate: null,
        },
        ...otherData,
        createdAt: new Date(
          new Date().getTime() + 2 * 60 * 60 * 1000
        ).toISOString(),
      });
    }

    return res.status(201).json({
      message: "Google user registered successfully",
      uid,
      email,
    });
  } catch (error) {
    console.error("Error registering Google user:", error);
    return res.status(500).json({
      message: "Error registering Google user",
      error: error.message,
    });
  }
};


// Login User with Google Account:
const loginGoogleUser = async (req, res) => {
  const { email } = req.body;

  if (!email) {
    return res.status(400).json({ message: "Email is required" });
  }

  try {
    const userRecord = await auth.getUserByEmail(email);
    const userDoc = await db.collection("users").doc(userRecord.uid).get();

    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found in Firestore" });
    }

    return res.status(200).json({
      message: "Google login successful",
      user: userDoc.data(),
    });
  } catch (error) {
    console.error("Google login error:", error);
    return res.status(500).json({
      message: "Error logging in Google user",
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
