package com.vncodelab.entity.ischolar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    String id;
    ArrayList<Option> phrases;

}
