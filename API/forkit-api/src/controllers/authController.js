const { auth } = require("../config/firebase"); // your firebase.js file
const axios = require("axios");


// create a new user - used by api/users/register endpoint
const createUser = async (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).json({ message: "Email and password are required" });
  }

  try {
    const userRecord = await auth.createUser({
      email,
      password,
    });

    return res.status(201).json({
      message: "User created successfully",
      uid: userRecord.uid,
      email: userRecord.email,
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
      message: "Invalid email or password",
      error: error.response?.data?.error?.message || error.message,
    });
  }
};

module.exports = {
  createUser, 
  loginUser
};
