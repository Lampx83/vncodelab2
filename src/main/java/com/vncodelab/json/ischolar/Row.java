package com.vncodelab.json.ischolar;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.ArrayList;

@Data
public class Row implements Serializable {

    private ObjectId id;
    private String name;
    private Integer type;
    private String media;
    private String desc;
    private String sheet;
    private String section;
    private String item;
    private String option;
    private String info;

    private String url;
    private String data;
    private String assets;
    private String cloud;

    private String icon;
    private String background;
    private Integer position;
    private Integer time;
    private Integer limit;
    public String score;

    public ArrayList<Row> rows;

    public void addChild(Row row) {
        if (rows == null)
            rows = new ArrayList<>();
        rows.add(row);
    }
}
