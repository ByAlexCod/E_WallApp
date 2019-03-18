package com.andit.e_wall.data_model;

import java.util.List;

public class BoardModel {
    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBoardId() {
        return boardId;
    }

    public void setBoardId(int boardId) {
        this.boardId = boardId;
    }

    int boardId;

    String name;

    float latitude;
    float longitude;

    public List<MessageModel> getMessagesList() {
        return messagesList;
    }

    public void setMessagesList(List<MessageModel> messagesList) {
        this.messagesList = messagesList;
    }

    private List<MessageModel> messagesList;
}
