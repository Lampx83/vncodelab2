package com.vncodelab.respository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.vncodelab.json.ischolar.Row;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.ArrayList;

@Repository
public class SectionRepository extends AbsRepository {

    public Object getAllSection() {
        try {
            InputStream is = SectionRepository.class.getResourceAsStream("/sections.json"); //Appengine
            return new ObjectMapper().readValue(is, Object.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public Row getSectionByID(String sectionsName) {
        MongoCollection<Row> rows = getDB().getCollection("phrase", Row.class);
        ArrayList<Row> list = new ArrayList<>();
        Document filter = new Document();
        filter.append("section", sectionsName);
        FindIterable<Row> result = rows.find(filter);
        Row row = new Row();
        result.forEach(d -> {
            row.addChild(d);
        });
        return row;
    }
}