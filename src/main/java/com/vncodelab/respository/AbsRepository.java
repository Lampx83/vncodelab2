package com.vncodelab.respository;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.pojo.PojoCodecProvider;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public abstract class AbsRepository {
    static MongoDatabase db;

    MongoDatabase getDB() {
        if (db == null) {
            String host = "localhost";
            ConnectionString connectionString;
            if (host.equals("localhost"))
                connectionString = new ConnectionString("mongodb://localhost:27017");
            else
                connectionString = new ConnectionString("mongodb+srv://nckh:nckh@buithithom.j0du0.mongodb.net"); //NCKHSV

            //  ConnectionString connectionString = new ConnectionString("mongodb+srv://root:root@cluster0.lh5rj.mongodb.net"); //Lampx
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .codecRegistry(fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build())))
                    .build();
            MongoClient mongoClient = MongoClients.create(settings);
            db = mongoClient.getDatabase("NCKH");
            System.out.println("Connect to DB");
        }
        return db;
    }

//    public static void main(String[] args) {
//        // Replace the uri string with your MongoDB deployment's connection string
//        String uri = "mongodb://localhost:27017";
//        try (MongoClient mongoClient = MongoClients.create(uri)) {
//            MongoDatabase database = mongoClient.getDatabase("ischolar");
//            try {
//                MongoCollection<Document> movies = database.getCollection("phrase");
//
//                System.out.println(movies.countDocuments());
//            } catch (MongoException me) {
//                System.err.println("An error occurred while attempting to run a command: " + me);
//            }
//        }
//    }


}
