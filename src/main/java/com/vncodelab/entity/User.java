package com.vncodelab.entity;

import com.google.cloud.Timestamp;

public class User {
    private String userName;

    private Timestamp lastEnter;

    public Timestamp getLastEnter() {
        return lastEnter;
    }

    public void setLastEnter(Timestamp lastEnter) {
        this.lastEnter = lastEnter;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    private String userID;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    private  String userPhoto;

}
