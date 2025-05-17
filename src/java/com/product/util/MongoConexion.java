package com.product.util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoException;

public class MongoConexion {

    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "VentasMongo";

    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;

    // Obtiene la instancia de MongoDatabase
    public static MongoDatabase getDatabase() throws MongoException {
        if (mongoClient == null) {
            try {
                mongoClient = MongoClients.create(CONNECTION_STRING);
                database = mongoClient.getDatabase(DATABASE_NAME);
            } catch (MongoException e) {
                throw e; 
            }
        }
        return database;
    }

    // Cierra la conexión MongoClient
    public static void closeConnection() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
            } catch (MongoException e) {
                e.printStackTrace();
            } finally {
                mongoClient = null;
                database = null;
            }
        }
    }
}
