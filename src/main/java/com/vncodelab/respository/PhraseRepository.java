package com.vncodelab.respository;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.vncodelab.entity.ischolar.Item;
import com.vncodelab.model.ischolar.JournalList;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

@Repository
public class PhraseRepository extends AbsRepository {

    public ArrayList<Item> getAllPhrases() {  //Items are Sub of Abstract
        AggregateIterable<Item> result = getDB().getCollection("phrase", Item.class).aggregate(Arrays.asList(
                new Document("$group",
                        new Document("_id", "$item")
                                .append("phrases", new Document("$push",
                                                new Document("option", "$option")
                                                        .append("description", "$description")
                                                        .append("section", "$section")
                                        )
                                )
                )
        ));
        ArrayList<Item> list = new ArrayList<>();
        result.forEach(list::add);
        return list;
    }


    public JournalList getJournal(int draw, int start, int length) {
        JournalList journalList = new JournalList();
        journalList.setDraw(draw);
        journalList.setRecordsFiltered(59300);
        journalList.setRecordsTotal(59300);

        Bson filter = new Document();
        MongoCollection<Document> collection = getDB().getCollection("scopus_journal");
        FindIterable<Document> result = collection.find(filter).skip(start).limit(length);
        result.forEach(document -> {
            ArrayList item = new ArrayList();
            item.add(document.getString("Title"));
            item.add(document.getString("SJR"));
            journalList.getData().add(item);
        });


        return journalList;
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