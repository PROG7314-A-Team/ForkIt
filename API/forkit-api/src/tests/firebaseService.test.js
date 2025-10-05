// Mock Firebase Admin SDK first
jest.mock("firebase-admin", () => ({
  initializeApp: jest.fn(),
  credential: {
    cert: jest.fn(),
  },
  auth: jest.fn(() => ({
    verifyIdToken: jest.fn(),
  })),
  firestore: jest.fn(() => ({
    collection: jest.fn(() => ({
      doc: jest.fn(() => ({
        get: jest.fn(),
        set: jest.fn(),
        update: jest.fn(),
        delete: jest.fn(),
        id: "mock-doc-id",
      })),
      add: jest.fn(),
      where: jest.fn(() => ({
        get: jest.fn(),
      })),
      get: jest.fn(),
    })),
  })),
  storage: jest.fn(() => ({
    bucket: jest.fn(),
  })),
}));

const FirebaseService = require("../services/firebaseService");

describe("FirebaseService", () => {
  let firebaseService;
  let mockCollection;
  let mockDoc;

  beforeEach(() => {
    // Reset mocks
    jest.clearAllMocks();

    // Create service instance
    firebaseService = new FirebaseService("test-collection");

    // Setup mock chain
    const mockFirestore = require("firebase-admin").firestore();
    mockCollection = mockFirestore.collection();
    mockDoc = mockCollection.doc();
  });

  describe("constructor", () => {
    test("should initialize with collection name", () => {
      const service = new FirebaseService("users");
      expect(service).toBeDefined();
    });
  });

  describe("create", () => {
    test("should create a new document", async () => {
      const mockData = { name: "Test User", email: "test@example.com" };
      const mockResult = { id: "doc123", ...mockData };

      mockCollection.add.mockResolvedValue({ id: "doc123" });
      mockDoc.get.mockResolvedValue({
        exists: true,
        data: () => mockData,
        id: "doc123",
      });

      const result = await firebaseService.create(mockData);

      expect(mockCollection.add).toHaveBeenCalledWith(mockData);
      expect(result).toEqual(mockResult);
    });

    test("should handle creation errors", async () => {
      const mockData = { name: "Test User" };
      const error = new Error("Firebase error");

      mockCollection.add.mockRejectedValue(error);

      await expect(firebaseService.create(mockData)).rejects.toThrow(
        "Firebase error"
      );
    });
  });

  describe("getById", () => {
    test("should retrieve document by ID", async () => {
      const mockData = { name: "Test User", email: "test@example.com" };
      const docId = "doc123";

      mockDoc.get.mockResolvedValue({
        exists: true,
        data: () => mockData,
        id: docId,
      });

      const result = await firebaseService.getById(docId);

      expect(mockCollection.doc).toHaveBeenCalledWith(docId);
      expect(mockDoc.get).toHaveBeenCalled();
      expect(result).toEqual({ id: docId, ...mockData });
    });

    test("should return null for non-existent document", async () => {
      const docId = "non-existent";

      mockDoc.get.mockResolvedValue({ exists: false });

      const result = await firebaseService.getById(docId);

      expect(result).toBeNull();
    });

    test("should handle get errors", async () => {
      const docId = "doc123";
      const error = new Error("Firebase get error");

      mockDoc.get.mockRejectedValue(error);

      await expect(firebaseService.getById(docId)).rejects.toThrow(
        "Firebase get error"
      );
    });
  });

  describe("update", () => {
    test("should update existing document", async () => {
      const docId = "doc123";
      const updateData = { name: "Updated User" };
      const mockData = { name: "Updated User", email: "test@example.com" };

      mockDoc.update.mockResolvedValue();
      mockDoc.get.mockResolvedValue({
        exists: true,
        data: () => mockData,
        id: docId,
      });

      const result = await firebaseService.update(docId, updateData);

      expect(mockCollection.doc).toHaveBeenCalledWith(docId);
      expect(mockDoc.update).toHaveBeenCalledWith(updateData);
      expect(result).toEqual({ id: docId, ...mockData });
    });

    test("should handle update errors", async () => {
      const docId = "doc123";
      const updateData = { name: "Updated User" };
      const error = new Error("Firebase update error");

      mockDoc.update.mockRejectedValue(error);

      await expect(firebaseService.update(docId, updateData)).rejects.toThrow(
        "Firebase update error"
      );
    });
  });

  describe("delete", () => {
    test("should delete document", async () => {
      const docId = "doc123";

      mockDoc.delete.mockResolvedValue();

      const result = await firebaseService.delete(docId);

      expect(mockCollection.doc).toHaveBeenCalledWith(docId);
      expect(mockDoc.delete).toHaveBeenCalled();
      expect(result).toBe(true);
    });

    test("should handle delete errors", async () => {
      const docId = "doc123";
      const error = new Error("Firebase delete error");

      mockDoc.delete.mockRejectedValue(error);

      await expect(firebaseService.delete(docId)).rejects.toThrow(
        "Firebase delete error"
      );
    });
  });

  describe("getAll", () => {
    test("should retrieve all documents", async () => {
      const mockDocs = [
        { id: "doc1", data: () => ({ name: "User 1" }) },
        { id: "doc2", data: () => ({ name: "User 2" }) },
      ];

      mockCollection.get.mockResolvedValue({ docs: mockDocs });

      const result = await firebaseService.getAll();

      expect(mockCollection.get).toHaveBeenCalled();
      expect(result).toEqual([
        { id: "doc1", name: "User 1" },
        { id: "doc2", name: "User 2" },
      ]);
    });

    test("should return empty array when no documents", async () => {
      mockCollection.get.mockResolvedValue({ docs: [] });

      const result = await firebaseService.getAll();

      expect(result).toEqual([]);
    });

    test("should handle getAll errors", async () => {
      const error = new Error("Firebase getAll error");

      mockCollection.get.mockRejectedValue(error);

      await expect(firebaseService.getAll()).rejects.toThrow(
        "Firebase getAll error"
      );
    });
  });

  describe("query", () => {
    test("should query documents with where clause", async () => {
      const mockDocs = [
        { id: "doc1", data: () => ({ name: "User 1", age: 25 }) },
      ];
      const queryConditions = [{ field: "age", operator: ">=", value: 18 }];

      const mockWhere = mockCollection.where();
      mockWhere.get.mockResolvedValue({ docs: mockDocs });

      const result = await firebaseService.query(queryConditions);

      expect(mockCollection.where).toHaveBeenCalledWith("age", ">=", 18);
      expect(result).toEqual([{ id: "doc1", name: "User 1", age: 25 }]);
    });

    test("should handle multiple query conditions", async () => {
      const mockDocs = [
        {
          id: "doc1",
          data: () => ({ name: "User 1", age: 25, city: "New York" }),
        },
      ];
      const queryConditions = [
        { field: "age", operator: ">=", value: 18 },
        { field: "city", operator: "==", value: "New York" },
      ];

      const mockWhere = mockCollection.where();
      mockWhere.get.mockResolvedValue({ docs: mockDocs });

      const result = await firebaseService.query(queryConditions);

      expect(result).toEqual([
        { id: "doc1", name: "User 1", age: 25, city: "New York" },
      ]);
    });

    test("should handle query errors", async () => {
      const queryConditions = [{ field: "age", operator: ">=", value: 18 }];
      const error = new Error("Firebase query error");

      const mockWhere = mockCollection.where();
      mockWhere.get.mockRejectedValue(error);

      await expect(firebaseService.query(queryConditions)).rejects.toThrow(
        "Firebase query error"
      );
    });
  });

  describe("getByUserId", () => {
    test("should query documents by userId field", async () => {
      const userId = "user123";
      const mockDocs = [
        { id: "doc1", data: () => ({ userId: "user123", name: "User 1" }) },
      ];

      const mockWhere = mockCollection.where();
      mockWhere.get.mockResolvedValue({ docs: mockDocs });

      const result = await firebaseService.getByUserId(userId);

      expect(mockCollection.where).toHaveBeenCalledWith("userId", "==", userId);
      expect(result).toEqual([
        { id: "doc1", userId: "user123", name: "User 1" },
      ]);
    });

    test("should return empty array when no matching documents", async () => {
      const userId = "non-existent";

      const mockWhere = mockCollection.where();
      mockWhere.get.mockResolvedValue({ docs: [] });

      const result = await firebaseService.getByUserId(userId);

      expect(result).toEqual([]);
    });
  });
});
