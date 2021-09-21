package com.vncodelab.model;

import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class Theater {
    private ObjectId id;
    private Location location;
}


