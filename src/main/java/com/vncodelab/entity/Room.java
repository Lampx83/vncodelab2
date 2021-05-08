package com.vncodelab.entity;

import com.google.cloud.Timestamp;

public class Room {

    private String docID;
    private String userID;
    private Timestamp createTime;

    public int getNumberOfStep() {
        return numberOfStep;
    }


    public void setNumberOfStep(int numberOfStep) {
        this.numberOfStep = numberOfStep;
    }

    private String roomID;
    private int numberOfStep;


    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }
}
