package com.vncodelab.respository;

import com.mongodb.client.AggregateIterable;
import com.vncodelab.entity.ischolar.Item;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;

@Repository
public class PhraseRepository extends AbsRepository {

    public Object getAllPhrases() {
        try {
            ArrayList<Document> movies = getDB().getCollection("phrase").find().into(new ArrayList<>());
            return movies;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    public Row getSectionByID(String sectionsName) {
//        MongoCollection<Row> rows = getDB().getCollection("phrase", Row.class);
//        Document filter = new Document();
//        filter.append("section", sectionsName);
//        FindIterable<Row> result = rows.find(filter);
//        Row row = new Row();
//        result.forEach(d -> {
//            d.set_id(d.getId().toString());
//            row.addChild(d);
//        });
//        return row;
//    }


    public ArrayList<Item> getSectionByID(String sectionsName) {

        AggregateIterable<Item> result = getDB().getCollection("phrase", Item.class).aggregate(Arrays.asList(
                new Document("$match", new Document("section", sectionsName)),
                new Document("$group",
                        new Document("_id", "$item")
                                .append("phrases",
                                        new Document("$push",
                                                new Document("option", "$option")
                                                        .append("description", "$description"))))));
        ArrayList<Item> list = new ArrayList<>();
        result.forEach(list::add);
        return list;

    }
}