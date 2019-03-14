package com.andit.e_wall.data_model;

import java.util.List;

public class PathModel {
    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }


    public int getPathId() {
        return pathId;
    }

    public void setPathId(int pathId) {
        this.pathId = pathId;
    }
    private String pathName;

    private int pathId;

    public List<BoardModel> getBoardsList() {
        return boardsList;
    }

    public void setBoardsList(List<BoardModel> boardsList) {
        this.boardsList = boardsList;
    }

    private List<BoardModel> boardsList;
}
