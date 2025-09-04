const { db } = require("../config/firebase");

class FirebaseService {
  constructor(collectionName) {
    this.collectionName = collectionName;
  }

  // Create a new document
  async create(data) {
    try {
      const docRef = await db.collection(this.collectionName).add({
        ...data,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      });
      return { id: docRef.id, ...data };
    } catch (error) {
      throw new Error(`Error creating document: ${error.message}`);
    }
  }

  // Get document by ID
  async getById(id) {
    try {
      let docSnap = await db.collection(this.collectionName).doc(id).get();

      if (docSnap.exists) {
        return docSnap;
      } else {
        return null;
      }
    } catch (error) {
      throw new Error(`Error getting document: ${error.message}`);
    }
  }

  // Get all documents
  async getAll() {
    try {
      const querySnapshot = await db.collection(this.collectionName).get();
      return querySnapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      }));
    } catch (error) {
      throw new Error(`Error getting documents: ${error.message}`);
    }
  }

  // Update document
  async update(id, data) {
    try {
      await db
        .collection(this.collectionName)
        .doc(id)
        .update({
          ...data,
          updatedAt: new Date().toISOString(),
        });
      return { id, ...data };
    } catch (error) {
      throw new Error(`Error updating document: ${error.message}`);
    }
  }

  // Delete document
  async delete(id) {
    try {
      await db.collection(this.collectionName).doc(id).delete();
      return { id };
    } catch (error) {
      throw new Error(`Error deleting document: ${error.message}`);
    }
  }

  // Query documents
  async query(filters = [], orderByField = null, limitCount = null) {
    try {
      let q = db.collection(this.collectionName);

      // Apply filters
      filters.forEach((filter) => {
        q = q.where(filter.field, filter.operator, filter.value);
      });

      // Apply ordering
      if (orderByField) {
        q = q.orderBy(orderByField);
      }

      // Apply limit
      if (limitCount) {
        q = q.limit(limitCount);
      }

      const querySnapshot = await q.get();
      return querySnapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      }));
    } catch (error) {
      throw new Error(`Error querying documents: ${error.message}`);
    }
  }
}

module.exports = FirebaseService;
