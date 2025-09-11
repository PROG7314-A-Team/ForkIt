const admin = require('../firebase'); // import firebase admin

// Register a new user
const registerUser = async (req, res) => {
    const { email, password } = req.body;

    if (!email || !password) {
        return res.status(400).json({ message: 'Email and password are required' });
    }

    try {
        const userRecord = await admin.auth().createUser({
            email,
            password,
        });

        return res.status(201).json({
            message: 'User created successfully',
            uid: userRecord.uid,
            email: userRecord.email
        });
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: 'Error creating user', error: error.message });
    }
};

module.exports = { registerUser };
