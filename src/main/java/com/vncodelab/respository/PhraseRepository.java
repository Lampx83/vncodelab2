package com.vncodelab.respository;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.vncodelab.entity.ischolar.Item;
import com.vncodelab.model.ischolar.Column;
import com.vncodelab.model.ischolar.DataTableRequest;
import com.vncodelab.model.ischolar.JournalList;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;

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


    public JournalList getJournal(DataTableRequest dr, String type) {

        String[] cols = {"Title", "Scopus Sub-Subject Area", "CiteScore 2020", "Publisher", "E-ISSN", "Open Access", "Percent Cited", "Percentile", "Quartile", "RANK", "Rank Out Of", "SJR", "SNIP", "Scholarly Output", "Scopus ASJC Code (Sub-subject Area)", "Scopus Source ID", "Top 10% (CiteScore Percentile)", "URL Scopus Source ID", "Homepage", "Contact", "How_to_publish"};
        JournalList journalList = new JournalList();
        journalList.setDraw(dr.getDraw());

        Document filter = new Document();
        if (!dr.getSearch().getValue().isEmpty()) {
            filter.append("$text", new Document("$search", dr.getSearch().getValue()));
        }
        if (dr.getColumns() != null) {
            for (Column c : dr.getColumns()) {
                if (!c.getSearch().getValue().isEmpty()) {
                    filter.append(cols[c.getData()], Pattern.compile(c.getSearch().getValue() + "(?i)"));
                }
            }
        }
        filter.append("Type", type);

        MongoCollection<Document> collection = getDB().getCollection("scopus_journal");
        long nor = collection.countDocuments(filter);
        journalList.setRecordsFiltered(nor);
        journalList.setRecordsTotal(nor);

        FindIterable<Document> result = collection.find(filter).skip(dr.getStart()).limit(dr.getLength());
        if (dr.getOrder() != null) {
            Bson sort = eq(cols[dr.getOrder().get(0).getColumn()], dr.getOrder().get(0).getOrder());
            result.sort(sort);
        }

        result.forEach(document -> {
            ArrayList item = new ArrayList();
            for (String col : cols) {
                item.add(document.getString(col));
            }
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