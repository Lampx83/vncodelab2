package com.vncodelab.entity;

import com.google.cloud.Timestamp;
import lombok.Data;

@Data
public class Room {

    private String name;
    private String docID;
    private String userID;
    private Timestamp createTime;
    private String roomID;
    private int numberOfStep;

}
